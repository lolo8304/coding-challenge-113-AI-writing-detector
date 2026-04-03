package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.http")
public class OutboundHttpClientProperties {

    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);
    private final Tls tls = new Tls();
    private final Proxy proxy = new Proxy();
    private final OAuth oauth = new OAuth();

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Tls getTls() {
        return tls;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public static class Tls {
        private boolean enabled;
        private boolean sslVerification = true;
        private String keyStore;
        private String keyStorePassword;
        private String keyStoreType = "PKCS12";
        private String trustStore;
        private String trustStorePassword;
        private String trustStoreType = "PKCS12";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isSslVerification() {
            return sslVerification;
        }

        public void setSslVerification(boolean sslVerification) {
            this.sslVerification = sslVerification;
        }

        public String getKeyStore() {
            return keyStore;
        }

        public void setKeyStore(String keyStore) {
            this.keyStore = keyStore;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public String getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(String trustStore) {
            this.trustStore = trustStore;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public String getTrustStoreType() {
            return trustStoreType;
        }

        public void setTrustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }
    }

    public static class Proxy {
        private boolean enabled;
        private String host;
        private int port;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class OAuth {
        private OAuthMode mode = OAuthMode.NONE;
        private final Context context = new Context();
        private final ClientCredentials clientCredentials = new ClientCredentials();

        public OAuthMode getMode() {
            return mode;
        }

        public void setMode(OAuthMode mode) {
            this.mode = mode;
        }

        public Context getContext() {
            return context;
        }

        public ClientCredentials getClientCredentials() {
            return clientCredentials;
        }
    }

    public enum OAuthMode {
        NONE,
        CONTEXT,
        BEARER
    }

    public static class Context {
        private String issuer = "login.axa.ch";
        private String subject = "default-subject";
        private String clientId = "default-client-id";
        private Duration ttl = Duration.ofMinutes(5);

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class ClientCredentials {
        private String tokenUrl = "https://login.axa.ch/oauth/token";
        private String clientId;
        private String clientSecret;
        private String scope;

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}

