package ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.response;

import ch.lolo.coding.challenge.ai.writer.detector.exception.BadRequestException;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionTransition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContractResponseDowngradeFactory {

    private final Map<VersionTransition, ContractResponseDowngrader> downgraderByTransition;

    public ContractResponseDowngradeFactory(List<ContractResponseDowngrader> downgraders) {
        this.downgraderByTransition = downgraders.stream()
                .collect(Collectors.toMap(ContractResponseDowngrader::transition, Function.identity()));
    }

    public ContractResponseDowngrader getRequired(VersionTransition transition) {
        ContractResponseDowngrader downgrader = downgraderByTransition.get(transition);
        if (downgrader != null) {
            return downgrader;
        }
        throw new BadRequestException(
                "RESPONSE_DOWNGRADER_NOT_FOUND",
                "No response downgrader registered for " + transition.from() + " -> " + transition.to()
        );
    }
}
