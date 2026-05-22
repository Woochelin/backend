package com.woowacourse.woochelin.common;

import jakarta.validation.constraints.NotBlank;

public record DeleteReviewRequest(@NotBlank String password) {
}
