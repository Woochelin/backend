package com.woowacourse.woochelin.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReviewRequest(
        String nickname,
        @NotBlank String password,
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank String content,
        List<Long> tagIds
) {

    public String normalizedNickname() {
        if (nickname == null || nickname.isBlank()) {
            return "익명";
        }
        return nickname.trim();
    }

    public List<Long> normalizedTagIds() {
        if (tagIds == null) {
            return List.of();
        }
        return tagIds.stream().distinct().toList();
    }
}
