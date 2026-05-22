package com.woowacourse.woochelin.coach;

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
public class CoachService {

    private static final int TOP_TAG_LIMIT = 3;

    private final CoachRepository coachRepository;
    private final CoachReviewRepository coachReviewRepository;
    private final TagRepository tagRepository;
    private final SearchLogRepository searchLogRepository;
    private final PasswordEncoder passwordEncoder;

    public CoachService(
            CoachRepository coachRepository,
            CoachReviewRepository coachReviewRepository,
            TagRepository tagRepository,
            SearchLogRepository searchLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.coachRepository = coachRepository;
        this.coachReviewRepository = coachReviewRepository;
        this.tagRepository = tagRepository;
        this.searchLogRepository = searchLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<CoachCardResponse> findAll(String partValue, String keyword) {
        Part part = Part.from(partValue);
        List<Coach> coaches = findCoaches(part, keyword);
        return coaches.stream()
                .map(this::toCardResponse)
                .toList();
    }

    @Transactional
    public CoachDetailResponse findDetail(Long coachId) {
        Coach coach = findCoach(coachId);
        searchLogRepository.save(new SearchLog(coach.getName(), TargetType.COACH, coach.getId()));
        return new CoachDetailResponse(
                coach.getId(),
                coach.getName(),
                coach.getPart(),
                coach.getProfileImageUrl(),
                coach.getSlackUrl(),
                coach.getBotId(),
                coach.getBotDescription(),
                round(coachReviewRepository.averageRating(coachId)),
                topTags(coachId),
                coachReviewRepository.findByCoachIdAndDeletedAtIsNullOrderByCreatedAtDesc(coachId).stream()
                        .map(CoachReviewResponse::from)
                        .toList()
        );
    }

    @Transactional
    public CoachReviewResponse createReview(Long coachId, ReviewRequest request) {
        Coach coach = findCoach(coachId);
        Set<Tag> tags = validateTags(request.normalizedTagIds());
        CoachReview review = new CoachReview(
                coach,
                request.normalizedNickname(),
                passwordEncoder.encode(request.password()),
                request.rating(),
                request.content(),
                tags
        );
        return CoachReviewResponse.from(coachReviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long coachId, Long reviewId, DeleteReviewRequest request) {
        CoachReview review = coachReviewRepository.findByIdAndCoachIdAndDeletedAtIsNull(reviewId, coachId)
                .orElseThrow(() -> new NotFoundException("코치 리뷰를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), review.getPassword())) {
            throw new BadRequestException("삭제용 비밀번호가 일치하지 않습니다.");
        }
        review.delete();
    }

    @Transactional
    public CoachReviewResponse updateReview(Long coachId, Long reviewId, ReviewRequest request) {
        CoachReview review = coachReviewRepository.findByIdAndCoachIdAndDeletedAtIsNull(reviewId, coachId)
                .orElseThrow(() -> new NotFoundException("코치 리뷰를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.password(), review.getPassword())) {
            throw new BadRequestException("삭제용 비밀번호가 일치하지 않습니다.");
        }
        review.update(
                request.normalizedNickname(),
                request.rating(),
                request.content(),
                validateTags(request.normalizedTagIds())
        );
        return CoachReviewResponse.from(review);
    }

    public Coach findCoach(Long coachId) {
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new NotFoundException("코치를 찾을 수 없습니다."));
    }

    public CoachCardResponse toCardResponse(Coach coach) {
        return new CoachCardResponse(
                coach.getId(),
                coach.getName(),
                coach.getPart(),
                coach.getProfileImageUrl(),
                coach.getSlackUrl(),
                coach.getBotId(),
                coach.getBotDescription(),
                round(coachReviewRepository.averageRating(coach.getId())),
                topTags(coach.getId())
        );
    }

    private List<Coach> findCoaches(Part part, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (part != null && hasKeyword) {
            return coachRepository.findByPartAndNameContainingIgnoreCase(part, keyword.trim());
        }
        if (part != null) {
            return coachRepository.findByPart(part);
        }
        if (hasKeyword) {
            return coachRepository.findByNameContainingIgnoreCase(keyword.trim());
        }
        return coachRepository.findAll();
    }

    private List<TagStatResponse> topTags(Long coachId) {
        return coachReviewRepository.topTags(coachId).stream()
                .limit(TOP_TAG_LIMIT)
                .map(TagStatResponse::from)
                .toList();
    }

    private Set<Tag> validateTags(List<Long> tagIds) {
        List<Tag> tags = tagRepository.findByIdInAndTargetType(tagIds, TargetType.COACH);
        if (tags.size() != tagIds.size()) {
            throw new BadRequestException("코치 리뷰에 사용할 수 없는 태그가 포함되어 있습니다.");
        }
        return new LinkedHashSet<>(tags);
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
