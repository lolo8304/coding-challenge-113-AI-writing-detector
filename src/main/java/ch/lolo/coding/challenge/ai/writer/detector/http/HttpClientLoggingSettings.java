package ch.lolo.coding.challenge.ai.writer.detector.http;

import java.util.Set;

public record HttpClientLoggingSettings(boolean logRequest,
                                        boolean logResponse,
                                        boolean logErrors,
                                        boolean showSensitiveData,
                                        Set<String> sensitiveQueryParameters) {

    public static HttpClientLoggingSettings disabled() {
        return new HttpClientLoggingSettings(false, false, false, false, Set.of());
    }
}

