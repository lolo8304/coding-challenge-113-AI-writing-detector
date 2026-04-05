package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.request;

import ch.lolo.common.exception.BadRequestException;
import ch.lolo.common.versioning.VersionTransition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContractRequestUpgradeFactory {

    private final Map<VersionTransition, ContractRequestUpgrader> upgraderByTransition;

    public ContractRequestUpgradeFactory(List<ContractRequestUpgrader> upgraders) {
        this.upgraderByTransition = upgraders.stream()
                .collect(Collectors.toMap(ContractRequestUpgrader::transition, Function.identity()));
    }

    public ContractRequestUpgrader getRequired(VersionTransition transition) {
        ContractRequestUpgrader upgrader = upgraderByTransition.get(transition);
        if (upgrader != null) {
            return upgrader;
        }
        throw new BadRequestException(
                "REQUEST_UPGRADER_NOT_FOUND",
                "No request upgrader registered for " + transition.from() + " -> " + transition.to()
        );
    }
}
