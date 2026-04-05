package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import ch.lolo.common.configuration.HttpClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import tools.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpClientConfigurationTest {

    @Test
    void httpClientFactory_failsFastWhenLegacyApiKeyEnabledPropertyIsPresent() {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        ApplicationConfiguration.RestIntegration integration = new ApplicationConfiguration.RestIntegration();
        integration.setUrl("https://example.com");
        applicationConfiguration.getRest().put("hello-world", integration);

        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.rest.hello-world.http.api-key.enabled", "false");

        HttpClientConfiguration configuration = new HttpClientConfiguration();

        assertThatThrownBy(() -> configuration.httpClientFactory(applicationConfiguration, new ObjectMapper(), environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.rest.hello-world.http.api-key.enabled");
    }

    @Test
    void httpClientFactory_throwsWhenConfiguredKeyAliasDoesNotExistInKeystore() throws Exception {
        Path keystorePath = Files.createTempFile("outbound-client", ".p12");
        String password = "changeit";
        createEmptyPkcs12Keystore(keystorePath, password);

        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        ApplicationConfiguration.RestIntegration integration = new ApplicationConfiguration.RestIntegration();
        integration.setUrl("https://example.com");
        integration.getHttp().getTls().setEnabled(true);
        integration.getHttp().getTls().setKeyStore(keystorePath.toString());
        integration.getHttp().getTls().setKeyStorePassword(password);
        integration.getHttp().getTls().setKeyAlias("missing-alias");
        applicationConfiguration.getRest().put("hello-world", integration);

        HttpClientConfiguration configuration = new HttpClientConfiguration();

        try {
            assertThatThrownBy(() -> configuration.httpClientFactory(applicationConfiguration, new ObjectMapper(), new MockEnvironment()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("missing-alias");
        } finally {
            Files.deleteIfExists(keystorePath);
        }
    }

    private void createEmptyPkcs12Keystore(Path path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, password.toCharArray());
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            keyStore.store(outputStream, password.toCharArray());
        }
    }
}

