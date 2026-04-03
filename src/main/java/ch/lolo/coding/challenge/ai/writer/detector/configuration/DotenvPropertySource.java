package ch.lolo.coding.challenge.ai.writer.detector.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvPropertySource extends PropertySource<Map<String, Object>> {

    public DotenvPropertySource() {
        super("dotenvProperties", loadEnv());
    }

    private static Map<String, Object> loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> props = new HashMap<>();

        dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE).forEach(entry -> {
            props.put(entry.getKey(), entry.getValue());
        });

        return props;
    }

    @Override
    public Object getProperty(@NonNull String name) {
        return this.getSource().get(name);
    }

}