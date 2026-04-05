package ch.lolo.coding.challenge.ai.writer.detector.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApiException {

    public TooManyRequestsException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.TOO_MANY_REQUESTS, code, message, reasons);
    }
}

