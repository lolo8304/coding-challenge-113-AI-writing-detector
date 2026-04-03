package ch.lolo.coding.challenge.ai.writer.detector.http;

import org.springframework.http.HttpHeaders;

public class BearerTokenAuthStrategy implements AuthHeaderStrategy {

    private final OAuthClientCredentialsTokenProvider tokenProvider;

    public BearerTokenAuthStrategy(OAuthClientCredentialsTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void apply(HttpHeaders headers) {
        headers.setBearerAuth(tokenProvider.getAccessToken());
    }
}

