package ch.lolo.coding.challenge.ai.writer.detector.http;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;

import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ContextHeaderAuthStrategy implements AuthHeaderStrategy {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final ObjectMapper objectMapper;
    private final ApplicationConfiguration.Context properties;

    public ContextHeaderAuthStrategy(ObjectMapper objectMapper, ApplicationConfiguration.Context properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void apply(HttpHeaders headers) {
        headers.set("x-axa-context", buildUnsignedJwt());
    }

    private String buildUnsignedJwt() {
        Instant now = Instant.now();

        Map<String, Object> jwtHeader = Map.of(
                "typ", "JWT",
                "alg", "none"
        );

        Map<String, Object> jwtPayload = new LinkedHashMap<>();
        jwtPayload.put("iss", properties.getIssuer());
        jwtPayload.put("sub", properties.getSubject());
        jwtPayload.put("client_id", properties.getClientId());
        jwtPayload.put("iat", now.getEpochSecond());
        jwtPayload.put("exp", now.plus(properties.getTtl()).getEpochSecond());
        jwtPayload.put("jti", UUID.randomUUID().toString());

        return encode(jwtHeader) + "." + encode(jwtPayload) + ".";
    }

    private String encode(Object value) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
            return BASE64_URL_ENCODER.encodeToString(jsonBytes);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not create unsigned OAuth context header", exception);
        }
    }
}
