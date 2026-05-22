package com.woowacourse.woochelin.common;

public record TagStatResponse(Long id, String name, long count) {

    public static TagStatResponse from(Object[] row) {
        return new TagStatResponse(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).longValue()
        );
    }
}
