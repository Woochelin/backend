package com.woowacourse.woochelin.chatbot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(
                coachRepository, searchLogRepository, geminiClient, new ChatSessionStore());
    }

    @Test
    @DisplayName("botId에 맞는 코치 페르소나로 Gemini 응답을 생성한다")
    void chatWithCoachPersona() {
        Coach coach = new Coach("검프", Part.BACKEND, "/coach/gump.png", "https://slack", "gump", "system prompt");
        when(coachRepository.findByBotId("gump")).thenReturn(Optional.of(coach));
        when(geminiClient.generate(anyString(), anyList())).thenReturn("왜 어렵다고 생각하시죠?");

        ChatResponse response = chatbotService.chat(
                new ChatRequest("session-1", "gump", "체체", "코딩이 어려워요"));

        assertThat(response.reply()).isEqualTo("왜 어렵다고 생각하시죠?");
    }

    @Test
    @DisplayName("존재하지 않는 botId는 404 도메인 예외로 거절한다")
    void rejectUnknownBotId() {
        when(coachRepository.findByBotId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatbotService.chat(
                new ChatRequest("session-2", "unknown", "체체", "안녕하세요")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("챗봇 코치");
    }

    @Test
    @DisplayName("같은 sessionId의 다음 대화에는 이전 질문·답변이 함께 전달된다")
    void remembersConversationHistory() {
        Coach coach = new Coach("검프", Part.BACKEND, "/coach/gump.png", "https://slack", "gump", "system prompt");
        when(coachRepository.findByBotId("gump")).thenReturn(Optional.of(coach));
        when(geminiClient.generate(anyString(), anyList())).thenReturn("첫 답변", "두 번째 답변");

        chatbotService.chat(new ChatRequest("session-3", "gump", "체체", "첫 질문"));
        chatbotService.chat(new ChatRequest("session-3", "gump", "체체", "두 번째 질문"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(geminiClient, times(2)).generate(anyString(), captor.capture());
        List<ChatMessage> secondCall = captor.getAllValues().get(1);
        assertThat(secondCall).containsExactly(
                ChatMessage.user("첫 질문"),
                ChatMessage.bot("첫 답변"),
                ChatMessage.user("두 번째 질문"));
    }
}
