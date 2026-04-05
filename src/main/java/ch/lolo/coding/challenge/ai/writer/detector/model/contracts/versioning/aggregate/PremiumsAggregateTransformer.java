package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.aggregate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class PremiumsAggregateTransformer {

    public void ensureCurrency(ObjectNode json, String currency) {
        ObjectNode premiums = ensurePremiums(json);
        if (!premiums.hasNonNull("currency")) {
            premiums.put("currency", currency);
        }
    }

    public void convertPremiumToAggregate(ObjectNode json) {
        JsonNode premium = json.get("premium");
        if (premium == null || premium.isNull()) {
            return;
        }

        ObjectNode premiums = ensurePremiums(json);
        premiums.set("amount", premium);
        if (!premiums.has("currency")) {
            premiums.put("currency", "CHF");
        }
        json.remove("premium");
    }

    public void convertAggregateToPremium(ObjectNode json) {
        JsonNode premiums = json.get("premium");
        if (premiums == null || !premiums.isObject()) {
            return;
        }

        JsonNode amount = premiums.get("amount");
        if (amount != null) {
            json.set("premium", amount);
        }
        json.remove("premium");
    }

    private ObjectNode ensurePremiums(ObjectNode json) {
        JsonNode existing = json.get("premium");
        if (existing != null && existing.isObject()) {
            return (ObjectNode) existing;
        }

        ObjectNode created = JsonNodeFactory.instance.objectNode();
        json.set("premium", created);
        return created;
    }
}

