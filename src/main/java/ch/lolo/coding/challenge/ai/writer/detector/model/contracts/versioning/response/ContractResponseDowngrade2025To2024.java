package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.aggregate.PremiumsAggregateTransformer;
import ch.lolo.common.versioning.transform.BaseJsonTransformer;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ContractResponseDowngrade2025To2024 extends BaseJsonTransformer implements ContractResponseDowngrader {

    private final PremiumsAggregateTransformer premiumsTransformer;

    public ContractResponseDowngrade2025To2024(PremiumsAggregateTransformer premiumsTransformer) {
        this.premiumsTransformer = premiumsTransformer;
    }

    @Override
    public VersionTransition transition() {
        return new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2024_01_01);
    }

    @Override
    public ObjectNode transform(ObjectNode source) {
        ObjectNode target = source.deepCopy();
        renameField(target, "customerName", "name");
        custom(target, premiumsTransformer::convertAggregateToPremium);
        return target;
    }
}

