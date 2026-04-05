package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "app")
public class ApplicationConfiguration {

    @Setter
    private int timeout = 5000;
    @Setter
    private String helloWorldIntegration = "default";
    private final Logging logging = new Logging();
    private final Map<String, RestIntegration> rest = new LinkedHashMap<>();

    public RestIntegration getRequiredIntegration(String name) {
        RestIntegration integration = rest.get(name);
        if (integration == null) {
            throw new IllegalStateException("Missing app.rest." + name + " configuration");
        }
        return integration;
    }

    @Getter
    @Setter
    public static class RestIntegration {
        private String url;
        private final Map<String, String> endpoints = new LinkedHashMap<>();
        private final Http http = new Http();

        public String getRequiredEndpoint(String endpointName) {
            String endpoint = endpoints.get(endpointName);
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("Missing app.rest.<name>.endpoints." + endpointName + " configuration");
            }
            return endpoint;
        }
    }

    @Getter
    @Setter
    public static class Http {
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration readTimeout = Duration.ofSeconds(30);
        private final Tls tls = new Tls();
        private final Proxy proxy = new Proxy();
        private final OAuth oauth = new OAuth();
        private final ApiKey apiKey = new ApiKey();
        private final Logging logging = new Logging();

    }

    @Getter
    @Setter
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

    }

	@Getter
	@Setter
	public static class Proxy {
        private boolean enabled;
        private String host;
        private int port;
    }

	@Getter
	@Setter
	public static class OAuth {
        private OAuthMode mode = OAuthMode.NONE;
        private final Context context = new Context();
        private final ClientCredentials clientCredentials = new ClientCredentials();
    }

    public enum OAuthMode {
        NONE,
        CONTEXT,
        BEARER,
        API_KEY
    }

	@Getter
	@Setter
	public static class ApiKey {
        private boolean enabled;
        private String headerName = "x-api-key";
        private String value;
    }

	@Getter
	@Setter
	public static class Logging {
        private boolean logRequest;
        private boolean logResponse;
        private boolean logErrors;
        private boolean showSensitiveData;
        private final List<String> sensitiveQueryParameters = new ArrayList<>();
    }

	@Getter
	@Setter
	public static class Context {
        private String issuer = "login.axa.ch";
        private String subject = "default-subject";
        private String clientId = "default-client-id";
        private Duration ttl = Duration.ofMinutes(5);

    }

	@Getter
	@Setter
	public static class ClientCredentials {
        private String tokenUrl = "https://login.axa.ch/oauth/token";
        private String clientId;
        private String clientSecret;
        private String scope;
    }

}
