package ch.lolo.coding.challenge.ai.writer.detector.exception;

import ch.lolo.common.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.Arguments;

class GlobalExceptionInterceptorTest {

    private final GlobalExceptionInterceptor interceptor = new GlobalExceptionInterceptor();

    @ParameterizedTest
    @MethodSource("apiExceptions")
    void handleApiException_mapsCustomCodeMessageAndReasons(ApiException exception, HttpStatus expectedStatus) {
        // Arrange
        ErrorReason reason = new ErrorReason("DETAIL", "Detailed reason");
        ApiException exceptionWithReason = withReason(exception, reason);

        // Act
        ResponseEntity<ErrorResponse> response = interceptor.handleApiException(exceptionWithReason);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(exception.getCode());
        assertThat(response.getBody().message()).isEqualTo(exception.getMessage());
        assertThat(response.getBody().reasons()).hasSize(1);
        assertThat(response.getBody().reasons().get(0).code()).isEqualTo("DETAIL");
        assertThat(response.getBody().reasons().get(0).message()).isEqualTo("Detailed reason");
    }

    @ParameterizedTest
    @MethodSource("apiExceptions")
    void handleApiException_addsDefaultReason_whenNoReasonProvided(ApiException exception, HttpStatus expectedStatus) {
        // Act
        ResponseEntity<ErrorResponse> response = interceptor.handleApiException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(exception.getCode());
        assertThat(response.getBody().message()).isEqualTo(exception.getMessage());
        assertThat(response.getBody().reasons()).hasSize(1);
        assertThat(response.getBody().reasons().get(0).code()).isEqualTo(exception.getCode());
        assertThat(response.getBody().reasons().get(0).message()).isEqualTo(exception.getMessage());
    }

    @Test
    void handle_usesReasonCode999_whenExceptionOnlyHasMessage() {
        // Arrange
        RuntimeException exception = new RuntimeException("plain-message-only");

        // Act
        ResponseEntity<ErrorResponse> response = interceptor.handle(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("500");
        assertThat(response.getBody().message()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().reasons()).hasSize(1);
        assertThat(response.getBody().reasons().get(0).code()).isEqualTo("999");
        assertThat(response.getBody().reasons().get(0).message()).isEqualTo("plain-message-only");
    }

    @Test
    void handle_usesStatusCodeForReason_whenMessageIsMissing() {
        // Arrange
        RuntimeException exception = new RuntimeException();

        // Act
        ResponseEntity<ErrorResponse> response = interceptor.handle(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().reasons()).hasSize(1);
        assertThat(response.getBody().reasons().get(0).code()).isEqualTo("500");
        assertThat(response.getBody().reasons().get(0).message()).isEqualTo("Internal Server Error");
    }

    private static Stream<Arguments> apiExceptions() {
        return Stream.of(
                arguments(new BadRequestException("BAD_REQUEST_CODE", "Bad request message"), HttpStatus.BAD_REQUEST),
                arguments(new UnauthorizedException("UNAUTHORIZED_CODE", "Unauthorized message"), HttpStatus.UNAUTHORIZED),
                arguments(new ForbiddenException("FORBIDDEN_CODE", "Forbidden message"), HttpStatus.FORBIDDEN),
                arguments(new NotFoundException("NOT_FOUND_CODE", "Not found message"), HttpStatus.NOT_FOUND),
                arguments(new ConflictException("CONFLICT_CODE", "Conflict message"), HttpStatus.CONFLICT),
                arguments(new TooManyRequestsException("TOO_MANY_REQUESTS_CODE", "Too many requests message"), HttpStatus.TOO_MANY_REQUESTS),
                arguments(new InternalServerErrorException("INTERNAL_SERVER_ERROR_CODE", "Internal server error message"), HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    private ApiException withReason(ApiException exception, ErrorReason reason) {
        if (exception instanceof BadRequestException) {
            return new BadRequestException(exception.getCode(), exception.getMessage(), reason);
        }
        if (exception instanceof UnauthorizedException) {
            return new UnauthorizedException(exception.getCode(), exception.getMessage(), reason);
        }
        if (exception instanceof ForbiddenException) {
            return new ForbiddenException(exception.getCode(), exception.getMessage(), reason);
        }
        if (exception instanceof NotFoundException) {
            return new NotFoundException(exception.getCode(), exception.getMessage(), reason);
        }
        if (exception instanceof ConflictException) {
            return new ConflictException(exception.getCode(), exception.getMessage(), reason);
        }
        if (exception instanceof TooManyRequestsException) {
            return new TooManyRequestsException(exception.getCode(), exception.getMessage(), reason);
        }
        if (exception instanceof InternalServerErrorException) {
            return new InternalServerErrorException(exception.getCode(), exception.getMessage(), reason);
        }
        throw new IllegalStateException("Unexpected exception type: " + exception.getClass().getName());
    }
}
