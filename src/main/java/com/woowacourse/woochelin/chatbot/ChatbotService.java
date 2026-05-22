package com.woowacourse.woochelin.chatbot;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.common.exception.NotFoundException;
import com.woowacourse.woochelin.home.RankingResponse;
import java.time.LocalDateTime;
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

    public ChatbotService(CoachRepository coachRepository, SearchLogRepository searchLogRepository, GeminiClient geminiClient) {
        this.coachRepository = coachRepository;
        this.searchLogRepository = searchLogRepository;
        this.geminiClient = geminiClient;
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
        Coach coach = coachRepository.findByBotId(request.botId())
                .orElseThrow(() -> new NotFoundException("챗봇 코치를 찾을 수 없습니다."));
        String reply = geminiClient.generate(coach.getPersonaPrompt(), request.message());
        return new ChatResponse(reply);
    }
}
