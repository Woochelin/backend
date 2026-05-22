package com.woowacourse.woochelin.home;

import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.TargetType;

public record RankingResponse(
        TargetType targetType,
        Long targetId,
        String name,
        Part part,
        String profileImageUrl,
        long viewCount
) {
}
