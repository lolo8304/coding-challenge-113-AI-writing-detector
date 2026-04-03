package ch.lolo.coding.challenge.ai.writer.detector.controller;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.coding.challenge.ai.writer.detector.http.HttpClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/ai/detector")
public class AiWritingDetector {

    private final HttpClient httpClient;
    private final ApplicationConfiguration applicationConfiguration;

    public AiWritingDetector(HttpClient httpClient, ApplicationConfiguration applicationConfiguration) {
        this.httpClient = httpClient;
        this.applicationConfiguration = applicationConfiguration;
    }

    @RequestMapping(value = "hello-world", method = RequestMethod.GET)
    String home() {
        return "Hello World!";
    }

    @RequestMapping(value = "hello-world-remote", method = RequestMethod.GET)
    String homeRemote() {
        String endpoint = applicationConfiguration
                .getRequiredIntegration(httpClient.getIntegrationName())
                .getRequiredEndpoint("hello-world");
        return httpClient.get(endpoint, String.class);
    }

}