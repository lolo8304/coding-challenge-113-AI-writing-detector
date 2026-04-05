package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.common.versioning.VersionTransformProcess;
import ch.lolo.common.versioning.VersionTransition;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractResponseDowngradeProcess extends VersionTransformProcess<ContractResponseDowngrader> {

    public ContractResponseDowngradeProcess(ContractResponseDowngradeFactory factory) {
        super(factory);
    }

    public ObjectNode downgrade(ObjectNode source, List<VersionTransition> transitions) {
        return transform(source, transitions);
    }
}

