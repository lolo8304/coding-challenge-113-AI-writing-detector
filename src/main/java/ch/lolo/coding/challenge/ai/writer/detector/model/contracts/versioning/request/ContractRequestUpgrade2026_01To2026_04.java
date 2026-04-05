package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.request;

import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.aggregate.PremiumsAggregateTransformer;
import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.common.versioning.transform.BaseJsonTransformer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

@Component
public class ContractRequestUpgrade2026_01To2026_04 extends BaseJsonTransformer implements ContractRequestUpgrader {

    private final PremiumsAggregateTransformer premiumsTransformer;

    public ContractRequestUpgrade2026_01To2026_04(PremiumsAggregateTransformer premiumsTransformer) {
        this.premiumsTransformer = premiumsTransformer;
    }

    @Override
    public VersionTransition transition() {
        return new VersionTransition(ApiVersion.V2026_01_01, ApiVersion.V2026_04_01);
    }

    @Override
    public ObjectNode transform(ObjectNode source) {
        ObjectNode target = source.deepCopy();
        // split name into firstName and lastname by " "
        // if only 2 all fine. if 3 mainly 2 firstNames, if 4 firstName and lastName has 2
        String name = target.path("name").asString();
        String[] nameParts = name.split(" ");
        if (nameParts.length == 2) {
            target.put("firstName", nameParts[0]);
            target.put("lastName", nameParts[1]);
        } else if (nameParts.length == 3) {
            target.put("firstName", nameParts[0] + " " + nameParts[1]);
            target.put("lastName", nameParts[2]);
        } else if (nameParts.length == 4) {
            target.put("firstName", nameParts[0]);
            target.put("lastName", nameParts[1] + " " + nameParts[2] + " " + nameParts[3]);
        } else {
            target.put("firstName", "");
            target.put("lastName", name);
        }
        return target;
    }
}

