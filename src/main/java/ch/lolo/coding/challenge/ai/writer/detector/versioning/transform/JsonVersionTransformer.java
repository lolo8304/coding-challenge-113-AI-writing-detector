package ch.lolo.coding.challenge.ai.writer.detector.versioning.transform;

import ch.lolo.coding.challenge.ai.writer.detector.versioning.VersionTransition;
import tools.jackson.databind.node.ObjectNode;

public interface JsonVersionTransformer {

    VersionTransition transition();

    ObjectNode transform(ObjectNode source);
}

