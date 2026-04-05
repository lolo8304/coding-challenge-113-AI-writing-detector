package ch.lolo.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.BAD_REQUEST, code, message, reasons);
    }
}

