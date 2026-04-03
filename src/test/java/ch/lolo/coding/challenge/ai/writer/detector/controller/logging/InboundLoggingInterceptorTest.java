package ch.lolo.coding.challenge.ai.writer.detector.controller.logging;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InboundLoggingInterceptorTest {

    @Test
    void preHandleAndAfterCompletion_logsRequestAndResponse_andMasksSensitiveQueryValues() throws Exception {
        ApplicationConfiguration.Logging logging = new ApplicationConfiguration.Logging();
        logging.setLogRequest(true);
        logging.setLogResponse(true);
        logging.setLogErrors(true);
        logging.setShowSensitiveData(false);
        logging.getSensitiveQueryParameters().add("client_assertion");

        InboundLoggingInterceptor interceptor = new InboundLoggingInterceptor(logging);
        ListAppender<ILoggingEvent> appender = attachAppender();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/ai/detector/v1/hello-world");
        request.setQueryString("client_assertion=jwt&name=ok");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        HandlerMethod handler = handlerMethod();

        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, null);

        List<ILoggingEvent> events = appender.list;
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(events.get(0).getFormattedMessage()).contains("client_assertion=***").contains("name=ok");
        assertThat(events.get(1).getLevel()).isEqualTo(Level.INFO);
        assertThat(events.get(1).getFormattedMessage()).contains("status=200");

        detachAppender(appender);
    }

    @Test
    void monitoringHeaderTrue_suppressesRequestAndResponseLogs() throws Exception {
        ApplicationConfiguration.Logging logging = new ApplicationConfiguration.Logging();
        logging.setLogRequest(true);
        logging.setLogResponse(true);
        logging.setLogErrors(true);

        InboundLoggingInterceptor interceptor = new InboundLoggingInterceptor(logging);
        ListAppender<ILoggingEvent> appender = attachAppender();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/ai/detector/v1/hello-world");
        request.addHeader("x-axa-monitoring", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        HandlerMethod handler = handlerMethod();

        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, null);

        assertThat(appender.list).isEmpty();

        detachAppender(appender);
    }

    @Test
    void monitoringHeaderTrue_stillLogsErrors() throws Exception {
        ApplicationConfiguration.Logging logging = new ApplicationConfiguration.Logging();
        logging.setLogRequest(true);
        logging.setLogResponse(true);
        logging.setLogErrors(true);

        InboundLoggingInterceptor interceptor = new InboundLoggingInterceptor(logging);
        ListAppender<ILoggingEvent> appender = attachAppender();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/ai/detector/v1/hello-world");
        request.addHeader("x-axa-monitoring", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        HandlerMethod handler = handlerMethod();

        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, new RuntimeException("boom"));

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(appender.list.get(0).getFormattedMessage()).contains("ERROR").contains("boom");

        detachAppender(appender);
    }

    @Test
    void classLevelOverride_disablesRequestAndResponseEvenWhenAppDefaultsAreTrue() throws Exception {
        ApplicationConfiguration.Logging logging = new ApplicationConfiguration.Logging();
        logging.setLogRequest(true);
        logging.setLogResponse(true);
        logging.setLogErrors(true);

        InboundLoggingInterceptor interceptor = new InboundLoggingInterceptor(logging);
        ListAppender<ILoggingEvent> appender = attachAppender();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/ai/detector/v1/hello-world");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        HandlerMethod handler = classOverrideHandlerMethod("classOnly");

        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, null);

        assertThat(appender.list).isEmpty();

        detachAppender(appender);
    }

    @Test
    void methodLevelOverride_hasPriorityOverClassLevel() throws Exception {
        ApplicationConfiguration.Logging logging = new ApplicationConfiguration.Logging();
        logging.setLogRequest(false);
        logging.setLogResponse(false);
        logging.setLogErrors(false);

        InboundLoggingInterceptor interceptor = new InboundLoggingInterceptor(logging);
        ListAppender<ILoggingEvent> appender = attachAppender();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/ai/detector/v1/hello-world");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        HandlerMethod handler = classOverrideHandlerMethod("methodOverride");

        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, null);

        assertThat(appender.list).hasSize(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains("[INBOUND]");
        assertThat(appender.list.get(1).getFormattedMessage()).contains("COMPLETED");

        detachAppender(appender);
    }

    private static HandlerMethod handlerMethod() throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod("endpoint");
        return new HandlerMethod(new TestController(), method);
    }

    private static HandlerMethod classOverrideHandlerMethod(String methodName) throws NoSuchMethodException {
        Method method = ClassOverrideController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new ClassOverrideController(), method);
    }

    private static ListAppender<ILoggingEvent> attachAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(InboundLoggingInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static void detachAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(InboundLoggingInterceptor.class);
        logger.detachAppender(appender);
    }

    private static class TestController {
        @SuppressWarnings("unused")
        public String endpoint() {
            return "ok";
        }
    }

    @ControllerLogging(logRequest = false, logResponse = false, logErrors = true)
    private static class ClassOverrideController {

        @SuppressWarnings("unused")
        public String classOnly() {
            return "ok";
        }

        @SuppressWarnings("unused")
        @ControllerLogging(logRequest = true, logResponse = true, logErrors = true)
        public String methodOverride() {
            return "ok";
        }
    }
}

