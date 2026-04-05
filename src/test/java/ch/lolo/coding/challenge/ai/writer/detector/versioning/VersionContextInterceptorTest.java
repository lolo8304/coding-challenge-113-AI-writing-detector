package ch.lolo.coding.challenge.ai.writer.detector.versioning;

import ch.lolo.common.versioning.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class VersionContextInterceptorTest {

    @Test
    void preHandle_buildsUpgradeAndDowngradeTransitions_fromHeaderDate() throws Exception {
        // Arrange
        VersionContextInterceptor interceptor = new VersionContextInterceptor(new VersionChainPlanner());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/contracts");
        request.addHeader(ApiVersion.VERSION_HEADER, "2024-03-01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        boolean proceed = interceptor.preHandle(request, response, handlerMethod());

        // Assert
        assertThat(proceed).isTrue();
        VersionContext context = VersionContextHolder.getRequired(request);
        assertThat(context.requestedVersion()).isEqualTo(ApiVersion.V2024_01_01);
        assertThat(context.requestUpgradeTransitions()).hasSize(3);
        assertThat(context.responseDowngradeTransitions()).hasSize(3);
        assertThat(context.requestUpgradeTransitions().get(0))
                .isEqualTo(new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01));
        assertThat(context.responseDowngradeTransitions().get(0))
                .isEqualTo(new VersionTransition(ApiVersion.V2026_04_01, ApiVersion.V2026_01_01));
    }

    @Test
    void preHandle_usesLatest_whenHeaderMissing() throws Exception {
        // Arrange
        VersionContextInterceptor interceptor = new VersionContextInterceptor(new VersionChainPlanner());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/contracts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        interceptor.preHandle(request, response, handlerMethod());

        // Assert
        VersionContext context = VersionContextHolder.getRequired(request);
        assertThat(context.requestedVersion()).isEqualTo(ApiVersion.latest());
        assertThat(context.requestUpgradeTransitions()).isEmpty();
        assertThat(context.responseDowngradeTransitions()).isEmpty();
    }

    @Test
    void preHandle_skipsVersionPlanning_whenHandlerIsNotControllerMethod() throws Exception {
        // Arrange
        VersionContextInterceptor interceptor = new VersionContextInterceptor(new VersionChainPlanner());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/rest/ai/detector/v1/contracts");
        request.addHeader(ApiVersion.VERSION_HEADER, "2024-03-01");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        boolean proceed = interceptor.preHandle(request, response, new Object());

        // Assert
        assertThat(proceed).isTrue();
        assertThat(request.getAttribute(VersionContextHolder.ATTR_VERSION_CONTEXT)).isNull();
    }

    private static HandlerMethod handlerMethod() throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod("endpoint");
        return new HandlerMethod(new TestController(), method);
    }

    private static class TestController {
        @SuppressWarnings("unused")
        public String endpoint() {
            return "ok";
        }
    }
}

