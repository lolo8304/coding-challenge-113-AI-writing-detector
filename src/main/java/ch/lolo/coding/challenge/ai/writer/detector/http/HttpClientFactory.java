package ch.lolo.coding.challenge.ai.writer.detector.http;

import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

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

        return new HttpClient(
                integrationName,
                integration.baseUrl(),
                integration.restClientSupplier(),
                integration.authHeaderStrategySupplier(),
                integration.refreshAction(),
                integration.loggingSettings()
        );
    }

    public record BackendIntegration(String baseUrl,
                                     Supplier<RestClient> restClientSupplier,
                                     Supplier<AuthHeaderStrategy> authHeaderStrategySupplier,
                                     Runnable refreshAction,
                                     HttpClientLoggingSettings loggingSettings) {
        public BackendIntegration {
            Objects.requireNonNull(restClientSupplier, "restClientSupplier must not be null");
            Objects.requireNonNull(authHeaderStrategySupplier, "authHeaderStrategySupplier must not be null");
            Objects.requireNonNull(refreshAction, "refreshAction must not be null");
            Objects.requireNonNull(loggingSettings, "loggingSettings must not be null");
        }
    }
}

