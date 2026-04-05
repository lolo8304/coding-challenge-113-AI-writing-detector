package ch.lolo.coding.challenge.ai.writer.detector.model.contracts;

import ch.lolo.coding.challenge.ai.writer.detector.model.Amount;
import ch.lolo.coding.challenge.ai.writer.detector.model.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ContractFactory {

    public Contract fromName(String name) {
        long hash = Math.abs((long) name.hashCode());
        BigDecimal raw = BigDecimal.valueOf(hash % 1_000_000, 2);
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

