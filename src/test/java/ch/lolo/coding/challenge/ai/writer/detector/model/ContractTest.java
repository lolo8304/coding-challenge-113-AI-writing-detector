package ch.lolo.coding.challenge.ai.writer.detector.model;

import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.Contract;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.ContractFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTest {

    static final String UUID_PATTERN =
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final ContractFactory contractFactory = new ContractFactory();

    // ── id ────────────────────────────────────────────────────────────────────

    @Test
    void builder_generatesUuidId() {
        // Arrange
        Contract contract = Contract.builder().name("Alice").build();

        // Act
        String id = contract.getId();

        // Assert
        assertThat(id)
                .isNotNull()
                .matches(UUID_PATTERN);
    }

    @Test
    void builder_generatesDifferentIdsForEachInstance() {
        // Arrange + Act
        Contract c1 = Contract.builder().name("Alice").build();
        Contract c2 = Contract.builder().name("Alice").build();

        // Assert
        assertThat(c1.getId()).isNotEqualTo(c2.getId());
    }

    // ── fromName: name and id ─────────────────────────────────────────────────

    @ParameterizedTest(name = "fromName(\"{0}\") sets name and generates UUID id")
    @ValueSource(strings = {"Alice", "Bob", "Charlie", "Diana", "X", "LongNameForHashing123"})
    void fromName_setsNameAndGeneratesUuidId(String name) {
        // Arrange + Act
        Contract contract = contractFactory.fromName(name);

        // Assert
        assertThat(contract.getName()).isEqualTo(name);
        assertThat(contract.getId())
                .isNotNull()
                .matches(UUID_PATTERN);
    }

    // ── fromName: currency ────────────────────────────────────────────────────

    @ParameterizedTest(name = "fromName(\"{0}\") sets currency to CHF")
    @ValueSource(strings = {"Alice", "Bob", "Charlie"})
    void fromName_setCurrencyToCHF(String name) {
        // Arrange + Act
        Contract contract = contractFactory.fromName(name);

        // Assert
        assertThat(contract.getPremium().getCurrency()).isEqualTo(Currency.CHF);
    }

    // ── fromName: premium not null ────────────────────────────────────────────

    @ParameterizedTest(name = "fromName(\"{0}\") premiums fields are not null")
    @ValueSource(strings = {"Alice", "Bob", "Charlie"})
    void fromName_premiumsNotNull(String name) {
        // Arrange + Act
        Contract contract = contractFactory.fromName(name);

        // Assert
        assertThat(contract.getPremium()).isNotNull();
        assertThat(contract.getPremium().getAmount()).isNotNull();
        assertThat(contract.getPremium().getCurrency()).isNotNull();
    }

    // ── fromName: premium rounding ────────────────────────────────────────────

    @ParameterizedTest(name = "fromName(\"{0}\") premium is rounded to 0.05")
    @ValueSource(strings = {"Alice", "Bob", "Charlie", "Diana", "X", "LongNameForHashing123"})
    void fromName_premiumIsRoundedToNearestFiveCents(String name) {
        // Arrange
        BigDecimal step = new BigDecimal("0.05");

        // Act
        BigDecimal premium = contractFactory.fromName(name).getPremium().getAmount();

        // Assert
        BigDecimal remainder = premium.remainder(step);
        assertThat(remainder.compareTo(BigDecimal.ZERO))
                .as("Premium %s should be divisible by 0.05", premium)
                .isEqualTo(0);
    }

    @ParameterizedTest(name = "fromName(\"{0}\") premium >= 0.05 CHF")
    @ValueSource(strings = {"Alice", "Bob", "Charlie", "Diana", "X", "LongNameForHashing123"})
    void fromName_premiumIsAtLeastFiveCents(String name) {
        // Arrange
        BigDecimal minPremium = new BigDecimal("0.05");

        // Act
        BigDecimal premium = contractFactory.fromName(name).getPremium().getAmount();

        // Assert
        assertThat(premium.compareTo(minPremium))
                .as("Premium %s should be at least 0.05 CHF", premium)
                .isGreaterThanOrEqualTo(0);
    }

    @ParameterizedTest(name = "fromName(\"{0}\") premium has exactly 2 decimal places")
    @ValueSource(strings = {"Alice", "Bob", "Charlie", "Diana", "X", "LongNameForHashing123"})
    void fromName_premiumHasTwoDecimalPlaces(String name) {
        // Arrange + Act
        BigDecimal premium = contractFactory.fromName(name).getPremium().getAmount();

        // Assert
        assertThat(premium.scale())
                .as("Premium %s should have scale 2", premium)
                .isEqualTo(2);
    }

    // ── fromName: determinism & uniqueness ────────────────────────────────────

    @ParameterizedTest(name = "fromName(\"{0}\") is deterministic")
    @ValueSource(strings = {"Alice", "Bob", "Charlie"})
    void fromName_deterministicPremiumForSameName(String name) {
        // Arrange + Act
        BigDecimal p1 = contractFactory.fromName(name).getPremium().getAmount();
        BigDecimal p2 = contractFactory.fromName(name).getPremium().getAmount();

        // Assert
        assertThat(p1).isEqualByComparingTo(p2);
    }

    static Stream<Arguments> differentNamePairs() {
        return Stream.of(
                Arguments.of("Alice",   "Bob"),
                Arguments.of("Charlie", "Diana"),
                Arguments.of("X",       "LongNameForHashing123")
        );
    }

    @ParameterizedTest(name = "fromName(\"{0}\") and fromName(\"{1}\") produce different premiums")
    @MethodSource("differentNamePairs")
    void fromName_differentNamesProduceDifferentPremiums(String nameA, String nameB) {
        // Arrange + Act
        BigDecimal premiumA = contractFactory.fromName(nameA).getPremium().getAmount();
        BigDecimal premiumB = contractFactory.fromName(nameB).getPremium().getAmount();

        // Assert
        assertThat(premiumA).isNotEqualByComparingTo(premiumB);
    }
}
