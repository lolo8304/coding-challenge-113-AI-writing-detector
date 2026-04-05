package ch.lolo.coding.challenge.ai.writer.detector.versioning;

import ch.lolo.common.exception.BadRequestException;
import ch.lolo.common.versioning.ApiVersion;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiVersionTest {

    @Test
    void latest_returnsMostRecentVersion() {
        // Arrange
        ApiVersion expected = ApiVersion.V2026_01_01;

        // Act
        ApiVersion actual = ApiVersion.latest();

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolve_returnsLatest_whenHeaderMissingBlankOrLatestKeyword() {
        // Arrange
        String missing = null;
        String blank = "   ";
        String latestKeyword = "latest";

        // Act
        ApiVersion fromMissing = ApiVersion.resolve(missing);
        ApiVersion fromBlank = ApiVersion.resolve(blank);
        ApiVersion fromLatestKeyword = ApiVersion.resolve(latestKeyword);

        // Assert
        assertThat(fromMissing).isEqualTo(ApiVersion.latest());
        assertThat(fromBlank).isEqualTo(ApiVersion.latest());
        assertThat(fromLatestKeyword).isEqualTo(ApiVersion.latest());
    }

    @Test
    void resolve_mapsDateToEffectiveVersion() {
        // Arrange
        String dateIn2024 = "2024-03-01";
        String dateIn2025 = "2025-03-01";
        String dateIn2026 = "2026-03-01";

        // Act
        ApiVersion v2024 = ApiVersion.resolve(dateIn2024);
        ApiVersion v2025 = ApiVersion.resolve(dateIn2025);
        ApiVersion v2026 = ApiVersion.resolve(dateIn2026);

        // Assert
        assertThat(v2024).isEqualTo(ApiVersion.V2024_01_01);
        assertThat(v2025).isEqualTo(ApiVersion.V2025_01_01);
        assertThat(v2026).isEqualTo(ApiVersion.V2026_01_01);
    }

    @Test
    void resolve_returnsFirstVersion_whenDateIsBeforeAllEffectiveDates() {
        // Arrange
        String veryOldDate = "2020-01-01";

        // Act
        ApiVersion resolved = ApiVersion.resolve(veryOldDate);

        // Assert
        assertThat(resolved).isEqualTo(ApiVersion.V2024_01_01);
    }

    @Test
    void resolve_throwsBadRequest_whenHeaderIsInvalidDate() {
        // Arrange
        String invalidHeader = "not-a-date";

        // Act + Assert
        assertThatThrownBy(() -> ApiVersion.resolve(invalidHeader))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> {
                    BadRequestException badRequestException = (BadRequestException) ex;
                    assertThat(badRequestException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(badRequestException.getCode()).isEqualTo("INVALID_VERSION_HEADER");
                });
    }
}

