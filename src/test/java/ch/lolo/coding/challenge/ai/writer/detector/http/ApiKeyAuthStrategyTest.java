package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.common.http.ApiKeyAuthStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthStrategyTest {

    @Test
    void apply_setsConfiguredApiKeyHeader() {
        ApiKeyAuthStrategy strategy = new ApiKeyAuthStrategy("x-api-key", "secret-value");
        HttpHeaders headers = new HttpHeaders();

        strategy.apply(headers);

        assertThat(headers.getFirst("x-api-key")).isEqualTo("secret-value");
    }
}

