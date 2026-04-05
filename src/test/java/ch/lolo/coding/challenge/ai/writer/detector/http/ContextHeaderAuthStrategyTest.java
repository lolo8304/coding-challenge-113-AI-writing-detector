package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.common.http.ContextHeaderAuthStrategy;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ContextHeaderAuthStrategyTest {

    private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();

    private ObjectMapper objectMapper;
    private ApplicationConfiguration.Context contextProperties;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        contextProperties = new ApplicationConfiguration.Context();
        contextProperties.setIssuer("test-issuer");
        contextProperties.setSubject("test-subject");
        contextProperties.setClientId("test-client-id");
        contextProperties.setTtl(Duration.ofMinutes(5));
    }

    @Test
    void apply_setsXAxaContextHeader() {
        // Arrange
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        assertThat(headers.getFirst("x-axa-context")).isNotNull();
    }

    @Test
    void apply_headerContainsThreeParts_unsignedJwtFormat() {
        // Arrange
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        String token = headers.getFirst("x-axa-context");
        assertThat(token).isNotNull();
        String[] parts = token.split("\\.");
        // unsigned JWT: header.payload. (trailing dot → 3 parts when splitting with limit, or 2 without empty trailing)
        assertThat(token).endsWith(".");
        assertThat(parts).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void apply_headerPayloadContainsExpectedClaims() throws Exception {
        // Arrange
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        String token = headers.getFirst("x-axa-context");
        assertThat(token).isNotNull();
        String[] parts = token.split("\\.");
        String payloadJson = new String(BASE64_DECODER.decode(parts[1]));
        var payload = objectMapper.readTree(payloadJson);

        assertThat(payload.get("iss").asString()).isEqualTo("test-issuer");
        assertThat(payload.get("sub").asString()).isEqualTo("test-subject");
        assertThat(payload.get("client_id").asString()).isEqualTo("test-client-id");
        assertThat(payload.has("iat")).isTrue();
        assertThat(payload.has("exp")).isTrue();
        assertThat(payload.has("jti")).isTrue();
    }

    @Test
    void apply_jwtHeaderHasAlgNone() throws Exception {
        // Arrange
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        String token = headers.getFirst("x-axa-context");
        assertThat(token).isNotNull();
        String[] parts = token.split("\\.");
        String headerJson = new String(BASE64_DECODER.decode(parts[0]));
        var jwtHeader = objectMapper.readTree(headerJson);

        assertThat(jwtHeader.get("alg").asString()).isEqualTo("none");
        assertThat(jwtHeader.get("typ").asString()).isEqualTo("JWT");
    }

    @Test
    void apply_expIsAfterIat() throws Exception {
        // Arrange
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers = new HttpHeaders();

        // Act
        strategy.apply(headers);

        // Assert
        String token = headers.getFirst("x-axa-context");
        assertThat(token).isNotNull();
        String[] parts = token.split("\\.");
        String payloadJson = new String(BASE64_DECODER.decode(parts[1]));
        var payload = objectMapper.readTree(payloadJson);

        long iat = payload.get("iat").asLong();
        long exp = payload.get("exp").asLong();
        assertThat(exp).isGreaterThan(iat);
    }

    @Test
    void apply_expReflectsTtl() throws Exception {
        // Arrange
        contextProperties.setTtl(Duration.ofMinutes(10));
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers = new HttpHeaders();
        Instant beforeCall = Instant.now();

        // Act
        strategy.apply(headers);

        // Assert
        String token = headers.getFirst("x-axa-context");
        assertThat(token).isNotNull();
        String[] parts = token.split("\\.");
        String payloadJson = new String(BASE64_DECODER.decode(parts[1]));
        var payload = objectMapper.readTree(payloadJson);

        long iat = payload.get("iat").asLong();
        long exp = payload.get("exp").asLong();
        long expectedTtlSeconds = Duration.ofMinutes(10).toSeconds();
        assertThat(exp - iat).isCloseTo(expectedTtlSeconds, org.assertj.core.data.Offset.offset(2L));
    }

    @Test
    void apply_eachCallGeneratesUniqueJti() throws Exception {
        // Arrange
        ContextHeaderAuthStrategy strategy = new ContextHeaderAuthStrategy(objectMapper, contextProperties);
        HttpHeaders headers1 = new HttpHeaders();
        HttpHeaders headers2 = new HttpHeaders();

        // Act
        strategy.apply(headers1);
        strategy.apply(headers2);

        // Assert
        String token1 = headers1.getFirst("x-axa-context");
        String token2 = headers2.getFirst("x-axa-context");
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();

        String[] parts1 = token1.split("\\.");
        String[] parts2 = token2.split("\\.");
        String payload1 = new String(BASE64_DECODER.decode(parts1[1]));
        String payload2 = new String(BASE64_DECODER.decode(parts2[1]));

        var jti1 = objectMapper.readTree(payload1).get("jti").asString();
        var jti2 = objectMapper.readTree(payload2).get("jti").asString();
        assertThat(jti1).isNotEqualTo(jti2);
    }
}

