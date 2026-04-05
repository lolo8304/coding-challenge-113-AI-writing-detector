package ch.lolo.coding.challenge.ai.writer.detector.versioning;

import ch.lolo.common.versioning.ApiVersion;
import ch.lolo.common.versioning.VersionChainPlanner;
import ch.lolo.common.versioning.VersionTransition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersionChainPlannerTest {

    @Test
    void planUpgradeChain_returnsStepByStepTransitions() {
        // Arrange
        VersionChainPlanner planner = new VersionChainPlanner();

        // Act
        List<VersionTransition> transitions =
                planner.planUpgradeChain(ApiVersion.V2024_01_01, ApiVersion.V2026_01_01);

        // Assert
        assertThat(transitions).containsExactly(
                new VersionTransition(ApiVersion.V2024_01_01, ApiVersion.V2025_01_01),
                new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2026_01_01)
        );
    }

    @Test
    void planUpgradeChain_returnsEmpty_whenFromIsNewerThanTo() {
        // Arrange
        VersionChainPlanner planner = new VersionChainPlanner();

        // Act
        List<VersionTransition> transitions =
                planner.planUpgradeChain(ApiVersion.V2026_01_01, ApiVersion.V2024_01_01);

        // Assert
        assertThat(transitions).isEmpty();
    }

    @Test
    void planDowngradeChain_returnsStepByStepTransitions() {
        // Arrange
        VersionChainPlanner planner = new VersionChainPlanner();

        // Act
        List<VersionTransition> transitions =
                planner.planDowngradeChain(ApiVersion.V2026_01_01, ApiVersion.V2024_01_01);

        // Assert
        assertThat(transitions).containsExactly(
                new VersionTransition(ApiVersion.V2026_01_01, ApiVersion.V2025_01_01),
                new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2024_01_01)
        );
    }

    @Test
    void planDowngradeChain_returnsUnmodifiableList() {
        // Arrange
        VersionChainPlanner planner = new VersionChainPlanner();
        List<VersionTransition> transitions =
                planner.planDowngradeChain(ApiVersion.V2026_01_01, ApiVersion.V2025_01_01);

        // Act + Assert
        assertThatThrownBy(() -> transitions.add(new VersionTransition(ApiVersion.V2025_01_01, ApiVersion.V2024_01_01)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void planDowngradeChain_returnsEmpty_whenFromIsOlderThanTo() {
        // Arrange
        VersionChainPlanner planner = new VersionChainPlanner();

        // Act
        List<VersionTransition> transitions =
                planner.planDowngradeChain(ApiVersion.V2024_01_01, ApiVersion.V2026_01_01);

        // Assert
        assertThat(transitions).isEmpty();
    }
}

