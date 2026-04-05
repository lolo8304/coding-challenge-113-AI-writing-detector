package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.common.versioning.VersionTransformerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractResponseDowngradeFactory extends VersionTransformerFactory<ContractResponseDowngrader> {

    public ContractResponseDowngradeFactory(List<ContractResponseDowngrader> downgraders) {
        super(
                downgraders,
                "RESPONSE_DOWNGRADER_NOT_FOUND",
                transition -> "No response downgrader registered for " + transition.from() + " -> " + transition.to()
        );
    }
}
