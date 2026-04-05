package ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.request;

import ch.lolo.coding.challenge.ai.writer.detector.versioning.ApiVersion;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionTransition;
import ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.aggregate.PremiumsAggregateTransformer;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.transform.BaseJsonTransformer;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ContractRequestUpgrade2025To2026 extends BaseJsonTransformer implements ContractRequestUpgrader {

    private final PremiumsAggregateTransformer premiumsTransformer;

    public ContractRequestUpgrade2025To2026(PremiumsAggregateTransformer premiumsTransformer) {
        this.premiumsTransformer = premiumsTransformer;
    }

    @Override
    public VersionTransition transition() {
        return new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2026_01_01);
    }

    @Override
    public ObjectNode transform(ObjectNode source) {
        ObjectNode target = source.deepCopy();
        checkMandatoryField(target, "customerName");
        renameField(target, "customerName", "name");
        custom(target, json -> premiumsTransformer.ensureCurrency(json, "CHF"));
        removeField(target, "legacyRisk");
        return target;
    }
}

