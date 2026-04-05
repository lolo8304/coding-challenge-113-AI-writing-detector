package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.common.http.BearerTokenAuthStrategy;
import ch.lolo.common.http.OAuthClientCredentialsTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BearerTokenAuthStrategyTest {

    @Mock
    private OAuthClientCredentialsTokenProvider tokenProvider;

    @InjectMocks
    private BearerTokenAuthStrategy strategy;

    @Test
    void apply_setsAuthorizationBearerHeader() {
        // Arrange
        when(tokenProvider.getAccessToken()).thenReturn("my-access-token");
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer my-access-token");
    }

    @Test
    void apply_usesTokenFromProvider() {
        // Arrange
        when(tokenProvider.getAccessToken()).thenReturn("another-token-123");
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer another-token-123");
    }

    @Test
    void apply_doesNotAddExtraHeaders() {
        // Arrange
        when(tokenProvider.getAccessToken()).thenReturn("token");
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        assertThat(headers.size()).isEqualTo(1);
        assertThat(headers.containsHeader(HttpHeaders.AUTHORIZATION)).isTrue();
    }
}

