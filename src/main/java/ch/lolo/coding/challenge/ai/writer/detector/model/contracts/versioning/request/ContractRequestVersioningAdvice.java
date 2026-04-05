package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.request;

import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.Contract;
import ch.lolo.common.versioning.web.VersionedRequestBodyAdvice;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ContractRequestVersioningAdvice extends VersionedRequestBodyAdvice<ContractRequestUpgrader> {

    public ContractRequestVersioningAdvice(ObjectMapper objectMapper, ContractRequestUpgradeProcess upgradeProcess) {
        super(objectMapper, upgradeProcess);
    }

    @Override
    protected Class<?> supportedPayloadType() {
        return Contract.class;
    }

    @Override
    protected boolean supportsEndpoint(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/rest/ai/detector/v1/contracts");
    }
}

