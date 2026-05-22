package com.woowacourse.woochelin.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewRequestTest {

    @Test
    @DisplayName("닉네임이 비어 있으면 익명으로 정규화한다")
    void defaultNickname() {
        ReviewRequest request = new ReviewRequest(" ", "1234", 5, "좋았어요", List.of());

        assertThat(request.normalizedNickname()).isEqualTo("익명");
    }

    @Test
    @DisplayName("태그 목록은 중복을 제거하고 입력 순서를 유지한다")
    void distinctTagIds() {
        ReviewRequest request = new ReviewRequest("크루", "1234", 5, "좋았어요", List.of(1L, 2L, 1L, 3L));

        assertThat(request.normalizedTagIds()).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("태그 목록이 없으면 빈 목록으로 정규화한다")
    void nullTagIds() {
        ReviewRequest request = new ReviewRequest("크루", "1234", 5, "좋았어요", null);

        assertThat(request.normalizedTagIds()).isEmpty();
    }
}
