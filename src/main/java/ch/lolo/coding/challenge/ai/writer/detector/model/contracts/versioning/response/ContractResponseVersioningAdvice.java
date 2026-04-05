package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.Contract;
import ch.lolo.common.versioning.web.VersionedResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ContractResponseVersioningAdvice extends VersionedResponseBodyAdvice<ContractResponseDowngrader> {

    public ContractResponseVersioningAdvice(ObjectMapper objectMapper,
                                            ContractResponseDowngradeProcess downgradeProcess) {
        super(objectMapper, downgradeProcess);
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

