package ch.lolo.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String code, String message, ErrorReason... reasons) {
        super(HttpStatus.UNAUTHORIZED, code, message, reasons);
    }
}

