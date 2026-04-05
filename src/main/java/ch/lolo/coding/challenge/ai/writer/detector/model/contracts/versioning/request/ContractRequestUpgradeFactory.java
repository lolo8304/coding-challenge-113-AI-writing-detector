package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.request;

import ch.lolo.common.versioning.VersionTransformerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractRequestUpgradeFactory extends VersionTransformerFactory<ContractRequestUpgrader> {

    public ContractRequestUpgradeFactory(List<ContractRequestUpgrader> upgraders) {
        super(
                upgraders,
                "REQUEST_UPGRADER_NOT_FOUND",
                transition -> "No request upgrader registered for " + transition.from() + " -> " + transition.to()
        );
    }
}
