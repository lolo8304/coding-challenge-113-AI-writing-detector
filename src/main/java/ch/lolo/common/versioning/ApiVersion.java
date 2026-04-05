package ch.lolo.common.versioning;

import ch.lolo.common.exception.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public enum ApiVersion {
    V2024_01_01(LocalDate.of(2024, 1, 1)),
    V2025_01_01(LocalDate.of(2025, 1, 1)),
    V2026_01_01(LocalDate.of(2026, 1, 1));

    public static final String VERSION_HEADER = "x-version";

    private final LocalDate effectiveFrom;

    ApiVersion(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public static ApiVersion latest() {
        ApiVersion[] values = values();
        return values[values.length - 1];
    }

    public static ApiVersion resolve(String headerValue) {
        if (headerValue == null || headerValue.isBlank() || "latest".equalsIgnoreCase(headerValue)) {
            return latest();
        }

        LocalDate requestedDate;
        try {
            requestedDate = LocalDate.parse(headerValue);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(
                    "INVALID_VERSION_HEADER",
                    "Header x-version must be ISO date (yyyy-MM-dd) or 'latest'"
            );
        }

        return Arrays.stream(values())
                .filter(version -> !requestedDate.isBefore(version.effectiveFrom))
                .reduce((first, second) -> second)
                .orElse(V2024_01_01);
    }
}
