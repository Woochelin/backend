package com.woowacourse.woochelin.reviewer;

import com.woowacourse.woochelin.common.DeleteReviewRequest;
import com.woowacourse.woochelin.common.ReviewRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewerController {

    private final ReviewerService reviewerService;

    public ReviewerController(ReviewerService reviewerService) {
        this.reviewerService = reviewerService;
    }

    @GetMapping("/api/v1/reviewers")
    public List<ReviewerCardResponse> reviewers(
            @RequestParam(required = false) String part,
            @RequestParam(required = false) String keyword
    ) {
        return reviewerService.findAll(part, keyword);
    }

    @GetMapping("/api/v1/reviewers/{reviewerId}")
    public ReviewerDetailResponse reviewer(@PathVariable Long reviewerId) {
        return reviewerService.findDetail(reviewerId);
    }

    @PostMapping("/api/v1/reviewers/{reviewerId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewerReviewResponse createReview(@PathVariable Long reviewerId, @Valid @RequestBody ReviewRequest request) {
        return reviewerService.createReview(reviewerId, request);
    }

    @DeleteMapping("/api/v1/reviewers/{reviewerId}/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long reviewerId,
            @PathVariable Long reviewId,
            @Valid @RequestBody DeleteReviewRequest request
    ) {
        reviewerService.deleteReview(reviewerId, reviewId, request);
    }

    @PatchMapping("/api/v1/reviewers/{reviewerId}/reviews/{reviewId}")
    public ReviewerReviewResponse updateReview(
            @PathVariable Long reviewerId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return reviewerService.updateReview(reviewerId, reviewId, request);
    }
}
