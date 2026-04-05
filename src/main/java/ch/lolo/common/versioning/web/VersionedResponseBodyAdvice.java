package ch.lolo.common.versioning.web;

import ch.lolo.common.versioning.VersionContext;
import ch.lolo.common.versioning.VersionContextHolder;
import ch.lolo.common.versioning.VersionTransformProcess;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.common.versioning.transform.JsonVersionTransformer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

public abstract class VersionedResponseBodyAdvice<T extends JsonVersionTransformer> implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final VersionTransformProcess<T> transformProcess;

    protected VersionedResponseBodyAdvice(ObjectMapper objectMapper, VersionTransformProcess<T> transformProcess) {
        this.objectMapper = objectMapper;
        this.transformProcess = transformProcess;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return supportedPayloadType().isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null || !supportedPayloadType().isAssignableFrom(body.getClass())) {
            return body;
        }

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return body;
        }

        HttpServletRequest rawRequest = servletRequest.getServletRequest();
        if (!supportsEndpoint(rawRequest)) {
            return body;
        }

        VersionContext context = VersionContextHolder.getRequired(rawRequest);
        ObjectNode source = objectMapper.valueToTree(body);
        return transformProcess.transform(source, transitions(context));
    }

    protected List<VersionTransition> transitions(VersionContext context) {
        return context.responseDowngradeTransitions();
    }

    protected abstract Class<?> supportedPayloadType();

    protected abstract boolean supportsEndpoint(HttpServletRequest request);
}

