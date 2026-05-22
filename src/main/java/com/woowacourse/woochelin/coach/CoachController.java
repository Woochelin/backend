package com.woowacourse.woochelin.coach;

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
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping("/api/v1/coaches")
    public List<CoachCardResponse> coaches(
            @RequestParam(required = false) String part,
            @RequestParam(required = false) String keyword
    ) {
        return coachService.findAll(part, keyword);
    }

    @GetMapping("/api/v1/coaches/{coachId}")
    public CoachDetailResponse coach(@PathVariable Long coachId) {
        return coachService.findDetail(coachId);
    }

    @PostMapping("/api/v1/coaches/{coachId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public CoachReviewResponse createReview(@PathVariable Long coachId, @Valid @RequestBody ReviewRequest request) {
        return coachService.createReview(coachId, request);
    }

    @DeleteMapping("/api/v1/coaches/{coachId}/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long coachId,
            @PathVariable Long reviewId,
            @Valid @RequestBody DeleteReviewRequest request
    ) {
        coachService.deleteReview(coachId, reviewId, request);
    }

    @PatchMapping("/api/v1/coaches/{coachId}/reviews/{reviewId}")
    public CoachReviewResponse updateReview(
            @PathVariable Long coachId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return coachService.updateReview(coachId, reviewId, request);
    }
}
