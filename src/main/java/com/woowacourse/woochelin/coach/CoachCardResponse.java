package com.woowacourse.woochelin.coach;

import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.TagStatResponse;
import java.util.List;

public record CoachCardResponse(
        Long id,
        String name,
        Part part,
        String profileImageUrl,
        String slackUrl,
        String botId,
        String botDescription,
        double averageRating,
        List<TagStatResponse> topTags
) {
}
