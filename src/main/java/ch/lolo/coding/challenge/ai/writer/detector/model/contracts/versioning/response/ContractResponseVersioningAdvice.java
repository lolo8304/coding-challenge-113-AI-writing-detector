package ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response;

import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.Contract;
import ch.lolo.common.versioning.VersionContext;
import ch.lolo.common.versioning.VersionContextHolder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ContractResponseVersioningAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final ContractResponseDowngradeProcess downgradeProcess;

    public ContractResponseVersioningAdvice(ObjectMapper objectMapper,
                                            ContractResponseDowngradeProcess downgradeProcess) {
        this.objectMapper = objectMapper;
        this.downgradeProcess = downgradeProcess;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Contract.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(body instanceof Contract contract)) {
            return body;
        }

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return body;
        }

        HttpServletRequest rawRequest = servletRequest.getServletRequest();
        if (!isContractPost(rawRequest)) {
            return body;
        }

        VersionContext context = VersionContextHolder.getRequired(rawRequest);
        ObjectNode source = objectMapper.valueToTree(contract);
        ObjectNode downgraded = downgradeProcess.downgrade(source, context.responseDowngradeTransitions());
        return downgraded;
    }

    private boolean isContractPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/rest/ai/detector/v1/contracts");
    }
}

