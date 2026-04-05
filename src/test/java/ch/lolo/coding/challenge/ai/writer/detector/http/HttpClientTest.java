package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.common.http.AuthHeaderStrategy;
import ch.lolo.common.http.HttpClient;
import ch.lolo.common.http.HttpClientLoggingSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private AuthHeaderStrategy authHeaderStrategy;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new HttpClient("integration-a", "https://example.com", restClient, authHeaderStrategy);
    }

    @Test
    void getIntegrationName_returnsConfiguredIntegrationName() {
        assertThat(httpClient.getIntegrationName()).isEqualTo("integration-a");
    }

    // --- getBaseUrl ---

    @Test
    void getBaseUrl_returnsConfiguredBaseUrl() {
        // Arrange & Act
        String baseUrl = httpClient.getBaseUrl();

        // Assert
        assertThat(baseUrl).isEqualTo("https://example.com");
    }

    // --- get ---

    @Test
    void get_withPath_performsGetRequestAndReturnsBody() {
        // Arrange
        when(restClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/test")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("response-body");

        // Act
        String result = httpClient.get("/test", String.class);

        // Assert
        assertThat(result).isEqualTo("response-body");
        verify(restClient).method(HttpMethod.GET);
    }

    @Test
    void get_withPathAndHeaders_performsGetRequestWithCustomHeaders() {
        // Arrange
        when(restClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/test")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("response-with-headers");

        // Act
        String result = httpClient.get("/test", Map.of("X-Custom", "value"), String.class);

        // Assert
        assertThat(result).isEqualTo("response-with-headers");
    }

    // --- post ---

    @Test
    void post_withBody_performsPostRequestAndReturnsBody() {
        // Arrange
        when(restClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/create")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body("request-body")).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("created");

        // Act
        String result = httpClient.post("/create", "request-body", String.class);

        // Assert
        assertThat(result).isEqualTo("created");
        verify(restClient).method(HttpMethod.POST);
    }

    @Test
    void post_withBodyAndHeaders_performsPostRequestWithCustomHeaders() {
        // Arrange
        when(restClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/create")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body("request-body")).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("created");

        // Act
        String result = httpClient.post("/create", "request-body", Map.of("X-Trace", "123"), String.class);

        // Assert
        assertThat(result).isEqualTo("created");
    }

    // --- put ---

    @Test
    void put_withBody_performsPutRequestAndReturnsBody() {
        // Arrange
        when(restClient.method(HttpMethod.PUT)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/update/1")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body("updated")).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("ok");

        // Act
        String result = httpClient.put("/update/1", "updated", String.class);

        // Assert
        assertThat(result).isEqualTo("ok");
        verify(restClient).method(HttpMethod.PUT);
    }

    // --- patch ---

    @Test
    void patch_withBody_performsPatchRequestAndReturnsBody() {
        // Arrange
        when(restClient.method(HttpMethod.PATCH)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/patch/1")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body("patch-data")).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("patched");

        // Act
        String result = httpClient.patch("/patch/1", "patch-data", String.class);

        // Assert
        assertThat(result).isEqualTo("patched");
        verify(restClient).method(HttpMethod.PATCH);
    }

    // --- delete ---

    @Test
    void delete_withPath_performsDeleteRequestAndReturnsBody() {
        // Arrange
        when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/delete/1")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Void.class)).thenReturn(null);

        // Act
        Void result = httpClient.delete("/delete/1", Void.class);

        // Assert
        assertThat(result).isNull();
        verify(restClient).method(HttpMethod.DELETE);
    }

    // --- exchange without body ---

    @Test
    void exchange_withNullBody_doesNotCallBodyOnRequestSpec() {
        // Arrange
        when(restClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/no-body")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("result");

        // Act
        String result = httpClient.exchange(HttpMethod.GET, "/no-body", null, Map.of(), String.class);

        // Assert
        assertThat(result).isEqualTo("result");
        verify(requestBodySpec, never()).body(any(Object.class));
    }

    // --- authHeaderStrategy is applied ---

    @Test
    void exchange_appliesAuthHeaderStrategy() {
        // Arrange
        when(restClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/secure")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenAnswer(invocation -> {
            java.util.function.Consumer<org.springframework.http.HttpHeaders> consumer =
                    invocation.getArgument(0, java.util.function.Consumer.class);
            var httpHeaders = new org.springframework.http.HttpHeaders();
            consumer.accept(httpHeaders);
            return requestBodySpec;
        });
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("secured");

        // Act
        httpClient.get("/secure", String.class);

        // Assert
        verify(authHeaderStrategy).apply(any());
    }

    @Test
    void exchange_runsRefreshActionBeforeRequest() {
        AtomicInteger refreshCounter = new AtomicInteger();
        HttpClient clientWithRefresh = new HttpClient(
                "integration-a",
                "https://example.com",
                () -> restClient,
                () -> authHeaderStrategy,
                refreshCounter::incrementAndGet,
                HttpClientLoggingSettings.disabled()
        );

        when(restClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/refresh")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("ok");

        clientWithRefresh.get("/refresh", String.class);

        assertThat(refreshCounter.get()).isEqualTo(1);
    }

    @Test
    void sanitizePath_masksDefaultAndConfiguredSensitiveQueryParameters() {
        HttpClient clientWithMasking = new HttpClient(
                "integration-a",
                "https://example.com",
                () -> restClient,
                () -> authHeaderStrategy,
                () -> {
                },
                new HttpClientLoggingSettings(true, false, false, false, Set.of("client_assertion"))
        );

        String sanitized = ReflectionTestUtils.invokeMethod(
                clientWithMasking,
                "sanitizePath",
                "/hello?token=abc&client_assertion=jwt&name=ok"
        );

        assertThat(sanitized).isEqualTo("/hello?token=***&client_assertion=***&name=ok");
    }

    @Test
    void sanitizePath_withShowSensitiveDataTrue_keepsOriginalQueryValues() {
        HttpClient clientWithoutMasking = new HttpClient(
                "integration-a",
                "https://example.com",
                () -> restClient,
                () -> authHeaderStrategy,
                () -> {
                },
                new HttpClientLoggingSettings(true, false, false, true, Set.of("client_assertion"))
        );

        String originalPath = "/hello?token=abc&client_assertion=jwt&name=ok";
        String sanitized = ReflectionTestUtils.invokeMethod(
                clientWithoutMasking,
                "sanitizePath",
                originalPath
        );

        assertThat(sanitized).isEqualTo(originalPath);
    }
}

