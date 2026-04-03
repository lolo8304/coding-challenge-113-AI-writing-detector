package ch.lolo.coding.challenge.ai.writer.detector.http;

import org.springframework.http.HttpHeaders;

@FunctionalInterface
public interface AuthHeaderStrategy {

    void apply(HttpHeaders headers);

    static AuthHeaderStrategy none() {
        return headers -> {
        };
    }
}

