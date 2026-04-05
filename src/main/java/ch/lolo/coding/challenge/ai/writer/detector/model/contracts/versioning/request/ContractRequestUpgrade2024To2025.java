package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.request;

import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.aggregate.PremiumsAggregateTransformer;
import ch.lolo.common.versioning.transform.BaseJsonTransformer;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ContractRequestUpgrade2024To2025 extends BaseJsonTransformer implements ContractRequestUpgrader {

    private final PremiumsAggregateTransformer premiumsTransformer;

    public ContractRequestUpgrade2024To2025(PremiumsAggregateTransformer premiumsTransformer) {
        this.premiumsTransformer = premiumsTransformer;
    }

    @Override
    public VersionTransition transition() {
        return new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01);
    }

    @Override
    public ObjectNode transform(ObjectNode source) {
        ObjectNode target = source.deepCopy();
        checkMandatoryField(target, "name");
        renameField(target, "name", "customerName");
        splitField(
                target,
                "premium",
                "premiums.amount",
                value -> value,
                "premiums.currency",
                value -> JsonNodeFactory.instance.textNode("CHF")
        );
        custom(target, premiumsTransformer::convertPremiumToAggregate);
        return target;
    }
}

