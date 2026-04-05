package ch.lolo.common.configuration;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.common.logging.InboundLoggingInterceptor;
import ch.lolo.common.versioning.VersionContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers Spring MVC interceptors.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ApplicationConfiguration applicationConfiguration;
    private final VersionContextInterceptor versionContextInterceptor;

    public WebMvcConfiguration(ApplicationConfiguration applicationConfiguration,
                               VersionContextInterceptor versionContextInterceptor) {
        this.applicationConfiguration = applicationConfiguration;
        this.versionContextInterceptor = versionContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InboundLoggingInterceptor(applicationConfiguration.getLogging()));
        registry.addInterceptor(versionContextInterceptor)
                .addPathPatterns("/rest/ai/detector/v1/contracts");
    }
}
