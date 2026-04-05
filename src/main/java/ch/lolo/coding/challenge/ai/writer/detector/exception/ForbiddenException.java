package ch.lolo.coding.challenge.ai.writer.detector.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.FORBIDDEN, code, message, reasons);
    }
}

