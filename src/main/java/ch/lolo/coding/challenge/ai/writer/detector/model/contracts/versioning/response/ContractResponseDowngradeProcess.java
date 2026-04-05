package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.common.versioning.VersionTransition;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractResponseDowngradeProcess {

    private final ContractResponseDowngradeFactory factory;

    public ContractResponseDowngradeProcess(ContractResponseDowngradeFactory factory) {
        this.factory = factory;
    }

    public ObjectNode downgrade(ObjectNode source, List<VersionTransition> transitions) {
        ObjectNode current = source.deepCopy();
        for (VersionTransition transition : transitions) {
            current = factory.getRequired(transition).transform(current);
        }
        return current;
    }
}

