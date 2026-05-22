package com.woowacourse.woochelin.common;

import java.util.Arrays;

public enum Part {

    BACKEND("BE", "backend", "back", "백"),
    FRONTEND("FE", "frontend", "front", "프론트"),
    ANDROID("AN", "AOS", "android", "안드"),
    COMMON("CT", "common"),
    SOFT_SKILL("SS", "softskill", "soft_skill");

    private final String[] aliases;

    Part(String... aliases) {
        this.aliases = aliases;
    }

    public static Part from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace("-", "_");
        return Arrays.stream(values())
                .filter(part -> part.name().equalsIgnoreCase(normalized)
                        || Arrays.stream(part.aliases).anyMatch(alias -> alias.equalsIgnoreCase(normalized)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 파트입니다: " + value));
    }
}
