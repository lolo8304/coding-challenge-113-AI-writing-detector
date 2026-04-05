package ch.lolo.coding.challenge.ai.writer.detector.exception;

import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<ErrorReason> reasons;

    protected ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, new ErrorReason[0]);
    }

    protected ApiException(HttpStatus status, String code, String message, ErrorReason... reasons) {
        super(message);
        this.status = status;
        this.code = code;
        this.reasons = reasons == null ? List.of() : Arrays.asList(reasons);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public List<ErrorReason> getReasons() {
        return reasons;
    }
}

