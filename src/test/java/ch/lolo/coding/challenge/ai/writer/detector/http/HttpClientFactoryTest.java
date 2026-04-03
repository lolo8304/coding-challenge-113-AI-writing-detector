package ch.lolo.coding.challenge.ai.writer.detector.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class HttpClientFactoryTest {

    @Mock
    private ClientHttpRequestFactory requestFactory;

    @Mock
    private AuthHeaderStrategy authHeaderStrategy;

    private HttpClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new HttpClientFactory(
                Map.of(
                        "integration-a", new HttpClientFactory.BackendIntegration("https://service-a.example.com", requestFactory, authHeaderStrategy),
                        "integration-b", new HttpClientFactory.BackendIntegration("https://service-b.example.com", requestFactory, authHeaderStrategy)
                )
        );
    }

    @Test
    void create_returnsNonNullHttpClient() {
        // Act
        HttpClient client = factory.create("integration-a");

        // Assert
        assertThat(client).isNotNull();
    }

    @Test
    void create_returnedHttpClientHasCorrectBaseUrl() {
        // Act
        HttpClient client = factory.create("integration-a");

        // Assert
        assertThat(client.getBaseUrl()).isEqualTo("https://service-a.example.com");
        assertThat(client.getIntegrationName()).isEqualTo("integration-a");
    }

    @Test
    void create_calledTwiceWithDifferentIntegrations_returnsDifferentClients() {
        // Act
        HttpClient client1 = factory.create("integration-a");
        HttpClient client2 = factory.create("integration-b");

        // Assert
        assertThat(client1.getBaseUrl()).isEqualTo("https://service-a.example.com");
        assertThat(client2.getBaseUrl()).isEqualTo("https://service-b.example.com");
        assertThat(client1.getIntegrationName()).isEqualTo("integration-a");
        assertThat(client2.getIntegrationName()).isEqualTo("integration-b");
        assertThat(client1).isNotSameAs(client2);
    }

    @Test
    void create_calledTwiceWithSameIntegration_returnsDistinctInstances() {
        // Act
        HttpClient client1 = factory.create("integration-a");
        HttpClient client2 = factory.create("integration-a");

        // Assert
        assertThat(client1).isNotSameAs(client2);
        assertThat(client1.getBaseUrl()).isEqualTo(client2.getBaseUrl());
    }

    @Test
    void create_withUnknownIntegration_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> factory.create("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("app.rest.unknown");
    }
}

