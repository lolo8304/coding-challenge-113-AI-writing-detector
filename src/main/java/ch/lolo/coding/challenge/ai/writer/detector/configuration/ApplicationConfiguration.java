package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
public class ApplicationConfiguration {

	private int timeout = 5000;
	private String helloWorldIntegration = "default";
	private final Map<String, RestIntegration> rest = new LinkedHashMap<>();

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getHelloWorldIntegration() {
		return helloWorldIntegration;
	}

	public void setHelloWorldIntegration(String helloWorldIntegration) {
		this.helloWorldIntegration = helloWorldIntegration;
	}

	public Map<String, RestIntegration> getRest() {
		return rest;
	}

	public RestIntegration getRequiredIntegration(String name) {
		RestIntegration integration = rest.get(name);
		if (integration == null) {
			throw new IllegalStateException("Missing app.rest." + name + " configuration");
		}
		return integration;
	}

	public static class RestIntegration {
		private String url;
		private final Map<String, String> endpoints = new LinkedHashMap<>();
		private final Http http = new Http();

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Map<String, String> getEndpoints() {
			return endpoints;
		}

		public Http getHttp() {
			return http;
		}

		public String getRequiredEndpoint(String endpointName) {
			String endpoint = endpoints.get(endpointName);
			if (endpoint == null || endpoint.isBlank()) {
				throw new IllegalStateException("Missing app.rest.<name>.endpoints." + endpointName + " configuration");
			}
			return endpoint;
		}
	}

	public static class Http {
		private Duration connectTimeout = Duration.ofSeconds(5);
		private Duration readTimeout = Duration.ofSeconds(30);
		private final Tls tls = new Tls();
		private final Proxy proxy = new Proxy();
		private final OAuth oauth = new OAuth();
		private final ApiKey apiKey = new ApiKey();
		private final Logging logging = new Logging();

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

		public ApiKey getApiKey() {
			return apiKey;
		}

		public Logging getLogging() {
			return logging;
		}
	}

	public static class Tls {
		private boolean enabled;
		private boolean sslVerification = true;
		private String keyStore;
		private String keyStorePassword;
		private String keyStoreType = "PKCS12";
		private String keyAlias;
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

		public String getKeyAlias() {
			return keyAlias;
		}

		public void setKeyAlias(String keyAlias) {
			this.keyAlias = keyAlias;
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
		BEARER,
		API_KEY
	}

	public static class ApiKey {
		private boolean enabled;
		private String headerName = "x-api-key";
		private String value;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getHeaderName() {
			return headerName;
		}

		public void setHeaderName(String headerName) {
			this.headerName = headerName;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class Logging {
		private boolean logRequest;
		private boolean logResponse;
		private boolean logErrors;
		private boolean showSensitiveData;
		private final List<String> sensitiveQueryParameters = new ArrayList<>();

		public boolean isLogRequest() {
			return logRequest;
		}

		public void setLogRequest(boolean logRequest) {
			this.logRequest = logRequest;
		}

		public boolean isLogResponse() {
			return logResponse;
		}

		public void setLogResponse(boolean logResponse) {
			this.logResponse = logResponse;
		}

		public boolean isLogErrors() {
			return logErrors;
		}

		public void setLogErrors(boolean logErrors) {
			this.logErrors = logErrors;
		}

		public boolean isShowSensitiveData() {
			return showSensitiveData;
		}

		public void setShowSensitiveData(boolean showSensitiveData) {
			this.showSensitiveData = showSensitiveData;
		}

		public List<String> getSensitiveQueryParameters() {
			return sensitiveQueryParameters;
		}
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
