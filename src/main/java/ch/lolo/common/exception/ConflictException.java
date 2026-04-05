package ch.lolo.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.CONFLICT, code, message, reasons);
    }
}

