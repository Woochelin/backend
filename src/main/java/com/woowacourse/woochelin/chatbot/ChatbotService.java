package com.woowacourse.woochelin.chatbot;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.common.exception.NotFoundException;
import com.woowacourse.woochelin.home.RankingResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private static final int TRENDING_LIMIT = 10;

    private final CoachRepository coachRepository;
    private final SearchLogRepository searchLogRepository;
    private final GeminiClient geminiClient;
    private final ChatSessionStore chatSessionStore;

    public ChatbotService(
            CoachRepository coachRepository,
            SearchLogRepository searchLogRepository,
            GeminiClient geminiClient,
            ChatSessionStore chatSessionStore
    ) {
        this.coachRepository = coachRepository;
        this.searchLogRepository = searchLogRepository;
        this.geminiClient = geminiClient;
        this.chatSessionStore = chatSessionStore;
    }

    public List<RankingResponse> trending() {
        List<Object[]> rows = searchLogRepository.ranking(
                TargetType.COACH.name(),
                LocalDateTime.now().minusDays(7),
                TRENDING_LIMIT
        );
        Map<Long, Coach> coaches = coachRepository.findAllById(rows.stream()
                        .map(row -> ((Number) row[0]).longValue())
                        .toList())
                .stream()
                .collect(Collectors.toMap(Coach::getId, Function.identity()));
        return rows.stream()
                .map(row -> {
                    Long id = ((Number) row[0]).longValue();
                    Coach coach = coaches.get(id);
                    return new RankingResponse(
                            TargetType.COACH,
                            id,
                            coach.getName(),
                            coach.getPart(),
                            coach.getProfileImageUrl(),
                            ((Number) row[1]).longValue()
                    );
                })
                .toList();
    }

    public ChatResponse chat(ChatRequest request) {
        ChatSession session = chatSessionStore.getOrCreate(
                request.sessionId(), request.botId(), request.crewName());
        Coach coach = coachRepository.findByBotId(session.botId())
                .orElseThrow(() -> new NotFoundException("챗봇 코치를 찾을 수 없습니다."));

        String systemPrompt = buildSystemPrompt(coach.getPersonaPrompt(), session.crewName());
        List<ChatMessage> conversation = new ArrayList<>(session.history());
        conversation.add(ChatMessage.user(request.message()));

        String reply = geminiClient.generate(systemPrompt, conversation);
        session.append(request.message(), reply);
        return new ChatResponse(reply);
    }

    private String buildSystemPrompt(String persona, String crewName) {
        return """
                %s

                [상담 정보]
                상담을 요청한 크루의 이름은 "%s"입니다. 대화 중 이름을 자연스럽게 불러 주고, \
                이전 대화 맥락을 이어 가며 이 크루의 고민에 맞춰 상담해 주세요.""".formatted(persona, crewName);
    }
}
