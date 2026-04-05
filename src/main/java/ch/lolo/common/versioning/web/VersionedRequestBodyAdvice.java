package ch.lolo.common.versioning.web;

import ch.lolo.common.versioning.VersionContext;
import ch.lolo.common.versioning.VersionContextHolder;
import ch.lolo.common.versioning.VersionTransformProcess;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.common.versioning.transform.JsonVersionTransformer;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class VersionedRequestBodyAdvice<T extends JsonVersionTransformer> extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;
    private final VersionTransformProcess<T> transformProcess;

    protected VersionedRequestBodyAdvice(ObjectMapper objectMapper, VersionTransformProcess<T> transformProcess) {
        this.objectMapper = objectMapper;
        this.transformProcess = transformProcess;
    }

    @Override
    public boolean supports(MethodParameter methodParameter,
                            @NonNull Type targetType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return supportedPayloadType().equals(methodParameter.getParameterType());
    }

    @Override
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type targetType,
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        HttpServletRequest request = resolveRequest(inputMessage);
        if (request == null || !supportsEndpoint(request)) {
            return inputMessage;
        }

        ObjectNode source = objectMapper.readValue(inputMessage.getBody(), ObjectNode.class);
        VersionContext context = VersionContextHolder.getRequired(request);
        ObjectNode transformed = transformProcess.transform(source, transitions(context));

        byte[] payload = objectMapper.writeValueAsBytes(transformed);
        return new RewrittenHttpInputMessage(inputMessage, payload);
    }

    protected List<VersionTransition> transitions(VersionContext context) {
        return context.requestUpgradeTransitions();
    }

    protected abstract Class<?> supportedPayloadType();

    protected abstract boolean supportsEndpoint(HttpServletRequest request);

    private HttpServletRequest resolveRequest(HttpInputMessage inputMessage) {
        if (inputMessage instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest();
        }

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }

        return null;
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

