package ch.lolo.common.versioning;

import ch.lolo.common.versioning.transform.JsonVersionTransformer;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

public class VersionTransformProcess<T extends JsonVersionTransformer> {

    private final VersionTransformerFactory<T> factory;

    public VersionTransformProcess(VersionTransformerFactory<T> factory) {
        this.factory = factory;
    }

    public ObjectNode transform(ObjectNode source, List<VersionTransition> transitions) {
        ObjectNode current = source.deepCopy();
        for (VersionTransition transition : transitions) {
            current = factory.getRequired(transition).transform(current);
        }
        return current;
    }
}

