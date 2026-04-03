package ch.lolo.coding.challenge.ai.writer.detector.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AmountTest {

    // ── commercialRound: exact cases ──────────────────────────────────────────

    static Stream<Arguments> exactRoundCases() {
        return Stream.of(
                // already on a 0.05 boundary → unchanged
                Arguments.of("0.00",  "0.00"),
                Arguments.of("0.05",  "0.05"),
                Arguments.of("0.10",  "0.10"),
                Arguments.of("9.95",  "9.95"),
                // round down (< 0.025 distance to lower step)
                Arguments.of("0.02",  "0.00"),
                Arguments.of("0.07",  "0.05"),
                Arguments.of("1.22",  "1.20"),
                // round up (>= 0.025 distance to upper step)
                Arguments.of("0.03",  "0.05"),
                Arguments.of("0.08",  "0.10"),
                Arguments.of("1.23",  "1.25"),
                // exact midpoint → HALF_UP rounds up
                Arguments.of("0.025", "0.05"),
                Arguments.of("1.225", "1.25")
        );
    }

    @ParameterizedTest(name = "commercialRound({0}) = {1}")
    @MethodSource("exactRoundCases")
    void commercialRound_roundsToNearestFiveCents(String input, String expected) {
        // Arrange
        BigDecimal value = new BigDecimal(input);

        // Act
        BigDecimal result = Amount.commercialRound(value);

        // Assert
        assertThat(result).isEqualByComparingTo(new BigDecimal(expected));
    }

    // ── commercialRound: scale always 2 ──────────────────────────────────────

    static Stream<Arguments> scaleInputs() {
        return Stream.of(
                Arguments.of("0.00"),
                Arguments.of("1.00"),
                Arguments.of("12345.67"),
                Arguments.of("0.025"),
                Arguments.of("99.999")
        );
    }

    @ParameterizedTest(name = "commercialRound({0}) has scale 2")
    @MethodSource("scaleInputs")
    void commercialRound_alwaysReturnsTwoDecimalPlaces(String input) {
        // Arrange
        BigDecimal value = new BigDecimal(input);

        // Act
        BigDecimal result = Amount.commercialRound(value);

        // Assert
        assertThat(result.scale())
                .as("Result %s should have scale 2", result)
                .isEqualTo(2);
    }

    // ── commercialRound: result divisible by 0.05 ─────────────────────────────

    static Stream<Arguments> divisibilityInputs() {
        return Stream.of(
                Arguments.of("0.01"),
                Arguments.of("0.03"),
                Arguments.of("1.23"),
                Arguments.of("9999.99"),
                Arguments.of("0.025"),
                Arguments.of("12345.678")
        );
    }

    @ParameterizedTest(name = "commercialRound({0}) is divisible by 0.05")
    @MethodSource("divisibilityInputs")
    void commercialRound_resultDivisibleByStep(String input) {
        // Arrange
        BigDecimal value    = new BigDecimal(input);
        BigDecimal step     = new BigDecimal("0.05");

        // Act
        BigDecimal result   = Amount.commercialRound(value);

        // Assert
        BigDecimal remainder = result.remainder(step);
        assertThat(remainder.compareTo(BigDecimal.ZERO))
                .as("Result %s should be divisible by 0.05", result)
                .isEqualTo(0);
    }
}

