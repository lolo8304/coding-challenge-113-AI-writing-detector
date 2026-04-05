package ch.lolo.coding.challenge.ai.writer.detector.versioning.contract.request;

import ch.lolo.coding.challenge.ai.writer.detector.contracts.model.Contract;
import ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.request.*;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.ApiVersion;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionContext;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionContextHolder;
import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionTransition;
import ch.lolo.coding.challenge.ai.writer.detector.contracts.versioning.aggregate.PremiumsAggregateTransformer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContractRequestVersioningAdviceTest {

    @Test
    void beforeBodyRead_upgradesLegacyContractJson_toLatestVersion() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractRequestUpgradeFactory factory = new ContractRequestUpgradeFactory(List.of(
                new ContractRequestUpgrade2024To2025(premiums),
                new ContractRequestUpgrade2025To2026(premiums)
        ));
        ContractRequestUpgradeProcess process = new ContractRequestUpgradeProcess(factory);
        ContractRequestVersioningAdvice advice = new ContractRequestVersioningAdvice(objectMapper, process);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/contracts");
        request.setContentType("application/json");
        request.setContent("{\"name\":\"My contract 42\",\"premium\":123.45}".getBytes());
        VersionContextHolder.set(request, new VersionContext(
                ApiVersion.V2024_01_01,
                List.of(
                        new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01),
                        new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2026_01_01)
                ),
                List.of()
        ));

        // Act
        HttpInputMessage transformed = advice.beforeBodyRead(
                new ServletServerHttpRequest(request),
                contractParameter(),
                Contract.class,
                MappingJackson2HttpMessageConverter.class
        );

        // Assert
        ObjectNode rewritten = objectMapper.readValue(transformed.getBody(), ObjectNode.class);
        assertThat(rewritten.get("name").asText()).isEqualTo("My contract 42");
        assertThat(rewritten.get("premiums").get("amount").asDouble()).isEqualTo(123.45d);
        assertThat(rewritten.get("premiums").get("currency").asText()).isEqualTo("CHF");
        assertThat(rewritten.has("premium")).isFalse();
    }

    @Test
    void beforeBodyRead_keepsBodyUnchanged_forNonContractsPath() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractRequestUpgradeFactory factory = new ContractRequestUpgradeFactory(List.of(
                new ContractRequestUpgrade2024To2025(premiums),
                new ContractRequestUpgrade2025To2026(premiums)
        ));
        ContractRequestVersioningAdvice advice =
                new ContractRequestVersioningAdvice(objectMapper, new ContractRequestUpgradeProcess(factory));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/hello-world");
        request.setContentType("application/json");
        request.setContent("{\"name\":\"My contract 42\",\"premium\":123.45}".getBytes());
        ServletServerHttpRequest input = new ServletServerHttpRequest(request);

        // Act
        HttpInputMessage transformed = advice.beforeBodyRead(
                input,
                contractParameter(),
                Contract.class,
                MappingJackson2HttpMessageConverter.class
        );

        // Assert
        assertThat(transformed).isSameAs(input);
    }

    @Test
    void beforeBodyRead_upgradesWrappedHttpInputMessage_usingRequestContextFallback() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractRequestUpgradeFactory factory = new ContractRequestUpgradeFactory(List.of(
                new ContractRequestUpgrade2024To2025(premiums),
                new ContractRequestUpgrade2025To2026(premiums)
        ));
        ContractRequestVersioningAdvice advice =
                new ContractRequestVersioningAdvice(objectMapper, new ContractRequestUpgradeProcess(factory));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/contracts");
        request.setContentType("application/json");
        request.setContent("{\"name\":\"My contract 42\",\"premium\":123.45}".getBytes());
        VersionContextHolder.set(request, new VersionContext(
                ApiVersion.V2024_01_01,
                List.of(
                        new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01),
                        new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2026_01_01)
                ),
                List.of()
        ));

        ServletServerHttpRequest servletInput = new ServletServerHttpRequest(request);
        HttpInputMessage wrappedInput = new HttpInputMessage() {
            @Override
            public java.io.InputStream getBody() throws java.io.IOException {
                return servletInput.getBody();
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return servletInput.getHeaders();
            }
        };

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            // Act
            HttpInputMessage transformed = advice.beforeBodyRead(
                    wrappedInput,
                    contractParameter(),
                    Contract.class,
                    MappingJackson2HttpMessageConverter.class
            );

            // Assert
            ObjectNode rewritten = objectMapper.readValue(transformed.getBody(), ObjectNode.class);
            assertThat(rewritten.get("name").asText()).isEqualTo("My contract 42");
            assertThat(rewritten.get("premiums").get("amount").asDouble()).isEqualTo(123.45d);
            assertThat(rewritten.get("premiums").get("currency").asText()).isEqualTo("CHF");
            assertThat(rewritten.has("premium")).isFalse();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void supports_returnsTrue_onlyForContractParameter() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractRequestUpgradeFactory factory = new ContractRequestUpgradeFactory(List.of(
                new ContractRequestUpgrade2024To2025(premiums),
                new ContractRequestUpgrade2025To2026(premiums)
        ));
        ContractRequestVersioningAdvice advice =
                new ContractRequestVersioningAdvice(objectMapper, new ContractRequestUpgradeProcess(factory));

        // Act
        boolean contractSupported = advice.supports(
                contractParameter(),
                Contract.class,
                MappingJackson2HttpMessageConverter.class
        );
        boolean stringSupported = advice.supports(
                stringParameter(),
                String.class,
                MappingJackson2HttpMessageConverter.class
        );

        // Assert
        assertThat(contractSupported).isTrue();
        assertThat(stringSupported).isFalse();
    }

    private static MethodParameter contractParameter() throws NoSuchMethodException {
        Method method = RequestController.class.getDeclaredMethod("endpoint", Contract.class);
        return MethodParameter.forExecutable(method, 0);
    }

    private static MethodParameter stringParameter() throws NoSuchMethodException {
        Method method = RequestController.class.getDeclaredMethod("plain", String.class);
        return MethodParameter.forExecutable(method, 0);
    }

    private static class RequestController {
        @SuppressWarnings("unused")
        public Contract endpoint(Contract contract) {
            return contract;
        }

        @SuppressWarnings("unused")
        public String plain(String input) {
            return input;
        }
    }
}

