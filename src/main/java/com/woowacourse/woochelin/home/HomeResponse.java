package com.woowacourse.woochelin.home;

import java.util.List;

public record HomeResponse(
        List<RankingResponse> weeklyRanking,
        List<RankingResponse> weeklyCoachRanking,
        List<RankingResponse> weeklyReviewerRanking,
        List<RecentReviewResponse> recentReviews
) {
}
