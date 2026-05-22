package com.woowacourse.woochelin.common;

import java.util.List;

public record SearchResultResponse(
        TargetType targetType,
        Long id,
        String name,
        Part part,
        String profileImageUrl,
        String slackUrl,
        String botId,
        double averageRating,
        List<TagStatResponse> topTags
) {
}
