package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import ch.lolo.coding.challenge.ai.writer.detector.controller.logging.InboundLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers Spring MVC interceptors.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ApplicationConfiguration applicationConfiguration;

    public WebMvcConfiguration(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InboundLoggingInterceptor(applicationConfiguration.getLogging()));
    }
}

