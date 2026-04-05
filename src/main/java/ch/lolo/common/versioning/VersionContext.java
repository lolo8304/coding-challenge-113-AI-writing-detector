package ch.lolo.common.versioning;

import java.util.List;

public record VersionContext(
        ApiVersion requestedVersion,
        List<VersionTransition> requestUpgradeTransitions,
        List<VersionTransition> responseDowngradeTransitions
) {
}

