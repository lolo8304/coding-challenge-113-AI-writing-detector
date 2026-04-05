package ch.lolo.coding.challenge.ai.writer.detector.versioning;

import java.util.List;

public record VersionContext(
        ApiVersion requestedVersion,
        List<VersionTransition> requestUpgradeTransitions,
        List<VersionTransition> responseDowngradeTransitions
) {
}

