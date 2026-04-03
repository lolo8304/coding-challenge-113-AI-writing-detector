package ch.lolo.coding.challenge.ai.writer.detector.http;
import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Objects;

public class OAuthClientCredentialsTokenProvider {

    private static final long EXPIRY_SAFETY_SECONDS = 30;

    private final RestClient restClient;
    private final ApplicationConfiguration.ClientCredentials properties;

    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public OAuthClientCredentialsTokenProvider(RestClient restClient,
                                               ApplicationConfiguration.ClientCredentials properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public synchronized String getAccessToken() {
        Instant now = Instant.now();
        if (cachedToken != null && now.isBefore(expiresAt)) {
            return cachedToken;
        }

        if (properties.getClientId() == null || properties.getClientSecret() == null) {
            throw new IllegalStateException("OAuth client_credentials requires app.rest.<name>.http.oauth.client-credentials.client-id and client-secret");
        }

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        if (properties.getScope() != null && !properties.getScope().isBlank()) {
            requestBody.add("scope", properties.getScope());
        }

        TokenResponse tokenResponse = restClient.post()
                .uri(properties.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(headers -> headers.setBasicAuth(properties.getClientId(), properties.getClientSecret()))
                .body(requestBody)
                .retrieve()
                .body(TokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("Token endpoint did not return an access token");
        }

        long expiresIn = Objects.requireNonNullElse(tokenResponse.expiresIn(), 300L);
        cachedToken = tokenResponse.accessToken();
        expiresAt = now.plusSeconds(Math.max(1L, expiresIn - EXPIRY_SAFETY_SECONDS));
        return cachedToken;
    }

    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("scope") String scope
    ) {
    }
}

