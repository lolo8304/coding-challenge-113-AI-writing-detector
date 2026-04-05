package ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.request;

import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionTransition;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractRequestUpgradeProcess {

    private final ContractRequestUpgradeFactory factory;

    public ContractRequestUpgradeProcess(ContractRequestUpgradeFactory factory) {
        this.factory = factory;
    }

    public ObjectNode upgrade(ObjectNode source, List<VersionTransition> transitions) {
        ObjectNode current = source.deepCopy();
        for (VersionTransition transition : transitions) {
            current = factory.getRequired(transition).transform(current);
        }
        return current;
    }
}

