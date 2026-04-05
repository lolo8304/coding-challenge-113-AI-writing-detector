package ch.lolo.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.NOT_FOUND, code, message, reasons);
    }
}

