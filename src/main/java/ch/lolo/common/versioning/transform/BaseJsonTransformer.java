package ch.lolo.common.versioning.transform;

import ch.lolo.common.exception.BadRequestException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseJsonTransformer implements JsonVersionTransformer {

    protected void removeField(ObjectNode root, String path) {
        ObjectNode parent = parentNode(root, path, false);
        if (parent == null) {
            return;
        }
        parent.remove(lastSegment(path));
    }

    protected void addField(ObjectNode root, String path, JsonNode value) {
        ObjectNode parent = parentNode(root, path, true);
        parent.set(lastSegment(path), value);
    }

    protected void renameField(ObjectNode root, String fromPath, String toPath) {
        JsonNode value = readField(root, fromPath);
        if (value == null || value.isMissingNode()) {
            return;
        }
        addField(root, toPath, value);
        removeField(root, fromPath);
    }

    protected void checkMandatoryField(ObjectNode root, String path) {
        JsonNode value = readField(root, path);
        if (value == null || value.isMissingNode()) {
            throw new BadRequestException(
                    "MANDATORY_FIELD_MISSING",
                    "Mandatory field '" + path + "' is missing"
            );
        }
    }

    protected void splitField(
            ObjectNode root,
            String sourcePath,
            String firstTargetPath,
            Function<JsonNode, JsonNode> firstMapper,
            String secondTargetPath,
            Function<JsonNode, JsonNode> secondMapper
    ) {
        JsonNode value = readField(root, sourcePath);
        if (value == null || value.isMissingNode()) {
            return;
        }

        addField(root, firstTargetPath, firstMapper.apply(value));
        addField(root, secondTargetPath, secondMapper.apply(value));
        removeField(root, sourcePath);
    }

    protected void custom(ObjectNode root, Consumer<ObjectNode> customizer) {
        customizer.accept(root);
    }

    protected JsonNode readField(ObjectNode root, String path) {
        String[] segments = path.split("\\.");
        JsonNode current = root;
        for (String segment : segments) {
            if (current == null || !current.isObject()) {
                return JsonNodeFactory.instance.missingNode();
            }
            current = current.get(segment);
        }
        return current == null ? JsonNodeFactory.instance.missingNode() : current;
    }

    private ObjectNode parentNode(ObjectNode root, String path, boolean createMissing) {
        String[] segments = path.split("\\.");
        ObjectNode current = root;
        for (int index = 0; index < segments.length - 1; index++) {
            JsonNode child = current.get(segments[index]);
            if (child == null || child.isMissingNode() || !child.isObject()) {
                if (!createMissing) {
                    return null;
                }
                ObjectNode created = JsonNodeFactory.instance.objectNode();
                current.set(segments[index], created);
                current = created;
                continue;
            }
            current = (ObjectNode) child;
        }
        return current;
    }

    private String lastSegment(String path) {
        String[] segments = path.split("\\.");
        return segments[segments.length - 1];
    }
}
