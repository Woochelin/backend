package com.woowacourse.woochelin.common;

import java.util.List;

public record SearchResponse(
        String keyword,
        List<SearchResultResponse> results
) {
}
