package ch.lolo.common.versioning.transform;

import ch.lolo.common.versioning.VersionTransition;
import tools.jackson.databind.node.ObjectNode;

public interface JsonVersionTransformer {

    VersionTransition transition();

    ObjectNode transform(ObjectNode source);
}

