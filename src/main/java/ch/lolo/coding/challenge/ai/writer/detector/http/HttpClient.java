package ch.lolo.coding.challenge.ai.writer.detector.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import java.util.Map;

public class HttpClient {

    private final String integrationName;
    private final String baseUrl;
    private final RestClient restClient;
    private final AuthHeaderStrategy authHeaderStrategy;

    public HttpClient(String integrationName, String baseUrl, RestClient restClient, AuthHeaderStrategy authHeaderStrategy) {
        this.integrationName = integrationName;
        this.baseUrl = baseUrl;
        this.restClient = restClient;
        this.authHeaderStrategy = authHeaderStrategy;
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

        RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(path)
                .headers(httpHeaders -> applyHeaders(httpHeaders, headers));

        if (body != null) {
            return requestSpec.body(body).retrieve().body(responseType);
        }

        return requestSpec.retrieve().body(responseType);
    }

    public <B, R> R exchange(HttpMethod method,
                             String path,
                             B body,
                             Map<String, String> headers,
                             ParameterizedTypeReference<R> responseType) {

        RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(path)
                .headers(httpHeaders -> applyHeaders(httpHeaders, headers));

        if (body != null) {
            return requestSpec.body(body).retrieve().body(responseType);
        }

        return requestSpec.retrieve().body(responseType);
    }

    private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> customHeaders) {
        authHeaderStrategy.apply(httpHeaders);
        customHeaders.forEach(httpHeaders::set);
    }
}
