package ch.lolo.common.configuration;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.common.http.AuthHeaderStrategy;
import ch.lolo.common.http.ApiKeyAuthStrategy;
import ch.lolo.common.http.BearerTokenAuthStrategy;
import ch.lolo.common.http.ContextHeaderAuthStrategy;
import ch.lolo.common.http.HttpClient;
import ch.lolo.common.http.HttpClientFactory;
import ch.lolo.common.http.HttpClientLoggingSettings;
import ch.lolo.common.http.OAuthClientCredentialsTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Configuration
public class HttpClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfiguration.class);

    @Bean
    public HttpClientFactory httpClientFactory(ApplicationConfiguration applicationConfiguration,
                                               ObjectMapper objectMapper,
                                               Environment environment) {
        Map<String, HttpClientFactory.BackendIntegration> integrations = new LinkedHashMap<>();
        applicationConfiguration.getRest().forEach((name, integration) -> {
            validateLegacyApiKeyConfiguration(name, environment);
            ReloadableIntegrationRuntime runtime = new ReloadableIntegrationRuntime(
                    name,
                    integration.getUrl(),
                    integration.getHttp(),
                    objectMapper,
                    environment
            );
            integrations.put(name, new HttpClientFactory.BackendIntegration(
                    integration.getUrl(),
                    runtime::getRestClient,
                    runtime::getAuthHeaderStrategy,
                    runtime::refreshIfCertificateChanged,
                    toLoggingSettings(integration.getHttp().getLogging())
            ));
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

    private void validateLegacyApiKeyConfiguration(String integrationName, Environment environment) {
        String legacyProperty = "app.rest." + integrationName + ".http.api-key.enabled";
        if (environment.containsProperty(legacyProperty)) {
            throw new IllegalStateException("Legacy property " + legacyProperty + " is not supported. Use app.rest.<name>.http.oauth.mode=API_KEY only.");
        }
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
            case API_KEY -> createApiKeyStrategy(properties.getApiKey());
            case NONE -> AuthHeaderStrategy.none();
        };
    }

    private AuthHeaderStrategy createApiKeyStrategy(ApplicationConfiguration.ApiKey apiKey) {
        String headerName = apiKey.getHeaderName();
        if (headerName == null || headerName.isBlank()) {
            throw new IllegalStateException("API key header name must be configured when API key auth is enabled");
        }
        String value = apiKey.getValue();
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("API key value must be configured when API key auth is enabled");
        }
        return new ApiKeyAuthStrategy(headerName, value);
    }

    private HttpClientLoggingSettings toLoggingSettings(ApplicationConfiguration.Logging logging) {
        Set<String> configuredKeys = new LinkedHashSet<>();
        for (String key : logging.getSensitiveQueryParameters()) {
            if (key != null && !key.isBlank()) {
                configuredKeys.add(key.toLowerCase());
            }
        }

        return new HttpClientLoggingSettings(
                logging.isLogRequest(),
                logging.isLogResponse(),
                logging.isLogErrors(),
                logging.isShowSensitiveData(),
                configuredKeys
        );
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

    private String resolveKeyStoreLocation(ApplicationConfiguration.Tls tlsProperties, Environment environment) {
        return resolve(tlsProperties.getKeyStore(), environment.getProperty("server.ssl.key-store"));
    }

    private String getClientCertificateVersion(ApplicationConfiguration.Http properties, Environment environment) {
        String fingerprint = getClientCertificateFingerprint(properties, environment);

        if (!properties.getTls().isEnabled()) {
            return "tls-disabled|fp=" + fingerprint;
        }
        String keyStoreLocation = resolveKeyStoreLocation(properties.getTls(), environment);
        if (keyStoreLocation == null || keyStoreLocation.isBlank()) {
            return "tls-enabled-without-keystore-location|fp=" + fingerprint;
        }

        try {
            Path path = Path.of(keyStoreLocation);
            if (Files.exists(path)) {
                return path.toAbsolutePath() + "|" + Files.getLastModifiedTime(path).toMillis() + "|" + Files.size(path) + "|fp=" + fingerprint;
            }
        } catch (Exception ignored) {
            return "unreadable-keystore:" + keyStoreLocation + "|fp=" + fingerprint;
        }
        return "classpath-resource:" + keyStoreLocation + "|fp=" + fingerprint;
    }

    private String getClientCertificateFingerprint(ApplicationConfiguration.Http properties, Environment environment) {
        if (!properties.getTls().isEnabled()) {
            return "not-applicable";
        }

        try {
            String keyStoreLocation = resolve(properties.getTls().getKeyStore(), environment.getProperty("server.ssl.key-store"));
            String keyStorePassword = resolve(properties.getTls().getKeyStorePassword(), environment.getProperty("server.ssl.key-store-password"));
            String keyStoreType = resolve(properties.getTls().getKeyStoreType(), "PKCS12");
            String keyAlias = resolve(properties.getTls().getKeyAlias(), environment.getProperty("server.ssl.key-alias"));
            KeyStore keyStore = loadKeyStore(keyStoreType, keyStoreLocation, keyStorePassword);

            if (keyAlias == null || keyAlias.isBlank()) {
                throw new IllegalStateException("TLS key alias must be configured for certificate fingerprint detection");
            }

            Certificate certificate = keyStore.getCertificate(keyAlias);
            if (certificate == null) {
                throw new IllegalStateException("Could not find certificate for alias '" + keyAlias + "' in outbound keystore");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(certificate.getEncoded());
            return keyAlias + ":" + toHex(hash);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            return "fingerprint-unavailable";
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(String.format("%02x", value));
        }
        return hex.toString();
    }

    private class ReloadableIntegrationRuntime {
        private final String integrationName;
        private final String baseUrl;
        private final ApplicationConfiguration.Http properties;
        private final ObjectMapper objectMapper;
        private final Environment environment;

        private volatile RestClient restClient;
        private volatile AuthHeaderStrategy authHeaderStrategy;
        private volatile String certificateVersion;

        private ReloadableIntegrationRuntime(String integrationName,
                                            String baseUrl,
                                            ApplicationConfiguration.Http properties,
                                            ObjectMapper objectMapper,
                                            Environment environment) {
            this.integrationName = integrationName;
            this.baseUrl = baseUrl;
            this.properties = properties;
            this.objectMapper = objectMapper;
            this.environment = environment;
            this.certificateVersion = getClientCertificateVersion(properties, environment);
            rebuild(false);
        }

        private RestClient getRestClient() {
            return restClient;
        }

        private AuthHeaderStrategy getAuthHeaderStrategy() {
            return authHeaderStrategy;
        }

        private void refreshIfCertificateChanged() {
            String currentVersion = getClientCertificateVersion(properties, environment);
            if (Objects.equals(certificateVersion, currentVersion)) {
                return;
            }

            synchronized (this) {
                currentVersion = getClientCertificateVersion(properties, environment);
                if (Objects.equals(certificateVersion, currentVersion)) {
                    return;
                }
                certificateVersion = currentVersion;
                rebuild(true);
            }
        }

        private void rebuild(boolean logReload) {
            ClientHttpRequestFactory requestFactory = createRequestFactory(properties, environment);
            this.authHeaderStrategy = createAuthHeaderStrategy(properties, objectMapper, requestFactory);
            this.restClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .requestFactory(requestFactory)
                    .build();

            if (logReload) {
                LOGGER.info("[{}] Outbound client certificate change detected. Reloaded HTTP client (version={})",
                        integrationName,
                        certificateVersion);
            }
        }
    }
}
