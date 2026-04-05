package ch.lolo.coding.challenge.ai.writer.detector;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.common.configuration.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "ch.lolo")
@EnableConfigurationProperties(ApplicationConfiguration.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.addInitializers(new DotenvConfig());
        app.run(args);
    }

}