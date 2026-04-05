package ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.request;

import ch.lolo.coding.challenge.ai.writer.detector.contracts.model.Contract;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionContext;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionContextHolder;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class ContractRequestVersioningAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;
    private final ContractRequestUpgradeProcess upgradeProcess;

    public ContractRequestVersioningAdvice(ObjectMapper objectMapper, ContractRequestUpgradeProcess upgradeProcess) {
        this.objectMapper = objectMapper;
        this.upgradeProcess = upgradeProcess;
    }

    @Override
    public boolean supports(MethodParameter methodParameter,
                            @NonNull Type targetType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return Contract.class.equals(methodParameter.getParameterType());
    }

    @Override
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type targetType,
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        HttpServletRequest request = resolveRequest(inputMessage);
        if (request == null) {
            return inputMessage;
        }

        if (!isContractPost(request)) {
            return inputMessage;
        }

        ObjectNode source = objectMapper.readValue(inputMessage.getBody(), ObjectNode.class);
        VersionContext context = VersionContextHolder.getRequired(request);
        ObjectNode upgraded = upgradeProcess.upgrade(source, context.requestUpgradeTransitions());

        byte[] payload = objectMapper.writeValueAsBytes(upgraded);
        return new RewrittenHttpInputMessage(inputMessage, payload);
    }

    private HttpServletRequest resolveRequest(HttpInputMessage inputMessage) {
        if (inputMessage instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest();
        }

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }

        return null;
    }

    private boolean isContractPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/rest/ai/detector/v1/contracts");
    }

    private static final class RewrittenHttpInputMessage implements HttpInputMessage {

        private final HttpInputMessage delegate;
        private final byte[] payload;

        private RewrittenHttpInputMessage(HttpInputMessage delegate, byte[] payload) {
            this.delegate = delegate;
            this.payload = payload;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(payload);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.putAll(delegate.getHeaders());
            headers.setContentLength(payload.length);
            headers.set("Content-Type", "application/json; charset=" + StandardCharsets.UTF_8);
            return headers;
        }
    }
}

