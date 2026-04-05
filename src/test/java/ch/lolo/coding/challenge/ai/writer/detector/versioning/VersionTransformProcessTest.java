package ch.lolo.coding.challenge.ai.writer.detector.versioning;

import ch.lolo.common.exception.BadRequestException;
import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionTransformProcess;
import ch.lolo.common.versioning.VersionTransformerFactory;
import ch.lolo.common.versioning.VersionTransition;
import ch.lolo.common.versioning.transform.JsonVersionTransformer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersionTransformProcessTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void transform_appliesAllTransitionsInOrder() {
        // Arrange
        VersionTransition firstTransition = new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01);
        VersionTransition secondTransition = new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2026_01_01);

        JsonVersionTransformer first = new MarkerTransformer(firstTransition, "step", "first");
        JsonVersionTransformer second = new MarkerTransformer(secondTransition, "step2", "second");

        VersionTransformerFactory<JsonVersionTransformer> factory = new VersionTransformerFactory<>(
                List.of(first, second),
                "TRANSFORMER_NOT_FOUND",
                transition -> "Missing transformer for " + transition.from() + " -> " + transition.to()
        );
        VersionTransformProcess<JsonVersionTransformer> process = new VersionTransformProcess<>(factory);

        ObjectNode source = OBJECT_MAPPER.createObjectNode();
        source.put("name", "Acme");

        // Act
        ObjectNode transformed = process.transform(source, List.of(firstTransition, secondTransition));

        // Assert
        assertThat(transformed.get("step").asString()).isEqualTo("first");
        assertThat(transformed.get("step2").asString()).isEqualTo("second");
        assertThat(source.has("step")).isFalse();
        assertThat(source.has("step2")).isFalse();
    }

    @Test
    void transform_throwsBadRequest_whenTransitionTransformerIsMissing() {
        // Arrange
        VersionTransition knownTransition = new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2026_01_01);
        VersionTransition missingTransition = new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01);

        VersionTransformerFactory<JsonVersionTransformer> factory = new VersionTransformerFactory<>(
                List.of(new MarkerTransformer(knownTransition, "step", "known")),
                "TRANSFORMER_NOT_FOUND",
                transition -> "Missing transformer for " + transition.from() + " -> " + transition.to()
        );
        VersionTransformProcess<JsonVersionTransformer> process = new VersionTransformProcess<>(factory);

        ObjectNode source = OBJECT_MAPPER.createObjectNode();

        // Act + Assert
        assertThatThrownBy(() -> process.transform(source, List.of(missingTransition)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing transformer for")
                .extracting(exception -> ((BadRequestException) exception).getCode())
                .isEqualTo("TRANSFORMER_NOT_FOUND");
    }

    private record MarkerTransformer(VersionTransition transition, String fieldName, String fieldValue)
            implements JsonVersionTransformer {

        @Override
        public ObjectNode transform(ObjectNode source) {
            ObjectNode target = source.deepCopy();
            target.put(fieldName, fieldValue);
            return target;
        }
    }
}

