package ch.lolo.coding.challenge.ai.writer.detector.model.contracts;

import ch.lolo.coding.challenge.ai.writer.detector.model.Amount;
import ch.lolo.coding.challenge.ai.writer.detector.model.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ContractFactory {

    public static String toName(String firstname, String lastname) {
        // build name from firstName + " " lastName but add " " only if both are not null and not empty
        var name = (firstname != null && !firstname.isEmpty() ? firstname : "") +
                (lastname != null && !lastname.isEmpty() ? " " + lastname : "");
        return name.trim();
    }
    
    public static String[] splitName(String name) {
        var target = new String[2];
        String[] nameParts = name.split(" ");
        if (nameParts.length == 2) {
            target[0]= nameParts[0];
            target[1]= nameParts[1];
        } else if (nameParts.length == 3) {
            target[0]= nameParts[0] + " " + nameParts[1];
            target[1]= nameParts[2];
        } else if (nameParts.length == 4) {
            target[0]= nameParts[0];
            target[1]= nameParts[1] + " " + nameParts[2] + " " + nameParts[3];
        } else {
            target[0]= "";
            target[1]= name;
        }
        return target;
    }
    
    public Contract fromName(String name) {
        var splitted = splitName(name);
        return fromName(splitted[0], splitted[1]);
    }

    public Contract fromName(String firstname, String lastname) {
        var name = toName(firstname, lastname);
        long hash = Math.abs((long) name.hashCode());
        BigDecimal raw = BigDecimal.valueOf(hash % 1_000_000, 2);
        BigDecimal premiumAmount = Amount.commercialRound(raw);

        return Contract.builder()
                .firstName(firstname)
                .lastName(lastname)
                .premium(Amount.builder()
                        .amount(premiumAmount)
                        .currency(Currency.CHF)
                        .build())
                .build();
    }
}

