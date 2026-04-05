package ch.lolo.common.versioning;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class VersionChainPlanner {

    public List<VersionTransition> planUpgradeChain(ApiVersion fromVersion, ApiVersion toVersion) {
        if (fromVersion.ordinal() > toVersion.ordinal()) {
            return List.of();
        }

        List<VersionTransition> transitions = new ArrayList<>();
        ApiVersion[] versions = ApiVersion.values();
        for (int index = fromVersion.ordinal(); index < toVersion.ordinal(); index++) {
            transitions.add(new VersionTransition(versions[index], versions[index + 1]));
        }
        return transitions;
    }

    public List<VersionTransition> planDowngradeChain(ApiVersion fromVersion, ApiVersion toVersion) {
        if (fromVersion.ordinal() < toVersion.ordinal()) {
            return List.of();
        }

        List<VersionTransition> transitions = new ArrayList<>();
        ApiVersion[] versions = ApiVersion.values();
        for (int index = fromVersion.ordinal(); index > toVersion.ordinal(); index--) {
            transitions.add(new VersionTransition(versions[index], versions[index - 1]));
        }
        return Collections.unmodifiableList(transitions);
    }
}

