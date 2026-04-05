package ch.lolo.common.exception;

import java.util.List;

public record ErrorResponse(String code, String message, List<ErrorReason> reasons) {
}

