package ch.lolo.common.http;

import org.springframework.http.HttpHeaders;

public class ApiKeyAuthStrategy implements AuthHeaderStrategy {

    private final String headerName;
    private final String apiKey;

    public ApiKeyAuthStrategy(String headerName, String apiKey) {
        this.headerName = headerName;
        this.apiKey = apiKey;
    }

    @Override
    public void apply(HttpHeaders headers) {
        headers.set(headerName, apiKey);
    }
}

