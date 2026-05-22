package com.woowacourse.woochelin.common;

public record TrendingSearchTermResponse(
        int rank,
        String keyword,
        String badge,
        String trendType,
        String searchCount,
        String rankedAt
) {
}
