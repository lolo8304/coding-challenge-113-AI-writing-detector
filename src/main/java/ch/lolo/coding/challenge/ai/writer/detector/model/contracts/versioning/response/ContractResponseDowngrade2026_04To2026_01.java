package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.common.versioning.transform.BaseJsonTransformer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

@Component
public class ContractResponseDowngrade2026_04To2026_01 extends BaseJsonTransformer implements ContractResponseDowngrader {

    @Override
    public VersionTransition transition() {
        return new VersionTransition(ApiVersion.V2026_04_01, ApiVersion.V2026_01_01);
    }

    @Override
    public ObjectNode transform(ObjectNode source) {
        ObjectNode target = source.deepCopy();
        removeField(target, "firstName");
        removeField(target, "lastName");
        return target;
    }
}

