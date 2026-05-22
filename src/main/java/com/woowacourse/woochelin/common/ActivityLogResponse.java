package com.woowacourse.woochelin.common;

import java.time.format.DateTimeFormatter;

/**
 * 프론트엔드 홈 화면의 "최신 기록" 피드에 사용되는 응답 DTO.
 * 필드명은 프론트엔드가 기대하는 키와 1:1 매핑된다.
 *
 * <p>엔티티 → 프론트 필드 매핑:
 * <ul>
 *   <li>color       → dot        (타임라인 점 색상)</li>
 *   <li>actorSuffix → action     (actor 뒤에 오는 조사/동사, 예: "님이")</li>
 *   <li>action      → tail       (target 뒤에 오는 서술어, 예: "코치에게 리뷰를 남겼습니다")</li>
 *   <li>highlight   → extra      (별점 문자열 또는 태그명)</li>
 *   <li>highlightType → extraType (extra 값의 표시 타입: "rating" | "tag")</li>
 * </ul>
 */
public record ActivityLogResponse(
        Long id,
        String type,
        String dot,
        String actor,
        String action,
        String target,
        String tail,
        String extra,
        String extraType,
        String time
) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getType(),
                log.getColor(),
                log.getActor(),
                log.getActorSuffix(),
                log.getTarget(),
                log.getAction(),
                log.getHighlight(),
                log.getHighlightType(),
                log.getCreatedAt().format(TIME_FORMATTER)
        );
    }
}
