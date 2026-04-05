package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.request;

import ch.lolo.common.versioning.VersionTransformProcess;
import ch.lolo.common.versioning.VersionTransition;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractRequestUpgradeProcess extends VersionTransformProcess<ContractRequestUpgrader> {

    public ContractRequestUpgradeProcess(ContractRequestUpgradeFactory factory) {
        super(factory);
    }

    public ObjectNode upgrade(ObjectNode source, List<VersionTransition> transitions) {
        return transform(source, transitions);
    }
}

