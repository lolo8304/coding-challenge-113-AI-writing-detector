package ch.lolo.coding.challenge.ai.writer.detector.controller.logging;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Spring MVC interceptor that logs inbound HTTP requests, responses, and errors for controllers.
 *
 * <p>Logging behaviour per request:
 * <ul>
 *   <li>If the request header {@code x-axa-monitoring: true} is present, request and response
 *       logging is skipped – errors are still logged.</li>
 *   <li>Otherwise, request/response/error logging follows {@code app.logging} settings.</li>
 * </ul>
 */
public class InboundLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(InboundLoggingInterceptor.class);

    static final String MONITORING_HEADER = "x-axa-monitoring";
    private static final List<String> SENSITIVE_QUERY_HINTS = List.of(
            "token",
            "secret",
            "password",
            "assertion",
            "apikey",
            "api_key"
    );

    private static final String ATTR_START_TIME = InboundLoggingInterceptor.class.getName() + ".startTime";
    private static final String ATTR_MONITORING = InboundLoggingInterceptor.class.getName() + ".monitoring";
    private static final String ATTR_EFFECTIVE = InboundLoggingInterceptor.class.getName() + ".effective";
    private final ApplicationConfiguration.Logging logging;

    public InboundLoggingInterceptor(ApplicationConfiguration.Logging logging) {
        this.logging = logging;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        EffectiveLoggingSettings effective = resolveEffectiveSettings((HandlerMethod) handler);

        boolean monitoring = isMonitoringRequest(request);
        long startTime = System.currentTimeMillis();

        request.setAttribute(ATTR_MONITORING, monitoring);
        request.setAttribute(ATTR_START_TIME, startTime);
        request.setAttribute(ATTR_EFFECTIVE, effective);

        if (effective.logRequest() && !monitoring) {
            log.info("[INBOUND] {} {} | from={} | monitoring=false",
                    request.getMethod(),
                    sanitizeRequestUri(request, effective.showSensitiveData()),
                    request.getRemoteAddr());
        }

        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex) {

        if (!(handler instanceof HandlerMethod)) {
            return;
        }

        EffectiveLoggingSettings effective = (EffectiveLoggingSettings) request.getAttribute(ATTR_EFFECTIVE);
        if (effective == null) {
            effective = resolveEffectiveSettings((HandlerMethod) handler);
        }

        boolean monitoring = Boolean.TRUE.equals(request.getAttribute(ATTR_MONITORING));
        Object startAttr = request.getAttribute(ATTR_START_TIME);
        long startTime = startAttr instanceof Long start ? start : System.currentTimeMillis();
        long durationMs = System.currentTimeMillis() - startTime;

        if (ex != null && effective.logErrors()) {
            log.error("[INBOUND] ERROR {} {} | status={} | duration={}ms | error={}",
                    request.getMethod(),
                    sanitizeRequestUri(request, effective.showSensitiveData()),
                    response.getStatus(),
                    durationMs,
                    ex.getMessage(),
                    ex);
            return;
        }

        if (effective.logResponse() && !monitoring) {
            log.info("[INBOUND] COMPLETED {} {} | status={} | duration={}ms",
                    request.getMethod(),
                    sanitizeRequestUri(request, effective.showSensitiveData()),
                    response.getStatus(),
                    durationMs);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean isMonitoringRequest(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader(MONITORING_HEADER));
    }

    private String sanitizeRequestUri(HttpServletRequest request, boolean showSensitiveData) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return uri;
        }

        if (showSensitiveData) {
            return uri + "?" + query;
        }

        String[] parts = query.split("&");
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            int equalsIndex = part.indexOf('=');
            String key = equalsIndex >= 0 ? part.substring(0, equalsIndex) : part;

            if (!isSensitiveQueryParameter(key)) {
                continue;
            }
            parts[index] = key + "=***";
        }

        return uri + "?" + String.join("&", parts);
    }

    private boolean isSensitiveQueryParameter(String queryKey) {
        String normalized = queryKey == null ? "" : queryKey.toLowerCase();

        if (logging.getSensitiveQueryParameters().stream()
                .filter(key -> key != null && !key.isBlank())
                .map(String::toLowerCase)
                .anyMatch(normalized::contains)) {
            return true;
        }

        return SENSITIVE_QUERY_HINTS.stream().anyMatch(normalized::contains);
    }

    private EffectiveLoggingSettings resolveEffectiveSettings(HandlerMethod handlerMethod) {
        ControllerLogging methodLevel = handlerMethod.getMethodAnnotation(ControllerLogging.class);
        if (methodLevel != null) {
            return new EffectiveLoggingSettings(
                    methodLevel.logRequest(),
                    methodLevel.logResponse(),
                    methodLevel.logErrors(),
                    logging.isShowSensitiveData()
            );
        }

        ControllerLogging classLevel = handlerMethod.getBeanType().getAnnotation(ControllerLogging.class);
        if (classLevel != null) {
            return new EffectiveLoggingSettings(
                    classLevel.logRequest(),
                    classLevel.logResponse(),
                    classLevel.logErrors(),
                    logging.isShowSensitiveData()
            );
        }

        return new EffectiveLoggingSettings(
                logging.isLogRequest(),
                logging.isLogResponse(),
                logging.isLogErrors(),
                logging.isShowSensitiveData()
        );
    }

    private record EffectiveLoggingSettings(
            boolean logRequest,
            boolean logResponse,
            boolean logErrors,
            boolean showSensitiveData
    ) {
    }
}

