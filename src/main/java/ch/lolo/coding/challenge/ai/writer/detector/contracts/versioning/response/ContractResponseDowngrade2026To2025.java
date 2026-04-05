package ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.response;

import ch.lolo.coding.challenge.ai.writer.detector.versioning.ApiVersion;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionTransition;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.transform.BaseJsonTransformer;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ContractResponseDowngrade2026To2025 extends BaseJsonTransformer implements ContractResponseDowngrader {

    @Override
    public VersionTransition transition() {
        return new VersionTransition(ApiVersion.V2026_01_01, ApiVersion.V2025_01_01);
    }

    @Override
    public ObjectNode transform(ObjectNode source) {
        ObjectNode target = source.deepCopy();
        renameField(target, "name", "customerName");
        removeField(target, "id");
        return target;
    }
}

