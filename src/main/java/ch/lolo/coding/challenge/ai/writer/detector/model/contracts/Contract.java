package ch.lolo.coding.challenge.ai.writer.detector.model.contracts;

import ch.lolo.coding.challenge.ai.writer.detector.model.Amount;
import ch.lolo.coding.challenge.ai.writer.detector.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Contract {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private Amount premium;

    /**
     * Factory: creates a Contract from a name, computes premium as hash of name.
     */
    public static Contract fromName(String name) {
        long hash = Math.abs((long) name.hashCode());
        // Scale hash into range 0–9999.95 CHF, then commercial-round to nearest 0.05
        BigDecimal raw = BigDecimal.valueOf(hash % 1_000_000, 2); // e.g. 12345.67
        BigDecimal premiumAmount = Amount.commercialRound(raw);
        return Contract.builder()
                .name(name)
                .premium(Amount.builder()
                        .amount(premiumAmount)
                        .currency(Currency.CHF)
                        .build())
                .build();
    }
}
