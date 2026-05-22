package com.woowacourse.woochelin.coach;

import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.TagStatResponse;
import java.util.List;

public record CoachDetailResponse(
        Long id,
        String name,
        Part part,
        String profileImageUrl,
        String slackUrl,
        String botId,
        double averageRating,
        List<TagStatResponse> topTags,
        List<CoachReviewResponse> reviews
) {
}
