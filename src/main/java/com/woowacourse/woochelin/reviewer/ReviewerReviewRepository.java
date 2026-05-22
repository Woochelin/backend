package com.woowacourse.woochelin.reviewer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewerReviewRepository extends JpaRepository<ReviewerReview, Long> {

    List<ReviewerReview> findByReviewerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long reviewerId);

    Optional<ReviewerReview> findByIdAndReviewerIdAndDeletedAtIsNull(Long id, Long reviewerId);

    List<ReviewerReview> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("select coalesce(avg(r.rating), 0) from ReviewerReview r where r.reviewer.id = :reviewerId and r.deletedAt is null")
    double averageRating(@Param("reviewerId") Long reviewerId);

    @Query("select t.id, t.name, count(t.id) from ReviewerReview r join r.tags t "
            + "where r.reviewer.id = :reviewerId and r.deletedAt is null group by t.id, t.name "
            + "order by count(t.id) desc, t.id asc")
    List<Object[]> topTags(@Param("reviewerId") Long reviewerId);

    @Query("select t.id, t.name, count(t.id) from ReviewerReview r join r.tags t "
            + "where r.reviewer.id in :reviewerIds and r.deletedAt is null group by t.id, t.name "
            + "order by count(t.id) desc, t.id asc")
    List<Object[]> topTagsForReviewers(@Param("reviewerIds") Collection<Long> reviewerIds);
}
