package ch.lolo.coding.challenge.ai.writer.detector.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends ApiException {

    public InternalServerErrorException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code, message, reasons);
    }
}

