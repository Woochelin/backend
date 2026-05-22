package com.woowacourse.woochelin.common;

import com.woowacourse.woochelin.coach.CoachCardResponse;
import com.woowacourse.woochelin.coach.CoachService;
import com.woowacourse.woochelin.reviewer.ReviewerCardResponse;
import com.woowacourse.woochelin.reviewer.ReviewerService;
import java.util.Comparator;
import java.util.List;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final CoachService coachService;
    private final ReviewerService reviewerService;
    private final SearchKeywordLogRepository searchKeywordLogRepository;

    public SearchService(
            CoachService coachService,
            ReviewerService reviewerService,
            SearchKeywordLogRepository searchKeywordLogRepository
    ) {
        this.coachService = coachService;
        this.reviewerService = reviewerService;
        this.searchKeywordLogRepository = searchKeywordLogRepository;
    }

    public SearchResponse search(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        List<SearchResultResponse> coachResults = coachService.findAll(null, normalizedKeyword).stream()
                .map(this::fromCoach)
                .toList();
        List<SearchResultResponse> reviewerResults = reviewerService.findAll(null, normalizedKeyword).stream()
                .map(this::fromReviewer)
                .toList();
        List<SearchResultResponse> results = Stream.concat(coachResults.stream(), reviewerResults.stream())
                .sorted(Comparator.comparing(SearchResultResponse::name))
                .toList();
        return new SearchResponse(normalizedKeyword, results);
    }

    public List<TrendingSearchTermResponse> trending() {
        NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm '기준'");
        List<SearchKeywordLog> logs = searchKeywordLogRepository.findTop10ByOrderBySearchCountDescIdAsc();
        java.util.List<TrendingSearchTermResponse> responses = new java.util.ArrayList<>();
        for (int index = 0; index < logs.size(); index++) {
            SearchKeywordLog log = logs.get(index);
            responses.add(new TrendingSearchTermResponse(
                    index + 1,
                    log.getKeyword(),
                    log.getBadge(),
                    log.getTrendType(),
                    numberFormat.format(log.getSearchCount()),
                    log.getRankedAt().format(timeFormatter)
            ));
        }
        return responses;
    }

    private SearchResultResponse fromCoach(CoachCardResponse coach) {
        return new SearchResultResponse(
                TargetType.COACH,
                coach.id(),
                coach.name(),
                coach.part(),
                coach.profileImageUrl(),
                coach.slackUrl(),
                coach.botId(),
                coach.botDescription(),
                null,
                coach.averageRating(),
                coach.topTags()
        );
    }

    private SearchResultResponse fromReviewer(ReviewerCardResponse reviewer) {
        return new SearchResultResponse(
                TargetType.REVIEWER,
                reviewer.id(),
                reviewer.name(),
                reviewer.part(),
                reviewer.profileImageUrl(),
                reviewer.slackUrl(),
                null,
                null,
                reviewer.style(),
                reviewer.averageRating(),
                reviewer.topTags()
        );
    }
}
