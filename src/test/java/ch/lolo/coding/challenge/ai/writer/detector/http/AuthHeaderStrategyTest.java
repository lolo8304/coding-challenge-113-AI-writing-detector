package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.common.http.AuthHeaderStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class AuthHeaderStrategyTest {

    // --- AuthHeaderStrategy.none() ---

    @Test
    void none_doesNotModifyHeaders() {
        // Arrange
        AuthHeaderStrategy strategy = AuthHeaderStrategy.none();
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        assertThat(headers.isEmpty()).isTrue();
    }

    @Test
    void none_returnsNonNullStrategy() {
        // Arrange & Act
        AuthHeaderStrategy strategy = AuthHeaderStrategy.none();

        // Assert
        assertThat(strategy).isNotNull();
    }

    @Test
    void none_canBeCalledMultipleTimes_withoutSideEffects() {
        // Arrange
        AuthHeaderStrategy strategy = AuthHeaderStrategy.none();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Existing", "value");

        // Act
        strategy.apply(headers);
        strategy.apply(headers);

        // Assert
        assertThat(headers.size()).isEqualTo(1);
        assertThat(headers.getFirst("X-Existing")).isEqualTo("value");
    }
}

