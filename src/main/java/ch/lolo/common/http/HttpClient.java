package ch.lolo.common.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    private static final Set<String> SENSITIVE_HEADER_HINTS = Set.of(
            "authorization", "api-key", "token", "secret", "password", "cookie"
    );
    private static final Set<String> SENSITIVE_QUERY_HINTS = Set.of(
            "token", "api-key", "api_key", "apikey", "secret", "password", "key"
    );

    private final String integrationName;
    private final String baseUrl;
    private final Supplier<RestClient> restClientSupplier;
    private final Supplier<AuthHeaderStrategy> authHeaderStrategySupplier;
    private final Runnable refreshAction;
    private final HttpClientLoggingSettings loggingSettings;

    public HttpClient(String integrationName, String baseUrl, RestClient restClient, AuthHeaderStrategy authHeaderStrategy) {
        this(
                integrationName,
                baseUrl,
                () -> restClient,
                () -> authHeaderStrategy,
                () -> {
                },
                HttpClientLoggingSettings.disabled()
        );
    }

    public HttpClient(String integrationName,
                      String baseUrl,
                      Supplier<RestClient> restClientSupplier,
                      Supplier<AuthHeaderStrategy> authHeaderStrategySupplier,
                      Runnable refreshAction,
                      HttpClientLoggingSettings loggingSettings) {
        this.integrationName = integrationName;
        this.baseUrl = baseUrl;
        this.restClientSupplier = restClientSupplier;
        this.authHeaderStrategySupplier = authHeaderStrategySupplier;
        this.refreshAction = refreshAction;
        this.loggingSettings = loggingSettings;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public <R> R get(String path, Class<R> responseType) {
        return exchange(HttpMethod.GET, path, null, Map.of(), responseType);
    }

    public <R> R get(String path, Map<String, String> headers, Class<R> responseType) {
        return exchange(HttpMethod.GET, path, null, headers, responseType);
    }

    public <B, R> R post(String path, B body, Class<R> responseType) {
        return exchange(HttpMethod.POST, path, body, Map.of(), responseType);
    }

    public <B, R> R post(String path, B body, Map<String, String> headers, Class<R> responseType) {
        return exchange(HttpMethod.POST, path, body, headers, responseType);
    }

    public <B, R> R put(String path, B body, Class<R> responseType) {
        return exchange(HttpMethod.PUT, path, body, Map.of(), responseType);
    }

    public <B, R> R put(String path, B body, Map<String, String> headers, Class<R> responseType) {
        return exchange(HttpMethod.PUT, path, body, headers, responseType);
    }

    public <B, R> R patch(String path, B body, Class<R> responseType) {
        return exchange(HttpMethod.PATCH, path, body, Map.of(), responseType);
    }

    public <B, R> R patch(String path, B body, Map<String, String> headers, Class<R> responseType) {
        return exchange(HttpMethod.PATCH, path, body, headers, responseType);
    }

    public <R> R delete(String path, Class<R> responseType) {
        return exchange(HttpMethod.DELETE, path, null, Map.of(), responseType);
    }

    public <R> R delete(String path, Map<String, String> headers, Class<R> responseType) {
        return exchange(HttpMethod.DELETE, path, null, headers, responseType);
    }

    public <B, R> R exchange(HttpMethod method,
                             String path,
                             B body,
                             Map<String, String> headers,
                             Class<R> responseType) {

        refreshAction.run();
        RestClient restClient = restClientSupplier.get();
        AuthHeaderStrategy authHeaderStrategy = authHeaderStrategySupplier.get();

        logRequest(method, path, headers);
        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(path)
                .headers(httpHeaders -> applyHeaders(httpHeaders, headers, authHeaderStrategy));

            R response;
            if (body != null) {
                response = requestSpec.body(body).retrieve().body(responseType);
            } else {
                response = requestSpec.retrieve().body(responseType);
            }

            logResponse(method, path, String.valueOf(responseType.getSimpleName()));
            return response;
        } catch (RuntimeException exception) {
            logRequestOnError(method, path, headers);
            logResponseOnError(method, path, exception);
            logError(method, path, exception);
            throw exception;
        }
    }

    public <B, R> R exchange(HttpMethod method,
                             String path,
                             B body,
                             Map<String, String> headers,
                             ParameterizedTypeReference<R> responseType) {

        refreshAction.run();
        RestClient restClient = restClientSupplier.get();
        AuthHeaderStrategy authHeaderStrategy = authHeaderStrategySupplier.get();

        logRequest(method, path, headers);
        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(path)
                .headers(httpHeaders -> applyHeaders(httpHeaders, headers, authHeaderStrategy));

            R response;
            if (body != null) {
                response = requestSpec.body(body).retrieve().body(responseType);
            } else {
                response = requestSpec.retrieve().body(responseType);
            }

            logResponse(method, path, String.valueOf(responseType.getType()));
            return response;
        } catch (RuntimeException exception) {
            logRequestOnError(method, path, headers);
            logResponseOnError(method, path, exception);
            logError(method, path, exception);
            throw exception;
        }
    }

    private void applyHeaders(HttpHeaders httpHeaders,
                              Map<String, String> customHeaders,
                              AuthHeaderStrategy authHeaderStrategy) {
        authHeaderStrategy.apply(httpHeaders);
        if (customHeaders != null) {
            customHeaders.forEach(httpHeaders::set);
        }
    }

    private void logRequest(HttpMethod method, String path, Map<String, String> headers) {
        if (!loggingSettings.logRequest()) {
            return;
        }
        LOGGER.info("[{}] Outbound request {} {} headers={}",
                integrationName,
                method,
                sanitizePath(path),
                sanitizeHeaders(headers));
    }

    private void logRequestOnError(HttpMethod method, String path, Map<String, String> headers) {
        if (!loggingSettings.logErrors() || loggingSettings.logRequest()) {
            return;
        }
        LOGGER.warn("[{}] Outbound request (error path) {} {} headers={}",
                integrationName,
                method,
                sanitizePath(path),
                sanitizeHeaders(headers));
    }

    private void logResponse(HttpMethod method, String path, String mappedType) {
        if (!loggingSettings.logResponse()) {
            return;
        }
        LOGGER.info("[{}] Outbound response {} {} mappedType={}", integrationName, method, sanitizePath(path), mappedType);
    }

    private void logResponseOnError(HttpMethod method, String path, RuntimeException exception) {
        if (!loggingSettings.logErrors() || loggingSettings.logResponse()) {
            return;
        }
        LOGGER.warn("[{}] Outbound response (error path) {} {} mappedType=error errorType={}",
                integrationName,
                method,
                sanitizePath(path),
                exception.getClass().getSimpleName());
    }

    private void logError(HttpMethod method, String path, RuntimeException exception) {
        if (!loggingSettings.logErrors()) {
            return;
        }
        LOGGER.error("[{}] Outbound error {} {}: {}", integrationName, method, sanitizePath(path), exception.getMessage(), exception);
    }

    private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        if (loggingSettings.showSensitiveData()) {
            return headers == null ? Map.of() : headers;
        }

        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }

        Map<String, String> sanitized = new LinkedHashMap<>();
        headers.forEach((key, value) -> sanitized.put(key, isSensitiveHeader(key) ? "***" : value));
        return sanitized;
    }

    private boolean isSensitiveHeader(String headerName) {
        String normalized = headerName == null ? "" : headerName.toLowerCase();
        for (String hint : SENSITIVE_HEADER_HINTS) {
            if (normalized.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizePath(String path) {
        if (loggingSettings.showSensitiveData()) {
            return path;
        }

        if (path == null) {
            return null;
        }

        int fragmentStart = path.indexOf('#');
        String fragment = fragmentStart >= 0 ? path.substring(fragmentStart) : "";
        String withoutFragment = fragmentStart >= 0 ? path.substring(0, fragmentStart) : path;

        int queryStart = withoutFragment.indexOf('?');
        if (queryStart < 0) {
            return path;
        }

        String basePath = withoutFragment.substring(0, queryStart);
        String query = withoutFragment.substring(queryStart + 1);
        if (query.isBlank()) {
            return path;
        }

        String[] parts = query.split("&");
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            int equalsIndex = part.indexOf('=');
            String key = equalsIndex >= 0 ? part.substring(0, equalsIndex) : part;

            if (!isSensitiveQueryParameter(key)) {
                continue;
            }

            parts[index] = equalsIndex >= 0 ? key + "=***" : key + "=***";
        }

        return basePath + "?" + String.join("&", parts) + fragment;
    }

    private boolean isSensitiveQueryParameter(String queryKey) {
        String normalized = queryKey == null ? "" : queryKey.toLowerCase();

        if (loggingSettings.sensitiveQueryParameters() != null &&
                loggingSettings.sensitiveQueryParameters().stream().anyMatch(normalized::contains)) {
            return true;
        }

        for (String hint : SENSITIVE_QUERY_HINTS) {
            if (normalized.contains(hint)) {
                return true;
            }
        }
        return false;
    }
}

