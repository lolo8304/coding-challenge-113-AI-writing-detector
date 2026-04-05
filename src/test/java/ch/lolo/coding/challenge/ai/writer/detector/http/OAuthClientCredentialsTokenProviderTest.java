package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.common.http.OAuthClientCredentialsTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OAuthClientCredentialsTokenProviderTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ApplicationConfiguration.ClientCredentials properties;
    private OAuthClientCredentialsTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        properties = new ApplicationConfiguration.ClientCredentials();
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setTokenUrl("https://auth.example.com/oauth/token");
        tokenProvider = new OAuthClientCredentialsTokenProvider(restClient, properties);
    }

    @SuppressWarnings("unchecked")
    private void mockTokenEndpoint(OAuthClientCredentialsTokenProvider.TokenResponse response) {
        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any());
        doReturn(requestBodySpec).when(requestBodySpec).headers(any());
        doReturn(requestBodySpec).when((RestClient.RequestBodySpec) requestBodySpec).body(any(org.springframework.util.MultiValueMap.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(response).when(responseSpec).body(OAuthClientCredentialsTokenProvider.TokenResponse.class);
    }

    // --- successful token fetch ---

    @Test
    void getAccessToken_returnsTokenFromEndpoint() {
        // Arrange
        var tokenResponse = new OAuthClientCredentialsTokenProvider.TokenResponse("access-token-abc", "Bearer", 300L, null);
        mockTokenEndpoint(tokenResponse);

        // Act
        String token = tokenProvider.getAccessToken();

        // Assert
        assertThat(token).isEqualTo("access-token-abc");
    }

    @Test
    void getAccessToken_calledTwice_usesCache() {
        // Arrange
        var tokenResponse = new OAuthClientCredentialsTokenProvider.TokenResponse("cached-token", "Bearer", 300L, null);
        mockTokenEndpoint(tokenResponse);

        // Act
        String first = tokenProvider.getAccessToken();
        String second = tokenProvider.getAccessToken();

        // Assert
        assertThat(first).isEqualTo("cached-token");
        assertThat(second).isEqualTo("cached-token");
        verify(restClient, times(1)).post(); // only one HTTP call
    }

    @Test
    void getAccessToken_withScope_includesScopeInRequest() {
        // Arrange
        properties.setScope("read write");
        var tokenResponse = new OAuthClientCredentialsTokenProvider.TokenResponse("scoped-token", "Bearer", 300L, "read write");
        mockTokenEndpoint(tokenResponse);

        // Act
        String token = tokenProvider.getAccessToken();

        // Assert
        assertThat(token).isEqualTo("scoped-token");
    }

    @Test
    void getAccessToken_withNullExpiresIn_usesDefaultExpiry() {
        // Arrange
        var tokenResponse = new OAuthClientCredentialsTokenProvider.TokenResponse("token-no-expiry", "Bearer", null, null);
        mockTokenEndpoint(tokenResponse);

        // Act
        String token = tokenProvider.getAccessToken();

        // Assert
        assertThat(token).isEqualTo("token-no-expiry");
    }

    // --- error cases ---

    @Test
    void getAccessToken_withMissingClientId_throwsIllegalStateException() {
        // Arrange
        properties.setClientId(null);
        OAuthClientCredentialsTokenProvider providerWithoutId = new OAuthClientCredentialsTokenProvider(restClient, properties);

        // Act & Assert
        assertThatThrownBy(providerWithoutId::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("client-id");
    }

    @Test
    void getAccessToken_withMissingClientSecret_throwsIllegalStateException() {
        // Arrange
        properties.setClientSecret(null);
        OAuthClientCredentialsTokenProvider providerWithoutSecret = new OAuthClientCredentialsTokenProvider(restClient, properties);

        // Act & Assert
        assertThatThrownBy(providerWithoutSecret::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("client-secret");
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAccessToken_whenEndpointReturnsNull_throwsIllegalStateException() {
        // Arrange
        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).contentType(any());
        doReturn(requestBodySpec).when(requestBodySpec).headers(any());
        doReturn(requestBodySpec).when((RestClient.RequestBodySpec) requestBodySpec).body(any(org.springframework.util.MultiValueMap.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(null).when(responseSpec).body(OAuthClientCredentialsTokenProvider.TokenResponse.class);

        // Act & Assert
        assertThatThrownBy(tokenProvider::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("access token");
    }

    @Test
    void getAccessToken_whenEndpointReturnsBlankToken_throwsIllegalStateException() {
        // Arrange
        var tokenResponse = new OAuthClientCredentialsTokenProvider.TokenResponse("  ", "Bearer", 300L, null);
        mockTokenEndpoint(tokenResponse);

        // Act & Assert
        assertThatThrownBy(tokenProvider::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("access token");
    }

    // --- TokenResponse record ---

    @Test
    void tokenResponse_storesAllFields() {
        // Arrange & Act
        var tokenResponse = new OAuthClientCredentialsTokenProvider.TokenResponse("my-token", "Bearer", 3600L, "read");

        // Assert
        assertThat(tokenResponse.accessToken()).isEqualTo("my-token");
        assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.expiresIn()).isEqualTo(3600L);
        assertThat(tokenResponse.scope()).isEqualTo("read");
    }
}
