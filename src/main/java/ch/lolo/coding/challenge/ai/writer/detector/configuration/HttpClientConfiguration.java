package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import ch.lolo.coding.challenge.ai.writer.detector.http.AuthHeaderStrategy;
import ch.lolo.coding.challenge.ai.writer.detector.http.BearerTokenAuthStrategy;
import ch.lolo.coding.challenge.ai.writer.detector.http.ContextHeaderAuthStrategy;
import ch.lolo.coding.challenge.ai.writer.detector.http.HttpClient;
import ch.lolo.coding.challenge.ai.writer.detector.http.HttpClientFactory;
import ch.lolo.coding.challenge.ai.writer.detector.http.OAuthClientCredentialsTokenProvider;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public HttpClientFactory httpClientFactory(ApplicationConfiguration applicationConfiguration,
                                               ObjectMapper objectMapper,
                                               Environment environment) {
        Map<String, HttpClientFactory.BackendIntegration> integrations = new LinkedHashMap<>();
        applicationConfiguration.getRest().forEach((name, integration) -> {
            ClientHttpRequestFactory requestFactory = createRequestFactory(integration.getHttp(), environment);
            AuthHeaderStrategy authHeaderStrategy = createAuthHeaderStrategy(integration.getHttp(), objectMapper, requestFactory);
            integrations.put(name, new HttpClientFactory.BackendIntegration(integration.getUrl(), requestFactory, authHeaderStrategy));
        });
        return new HttpClientFactory(integrations);
    }

    @Bean
    public HttpClient httpClient(HttpClientFactory httpClientFactory,
                                 ApplicationConfiguration applicationConfiguration) {
        return httpClientFactory.create(applicationConfiguration.getHelloWorldIntegration());
    }

    private ClientHttpRequestFactory createRequestFactory(ApplicationConfiguration.Http properties,
                                                          Environment environment) {
        java.net.http.HttpClient.Builder builder = java.net.http.HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .sslContext(createSslContext(properties.getTls(), environment));

        if (properties.getProxy().isEnabled()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(
                    properties.getProxy().getHost(),
                    properties.getProxy().getPort())));
        }

        java.net.http.HttpClient jdkClient = builder.build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return requestFactory;
    }

    private AuthHeaderStrategy createAuthHeaderStrategy(ApplicationConfiguration.Http properties,
                                                        ObjectMapper objectMapper,
                                                        ClientHttpRequestFactory requestFactory) {
        return switch (properties.getOauth().getMode()) {
            case CONTEXT -> new ContextHeaderAuthStrategy(objectMapper, properties.getOauth().getContext());
            case BEARER -> {
                RestClient oauthRestClient = RestClient.builder()
                        .requestFactory(requestFactory)
                        .build();
                OAuthClientCredentialsTokenProvider tokenProvider =
                        new OAuthClientCredentialsTokenProvider(oauthRestClient, properties.getOauth().getClientCredentials());
                yield new BearerTokenAuthStrategy(tokenProvider);
            }
            case NONE -> AuthHeaderStrategy.none();
        };
    }

    private SSLContext createSslContext(ApplicationConfiguration.Tls tlsProperties, Environment environment) {
        try {
            if (!tlsProperties.isSslVerification()) {
                return createTrustAllSslContext();
            }

            if (!tlsProperties.isEnabled()) {
                return SSLContext.getDefault();
            }

            String keyStoreLocation = resolve(
                    tlsProperties.getKeyStore(),
                    environment.getProperty("server.ssl.key-store"));
            String keyStorePassword = resolve(
                    tlsProperties.getKeyStorePassword(),
                    environment.getProperty("server.ssl.key-store-password"));
            String keyStoreType = resolve(
                    tlsProperties.getKeyStoreType(),
                    "PKCS12");

            String trustStoreLocation = resolve(
                    tlsProperties.getTrustStore(),
                    environment.getProperty("server.ssl.trust-store"));
            String trustStorePassword = resolve(
                    tlsProperties.getTrustStorePassword(),
                    environment.getProperty("server.ssl.trust-store-password"));
            String trustStoreType = resolve(
                    tlsProperties.getTrustStoreType(),
                    "PKCS12");

            KeyStore keyStore = loadKeyStore(keyStoreType, keyStoreLocation, keyStorePassword);
            KeyStore trustStore = loadKeyStore(trustStoreType, trustStoreLocation, trustStorePassword);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize outbound SSL context", exception);
        }
    }

    private KeyStore loadKeyStore(String storeType, String location, String password) throws Exception {
        if (location == null || password == null) {
            throw new IllegalStateException("Keystore/Truststore location and password must be configured when app.rest.<name>.http.tls.enabled=true");
        }

        KeyStore keyStore = KeyStore.getInstance(storeType);
        try (InputStream inputStream = openStream(location)) {
            keyStore.load(inputStream, password.toCharArray());
        }
        return keyStore;
    }

    private InputStream openStream(String location) throws Exception {
        Path filePath = Path.of(location);
        if (Files.exists(filePath)) {
            return Files.newInputStream(filePath);
        }
        return Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(location),
                "Could not resolve keystore/truststore at location: " + location);
    }

    private SSLContext createTrustAllSslContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    private String resolve(String configuredValue, String fallback) {
        if (configuredValue != null && !configuredValue.isBlank()) {
            return configuredValue;
        }
        return fallback;
    }
}
