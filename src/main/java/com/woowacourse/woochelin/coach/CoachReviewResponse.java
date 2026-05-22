package com.woowacourse.woochelin.coach;

import com.woowacourse.woochelin.common.ReviewTagResponse;
import java.time.LocalDateTime;
import java.util.List;

public record CoachReviewResponse(
        Long id,
        String nickname,
        int rating,
        String content,
        List<ReviewTagResponse> tags,
        LocalDateTime createdAt
) {

    public static CoachReviewResponse from(CoachReview review) {
        return new CoachReviewResponse(
                review.getId(),
                review.getNickname(),
                review.getRating(),
                review.getContent(),
                review.getTags().stream().map(ReviewTagResponse::from).toList(),
                review.getCreatedAt()
        );
    }
}
