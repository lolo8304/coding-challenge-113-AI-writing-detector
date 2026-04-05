package ch.lolo.coding.challenge.ai.writer.detector.controller;

import ch.lolo.common.versioning.ApiVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ContractVersioningIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @Test
    void post_contracts_withVersion2024_upgradesRequestAndDowngradesResponse() throws Exception {
        // Arrange – v2024 client sends old structure: "name" + flat "premium"
        String v2024Body = """
                {
                  "name": "Acme Corp",
                  "premium": 500.00
                }
                """;

        // Act
        MvcResult result = mockMvc.perform(
                        post("/rest/ai/detector/v1/contracts")
                                .header(ApiVersion.VERSION_HEADER, "2024-01-01")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(v2024Body)
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // Assert – response is downgraded back to v2024 structure (flat "premium", no "id")
        assertThat(responseBody).contains("\"premium\"");
        assertThat(responseBody).doesNotContain("\"amount\"");
        assertThat(responseBody).doesNotContain("\"id\"");
        assertThat(responseBody).contains("\"name\"");
    }

    @Test
    void post_contracts_withVersion2025_upgradesRequestAndDowngradesResponse() throws Exception {
        // Arrange – v2025 client sends "customerName" + nested "premium"
        String v2025Body = """
                {
                  "customerName": "Acme Corp",
                  "premium": { "amount": 500.00, "currency": "CHF" }
                }
                """;

        // Act
        MvcResult result = mockMvc.perform(
                        post("/rest/ai/detector/v1/contracts")
                                .header(ApiVersion.VERSION_HEADER, "2025-01-01")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(v2025Body)
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // Assert – response is downgraded back to v2025 structure ("customerName", no "id")
        assertThat(responseBody).contains("\"customerName\"");
        assertThat(responseBody).doesNotContain("\"id\"");
        assertThat(responseBody).contains("\"premium\"");
    }

    @Test
    void post_contracts_withoutVersionHeader_usesLatestAndReturnsCurrentStructure() throws Exception {
        // Arrange – latest client sends new structure: "name" + nested "premium"
        String latestBody = """
                {
                  "name": "Acme Corp",
                  "premium": { "amount": 500.00, "currency": "CHF" }
                }
                """;

        // Act
        MvcResult result = mockMvc.perform(
                        post("/rest/ai/detector/v1/contracts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(latestBody)
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // Assert – response is full latest structure
        assertThat(responseBody).contains("\"firstName\"");
        assertThat(responseBody).contains("\"lastName\"");
        assertThat(responseBody).doesNotContain("name");
        assertThat(responseBody).contains("\"premium\"");
        assertThat(responseBody).contains("\"id\"");
    }
}

