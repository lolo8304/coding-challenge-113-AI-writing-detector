package ch.lolo.coding.challenge.ai.writer.detector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Amount {

    private static final BigDecimal STEP = new BigDecimal("0.05");

    private BigDecimal amount;
    private Currency currency;

    /**
     * Rounds {@code value} to the nearest commercial unit of 0.05 (HALF_UP),
     * with exactly 2 decimal places.
     *
     * @param value raw monetary value
     * @return value rounded to nearest 0.05, scale 2
     */
    public static BigDecimal commercialRound(BigDecimal value) {
        return value
                .divide(STEP, 0, RoundingMode.HALF_UP)
                .multiply(STEP)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
