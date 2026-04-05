package ch.lolo.coding.challenge.ai.writer.detector.exception;

import java.util.List;

public record ErrorResponse(String code, String message, List<ErrorReason> reasons) {
}

