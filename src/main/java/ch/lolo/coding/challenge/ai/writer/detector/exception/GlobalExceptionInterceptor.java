package ch.lolo.coding.challenge.ai.writer.detector.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionInterceptor {

    private static final String FALLBACK_REASON_CODE = "999";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        List<ErrorReason> reasons = normalizeReasons(exception);

        ErrorResponse response = new ErrorResponse(
                exception.getCode(),
                exception.getMessage(),
                reasons
        );

        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception exception) {
        HttpStatus status = resolveStatus(exception);
        ErrorReason reason = new ErrorReason(resolveReasonCode(exception, status), resolveReasonMessage(exception, status));

        ErrorResponse response = new ErrorResponse(
                String.valueOf(status.value()),
                status.getReasonPhrase(),
                List.of(reason)
        );

        return ResponseEntity.status(status).body(response);
    }

    private HttpStatus resolveStatus(Exception exception) {
        if (exception instanceof ResponseStatusException responseStatusException) {
            HttpStatus resolved = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            return resolved == null ? HttpStatus.INTERNAL_SERVER_ERROR : resolved;
        }

        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(exception.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            if (responseStatus.value() != HttpStatus.INTERNAL_SERVER_ERROR) {
                return responseStatus.value();
            }
            return responseStatus.code();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveReasonCode(Exception exception, HttpStatus status) {
        String explicitCode = tryExtractCode(exception);
        if (hasText(explicitCode)) {
            return explicitCode;
        }

        if (hasExceptionMessage(exception)) {
            return FALLBACK_REASON_CODE;
        }

        return String.valueOf(status.value());
    }

    private String resolveReasonMessage(Exception exception, HttpStatus status) {
        if (exception instanceof ResponseStatusException responseStatusException && hasText(responseStatusException.getReason())) {
            return responseStatusException.getReason();
        }

        if (hasText(exception.getMessage())) {
            return exception.getMessage();
        }

        return status.getReasonPhrase();
    }

    private String tryExtractCode(Exception exception) {
        try {
            Method method = exception.getClass().getMethod("getCode");
            Object code = method.invoke(exception);
            return code == null ? null : String.valueOf(code);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasExceptionMessage(Exception exception) {
        if (exception instanceof ResponseStatusException responseStatusException) {
            return hasText(responseStatusException.getReason());
        }
        return hasText(exception.getMessage());
    }

    private List<ErrorReason> normalizeReasons(ApiException exception) {
        if (exception.getReasons() == null || exception.getReasons().isEmpty()) {
            return List.of(new ErrorReason(exception.getCode(), exception.getMessage()));
        }
        return exception.getReasons();
    }
}
