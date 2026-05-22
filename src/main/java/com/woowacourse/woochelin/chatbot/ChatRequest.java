package com.woowacourse.woochelin.chatbot;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String sessionId,
        @NotBlank String botId,
        @NotBlank String crewName,
        @NotBlank String message
) {
}
