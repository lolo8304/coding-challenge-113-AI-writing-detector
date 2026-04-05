package ch.lolo.coding.challenge.ai.writer.detector.controller;

import ch.lolo.coding.challenge.ai.writer.detector.configuration.ApplicationConfiguration;
import ch.lolo.common.http.HttpClient;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.Contract;
import ch.lolo.coding.challenge.ai.writer.detector.model.contracts.ContractFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/ai/detector/v1")
public class AiWritingDetectorController {

    private final HttpClient httpClient;
    private final ApplicationConfiguration applicationConfiguration;
    private final ContractFactory contractFactory;

    public AiWritingDetectorController(HttpClient httpClient,
                                       ApplicationConfiguration applicationConfiguration,
                                       ContractFactory contractFactory) {
        this.httpClient = httpClient;
        this.applicationConfiguration = applicationConfiguration;
        this.contractFactory = contractFactory;
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

    @RequestMapping(value = "contracts", method = RequestMethod.POST)
    Contract createContract(@RequestBody Contract contract) {
        return contractFactory.fromName(contract.getName());
    }
}