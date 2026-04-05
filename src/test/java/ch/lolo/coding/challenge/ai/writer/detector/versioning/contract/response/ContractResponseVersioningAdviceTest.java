package ch.lolo.coding.challenge.ai.writer.detector.versioning.contract.response;

import ch.lolo.coding.challenge.ai.writer.detector.model.Amount;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.Contract;
import ch.lolo.coding.challenge.ai.writer.detector.model.Currency;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.response.*;
import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionContext;
import ch.lolo.common.versioning.VersionContextHolder;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.versioning.aggregate.PremiumsAggregateTransformer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContractResponseVersioningAdviceTest {

    @Test
    void beforeBodyWrite_downgradesLatestContract_toRequestedLegacyVersion() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractResponseDowngradeFactory factory = new ContractResponseDowngradeFactory(List.of(
                new ContractResponseDowngrade2026To2025(),
                new ContractResponseDowngrade2025To2024(premiums)
        ));
        ContractResponseDowngradeProcess process = new ContractResponseDowngradeProcess(factory);
        ContractResponseVersioningAdvice advice = new ContractResponseVersioningAdvice(objectMapper, process);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/contracts");
        VersionContextHolder.set(request, new VersionContext(
                ApiVersion.V2024_01_01,
                List.of(),
                List.of(
                        new VersionTransition(ApiVersion.V2026_01_01, ApiVersion.V2025_01_01),
                        new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2024_01_01)
                )
        ));

        Contract latest = Contract.builder()
                .name("My contract 42")
                .premium(Amount.builder().amount(new BigDecimal("123.45")).currency(Currency.CHF).build())
                .build();

        // Act
        Object body = advice.beforeBodyWrite(
                latest,
                returnType(),
                MediaType.APPLICATION_JSON,
                JacksonJsonHttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        // Assert
        assertThat(body).isInstanceOf(ObjectNode.class);
        ObjectNode rewritten = (ObjectNode) body;
        assertThat(rewritten.get("name").asString()).isEqualTo("My contract 42");
        assertThat(rewritten.has("customerName")).isFalse();
        assertThat(rewritten.has("id")).isFalse();
    }

    @Test
    void beforeBodyWrite_returnsOriginalBody_forNonContractsPath() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractResponseDowngradeFactory factory = new ContractResponseDowngradeFactory(List.of(
                new ContractResponseDowngrade2026To2025(),
                new ContractResponseDowngrade2025To2024(premiums)
        ));
        ContractResponseVersioningAdvice advice =
                new ContractResponseVersioningAdvice(objectMapper, new ContractResponseDowngradeProcess(factory));

        Contract latest = Contract.builder()
                .name("My contract 42")
                .premium(Amount.builder().amount(new BigDecimal("123.45")).currency(Currency.CHF).build())
                .build();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/hello-world");

        // Act
        Object body = advice.beforeBodyWrite(
                latest,
                returnType(),
                MediaType.APPLICATION_JSON,
                JacksonJsonHttpMessageConverter.class,
                new ServletServerHttpRequest(request),
                new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        // Assert
        assertThat(body).isSameAs(latest);
    }

    @Test
    void supports_returnsTrue_onlyForContractReturnType() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        PremiumsAggregateTransformer premiums = new PremiumsAggregateTransformer();
        ContractResponseDowngradeFactory factory = new ContractResponseDowngradeFactory(List.of(
                new ContractResponseDowngrade2026To2025(),
                new ContractResponseDowngrade2025To2024(premiums)
        ));
        ContractResponseVersioningAdvice advice =
                new ContractResponseVersioningAdvice(objectMapper, new ContractResponseDowngradeProcess(factory));

        // Act
        boolean contractSupported = advice.supports(returnType(), JacksonJsonHttpMessageConverter.class);
        boolean stringSupported = advice.supports(stringReturnType(), JacksonJsonHttpMessageConverter.class);

        // Assert
        assertThat(contractSupported).isTrue();
        assertThat(stringSupported).isFalse();
    }

    private static MethodParameter returnType() throws NoSuchMethodException {
        Method method = ResponseController.class.getDeclaredMethod("endpoint");
        return MethodParameter.forExecutable(method, -1);
    }

    private static MethodParameter stringReturnType() throws NoSuchMethodException {
        Method method = ResponseController.class.getDeclaredMethod("plain");
        return MethodParameter.forExecutable(method, -1);
    }

    private static class ResponseController {
        @SuppressWarnings("unused")
        public Contract endpoint() {
            return null;
        }

        @SuppressWarnings("unused")
        public String plain() {
            return "ok";
        }
    }
}

