package com.woowacourse.woochelin.chatbot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private SearchLogRepository searchLogRepository;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    @DisplayName("botId에 맞는 코치 페르소나로 Gemini 응답을 생성한다")
    void chatWithCoachPersona() {
        Coach coach = new Coach("검프", Part.BACKEND, "/coach/gump.png", "https://slack", "gump", "system prompt");
        when(coachRepository.findByBotId("gump")).thenReturn(Optional.of(coach));
        when(geminiClient.generate("system prompt", "코딩이 어려워요")).thenReturn("왜 어렵다고 생각하시죠?");

        ChatResponse response = chatbotService.chat(new ChatRequest("gump", "코딩이 어려워요"));

        assertThat(response.reply()).isEqualTo("왜 어렵다고 생각하시죠?");
    }

    @Test
    @DisplayName("존재하지 않는 botId는 404 도메인 예외로 거절한다")
    void rejectUnknownBotId() {
        when(coachRepository.findByBotId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatbotService.chat(new ChatRequest("unknown", "안녕하세요")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("챗봇 코치");
    }
}
