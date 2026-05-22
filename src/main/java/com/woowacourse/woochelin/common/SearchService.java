package com.woowacourse.woochelin.common;

import com.woowacourse.woochelin.coach.CoachCardResponse;
import com.woowacourse.woochelin.coach.CoachService;
import com.woowacourse.woochelin.reviewer.ReviewerCardResponse;
import com.woowacourse.woochelin.reviewer.ReviewerService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final CoachService coachService;
    private final ReviewerService reviewerService;

    public SearchService(CoachService coachService, ReviewerService reviewerService) {
        this.coachService = coachService;
        this.reviewerService = reviewerService;
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

    private SearchResultResponse fromCoach(CoachCardResponse coach) {
        return new SearchResultResponse(
                TargetType.COACH,
                coach.id(),
                coach.name(),
                coach.part(),
                coach.profileImageUrl(),
                coach.slackUrl(),
                coach.botId(),
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
                reviewer.averageRating(),
                reviewer.topTags()
        );
    }
}
