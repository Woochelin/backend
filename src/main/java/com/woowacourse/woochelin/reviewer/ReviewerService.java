package com.woowacourse.woochelin.reviewer;

import com.woowacourse.woochelin.common.DeleteReviewRequest;
import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.ReviewRequest;
import com.woowacourse.woochelin.common.SearchLog;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TagStatResponse;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.common.exception.BadRequestException;
import com.woowacourse.woochelin.common.exception.NotFoundException;
import com.woowacourse.woochelin.tag.Tag;
import com.woowacourse.woochelin.tag.TagRepository;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ReviewerService {

    private static final int TOP_TAG_LIMIT = 3;

    private final ReviewerRepository reviewerRepository;
    private final ReviewerReviewRepository reviewerReviewRepository;
    private final TagRepository tagRepository;
    private final SearchLogRepository searchLogRepository;
    private final PasswordEncoder passwordEncoder;

    public ReviewerService(
            ReviewerRepository reviewerRepository,
            ReviewerReviewRepository reviewerReviewRepository,
            TagRepository tagRepository,
            SearchLogRepository searchLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.reviewerRepository = reviewerRepository;
        this.reviewerReviewRepository = reviewerReviewRepository;
        this.tagRepository = tagRepository;
        this.searchLogRepository = searchLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ReviewerCardResponse> findAll(String partValue, String keyword) {
        Part part = Part.from(partValue);
        return findReviewers(part, keyword).stream()
                .map(this::toCardResponse)
                .toList();
    }

    @Transactional
    public ReviewerDetailResponse findDetail(Long reviewerId) {
        Reviewer reviewer = findReviewer(reviewerId);
        searchLogRepository.save(new SearchLog(reviewer.getName(), TargetType.REVIEWER, reviewer.getId()));
        return new ReviewerDetailResponse(
                reviewer.getId(),
                reviewer.getName(),
                reviewer.getPart(),
                reviewer.getProfileImageUrl(),
                reviewer.getSlackUrl(),
                reviewer.getStyle(),
                round(reviewerReviewRepository.averageRating(reviewerId)),
                topTags(reviewerId),
                reviewerReviewRepository.findByReviewerIdAndDeletedAtIsNullOrderByCreatedAtDesc(reviewerId).stream()
                        .map(ReviewerReviewResponse::from)
                        .toList()
        );
    }

    @Transactional
    public ReviewerReviewResponse createReview(Long reviewerId, ReviewRequest request) {
        Reviewer reviewer = findReviewer(reviewerId);
        Set<Tag> tags = validateTags(request.normalizedTagIds());
        ReviewerReview review = new ReviewerReview(
                reviewer,
                request.normalizedNickname(),
                passwordEncoder.encode(request.password()),
                request.rating(),
                request.content(),
                tags
        );
        return ReviewerReviewResponse.from(reviewerReviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewerId, Long reviewId, DeleteReviewRequest request) {
        ReviewerReview review = reviewerReviewRepository.findByIdAndReviewerIdAndDeletedAtIsNull(reviewId, reviewerId)
                .orElseThrow(() -> new NotFoundException("리뷰어 리뷰를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), review.getPassword())) {
            throw new BadRequestException("삭제용 비밀번호가 일치하지 않습니다.");
        }
        review.delete();
    }

    @Transactional
    public ReviewerReviewResponse updateReview(Long reviewerId, Long reviewId, ReviewRequest request) {
        ReviewerReview review = reviewerReviewRepository.findByIdAndReviewerIdAndDeletedAtIsNull(reviewId, reviewerId)
                .orElseThrow(() -> new NotFoundException("리뷰어 리뷰를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), review.getPassword())) {
            throw new BadRequestException("삭제용 비밀번호가 일치하지 않습니다.");
        }
        review.update(
                request.normalizedNickname(),
                request.rating(),
                request.content(),
                validateTags(request.normalizedTagIds())
        );
        return ReviewerReviewResponse.from(review);
    }

    public Reviewer findReviewer(Long reviewerId) {
        return reviewerRepository.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("리뷰어를 찾을 수 없습니다."));
    }

    public ReviewerCardResponse toCardResponse(Reviewer reviewer) {
        return new ReviewerCardResponse(
                reviewer.getId(),
                reviewer.getName(),
                reviewer.getPart(),
                reviewer.getProfileImageUrl(),
                reviewer.getSlackUrl(),
                reviewer.getStyle(),
                round(reviewerReviewRepository.averageRating(reviewer.getId())),
                topTags(reviewer.getId())
        );
    }

    private List<Reviewer> findReviewers(Part part, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (part != null && hasKeyword) {
            return reviewerRepository.findByPartAndNameContainingIgnoreCase(part, keyword.trim());
        }
        if (part != null) {
            return reviewerRepository.findByPart(part);
        }
        if (hasKeyword) {
            return reviewerRepository.findByNameContainingIgnoreCase(keyword.trim());
        }
        return reviewerRepository.findAll();
    }

    private List<TagStatResponse> topTags(Long reviewerId) {
        return reviewerReviewRepository.topTags(reviewerId).stream()
                .limit(TOP_TAG_LIMIT)
                .map(TagStatResponse::from)
                .toList();
    }

    private Set<Tag> validateTags(List<Long> tagIds) {
        List<Tag> tags = tagRepository.findByIdInAndTargetType(tagIds, TargetType.REVIEWER);
        if (tags.size() != tagIds.size()) {
            throw new BadRequestException("리뷰어 리뷰에 사용할 수 없는 태그가 포함되어 있습니다.");
        }
        return new LinkedHashSet<>(tags);
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
