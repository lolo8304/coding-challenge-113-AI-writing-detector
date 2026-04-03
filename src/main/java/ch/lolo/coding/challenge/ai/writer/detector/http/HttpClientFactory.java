package ch.lolo.coding.challenge.ai.writer.detector.http;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

public class HttpClientFactory {

    private final Map<String, BackendIntegration> integrations;

    public HttpClientFactory(Map<String, BackendIntegration> integrations) {
        this.integrations = integrations;
    }

    public HttpClient create(String integrationName) {
        BackendIntegration integration = integrations.get(integrationName);
        if (integration == null) {
            throw new IllegalArgumentException("No HTTP integration configured for app.rest." + integrationName);
        }
        if (integration.baseUrl() == null || integration.baseUrl().isBlank()) {
            throw new IllegalStateException("HTTP integration app.rest." + integrationName + ".url must be configured");
        }

        RestClient restClient = RestClient.builder()
                .baseUrl(integration.baseUrl())
                .requestFactory(integration.requestFactory())
                .build();
        return new HttpClient(integrationName, integration.baseUrl(), restClient, integration.authHeaderStrategy());
    }

    public record BackendIntegration(String baseUrl,
                                     ClientHttpRequestFactory requestFactory,
                                     AuthHeaderStrategy authHeaderStrategy) {
        public BackendIntegration {
            Objects.requireNonNull(requestFactory, "requestFactory must not be null");
            Objects.requireNonNull(authHeaderStrategy, "authHeaderStrategy must not be null");
        }
    }
}

