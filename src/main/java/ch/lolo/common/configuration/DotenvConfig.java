package ch.lolo.common.configuration;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.getEnvironment().getPropertySources().addFirst(new DotenvPropertySource());
    }
}