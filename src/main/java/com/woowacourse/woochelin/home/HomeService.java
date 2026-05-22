package com.woowacourse.woochelin.home;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.coach.CoachReview;
import com.woowacourse.woochelin.coach.CoachReviewRepository;
import com.woowacourse.woochelin.common.ReviewTagResponse;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.reviewer.Reviewer;
import com.woowacourse.woochelin.reviewer.ReviewerRepository;
import com.woowacourse.woochelin.reviewer.ReviewerReview;
import com.woowacourse.woochelin.reviewer.ReviewerReviewRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

    private static final int RANKING_LIMIT = 3;
    private static final int RECENT_REVIEW_LIMIT = 10;

    private final SearchLogRepository searchLogRepository;
    private final CoachRepository coachRepository;
    private final ReviewerRepository reviewerRepository;
    private final CoachReviewRepository coachReviewRepository;
    private final ReviewerReviewRepository reviewerReviewRepository;

    public HomeService(
            SearchLogRepository searchLogRepository,
            CoachRepository coachRepository,
            ReviewerRepository reviewerRepository,
            CoachReviewRepository coachReviewRepository,
            ReviewerReviewRepository reviewerReviewRepository
    ) {
        this.searchLogRepository = searchLogRepository;
        this.coachRepository = coachRepository;
        this.reviewerRepository = reviewerRepository;
        this.coachReviewRepository = coachReviewRepository;
        this.reviewerReviewRepository = reviewerReviewRepository;
    }

    @Transactional(readOnly = true)
    public HomeResponse home() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<RankingResponse> coachRanking = coachRanking(from);
        List<RankingResponse> reviewerRanking = reviewerRanking(from);
        return new HomeResponse(
                combinedRanking(coachRanking, reviewerRanking),
                coachRanking,
                reviewerRanking,
                recentReviews()
        );
    }

    private List<RankingResponse> combinedRanking(List<RankingResponse> coachRanking, List<RankingResponse> reviewerRanking) {
        return java.util.stream.Stream.concat(coachRanking.stream(), reviewerRanking.stream())
                .sorted(Comparator.comparingLong(RankingResponse::viewCount).reversed())
                .limit(RANKING_LIMIT)
                .toList();
    }

    private List<RankingResponse> coachRanking(LocalDateTime from) {
        List<Object[]> rows = searchLogRepository.ranking(TargetType.COACH.name(), from, RANKING_LIMIT);
        Map<Long, Coach> coaches = coachRepository.findAllById(ids(rows)).stream()
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

    private List<RankingResponse> reviewerRanking(LocalDateTime from) {
        List<Object[]> rows = searchLogRepository.ranking(TargetType.REVIEWER.name(), from, RANKING_LIMIT);
        Map<Long, Reviewer> reviewers = reviewerRepository.findAllById(ids(rows)).stream()
                .collect(Collectors.toMap(Reviewer::getId, Function.identity()));
        return rows.stream()
                .map(row -> {
                    Long id = ((Number) row[0]).longValue();
                    Reviewer reviewer = reviewers.get(id);
                    return new RankingResponse(
                            TargetType.REVIEWER,
                            id,
                            reviewer.getName(),
                            reviewer.getPart(),
                            reviewer.getProfileImageUrl(),
                            ((Number) row[1]).longValue()
                    );
                })
                .toList();
    }

    private List<RecentReviewResponse> recentReviews() {
        List<RecentReviewResponse> coachReviews = coachReviewRepository.findTop10ByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::toRecentReview)
                .toList();
        List<RecentReviewResponse> reviewerReviews = reviewerReviewRepository.findTop10ByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::toRecentReview)
                .toList();
        return java.util.stream.Stream.concat(coachReviews.stream(), reviewerReviews.stream())
                .sorted(Comparator.comparing(RecentReviewResponse::createdAt).reversed())
                .limit(RECENT_REVIEW_LIMIT)
                .toList();
    }

    private RecentReviewResponse toRecentReview(CoachReview review) {
        return new RecentReviewResponse(
                TargetType.COACH,
                review.getCoach().getId(),
                review.getCoach().getName(),
                review.getId(),
                review.getNickname(),
                review.getRating(),
                review.getContent(),
                review.getTags().stream().map(ReviewTagResponse::from).toList(),
                review.getCreatedAt()
        );
    }

    private RecentReviewResponse toRecentReview(ReviewerReview review) {
        return new RecentReviewResponse(
                TargetType.REVIEWER,
                review.getReviewer().getId(),
                review.getReviewer().getName(),
                review.getId(),
                review.getNickname(),
                review.getRating(),
                review.getContent(),
                review.getTags().stream().map(ReviewTagResponse::from).toList(),
                review.getCreatedAt()
        );
    }

    private List<Long> ids(List<Object[]> rows) {
        return rows.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();
    }
}
