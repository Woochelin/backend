package com.woowacourse.woochelin.common;

import com.woowacourse.woochelin.tag.Tag;

public record ReviewTagResponse(Long id, String name) {

    public static ReviewTagResponse from(Tag tag) {
        return new ReviewTagResponse(tag.getId(), tag.getName());
    }
}
