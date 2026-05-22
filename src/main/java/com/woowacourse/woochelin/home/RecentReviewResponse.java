package com.woowacourse.woochelin.home;

import com.woowacourse.woochelin.common.ReviewTagResponse;
import com.woowacourse.woochelin.common.TargetType;
import java.time.LocalDateTime;
import java.util.List;

public record RecentReviewResponse(
        TargetType targetType,
        Long targetId,
        String targetName,
        Long reviewId,
        String nickname,
        int rating,
        String content,
        List<ReviewTagResponse> tags,
        LocalDateTime createdAt
) {
}
