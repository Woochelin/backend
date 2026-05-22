package com.woowacourse.woochelin.reviewer;

import com.woowacourse.woochelin.common.ReviewTagResponse;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewerReviewResponse(
        Long id,
        String nickname,
        int rating,
        String content,
        List<ReviewTagResponse> tags,
        LocalDateTime createdAt
) {

    public static ReviewerReviewResponse from(ReviewerReview review) {
        return new ReviewerReviewResponse(
                review.getId(),
                review.getNickname(),
                review.getRating(),
                review.getContent(),
                review.getTags().stream().map(ReviewTagResponse::from).toList(),
                review.getCreatedAt()
        );
    }
}
