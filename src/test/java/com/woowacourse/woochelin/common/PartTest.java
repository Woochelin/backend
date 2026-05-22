package com.woowacourse.woochelin.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PartTest {

    @Test
    @DisplayName("파트는 API, 파일명, 한글 별칭을 표준 enum으로 변환한다")
    void parseAliases() {
        assertThat(Part.from("backend")).isEqualTo(Part.BACKEND);
        assertThat(Part.from("BE")).isEqualTo(Part.BACKEND);
        assertThat(Part.from("프론트")).isEqualTo(Part.FRONTEND);
        assertThat(Part.from("AOS")).isEqualTo(Part.ANDROID);
        assertThat(Part.from("SS")).isEqualTo(Part.SOFT_SKILL);
    }

    @Test
    @DisplayName("비어 있는 파트 필터는 전체 조회를 의미한다")
    void blankPartMeansNoFilter() {
        assertThat(Part.from(null)).isNull();
        assertThat(Part.from(" ")).isNull();
    }

    @Test
    @DisplayName("지원하지 않는 파트는 명시적으로 거절한다")
    void rejectUnsupportedPart() {
        assertThatThrownBy(() -> Part.from("ios"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 파트");
    }
}
