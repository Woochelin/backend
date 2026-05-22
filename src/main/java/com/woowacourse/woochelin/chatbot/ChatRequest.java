package com.woowacourse.woochelin.chatbot;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String botId,
        @NotBlank String message
) {
}
