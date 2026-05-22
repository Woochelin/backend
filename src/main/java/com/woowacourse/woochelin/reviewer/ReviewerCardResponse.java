package com.woowacourse.woochelin.reviewer;

import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.TagStatResponse;
import java.util.List;

public record ReviewerCardResponse(
        Long id,
        String name,
        Part part,
        String profileImageUrl,
        String slackUrl,
        String style,
        double averageRating,
        List<TagStatResponse> topTags
) {
}
