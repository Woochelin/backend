package com.woowacourse.woochelin.coach;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoachReviewRepository extends JpaRepository<CoachReview, Long> {

    List<CoachReview> findByCoachIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long coachId);

    Optional<CoachReview> findByIdAndCoachIdAndDeletedAtIsNull(Long id, Long coachId);

    List<CoachReview> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("select coalesce(avg(r.rating), 0) from CoachReview r where r.coach.id = :coachId and r.deletedAt is null")
    double averageRating(@Param("coachId") Long coachId);

    @Query("select t.id, t.name, count(t.id) from CoachReview r join r.tags t "
            + "where r.coach.id = :coachId and r.deletedAt is null group by t.id, t.name "
            + "order by count(t.id) desc, t.id asc")
    List<Object[]> topTags(@Param("coachId") Long coachId);

    @Query("select t.id, t.name, count(t.id) from CoachReview r join r.tags t "
            + "where r.coach.id in :coachIds and r.deletedAt is null group by t.id, t.name "
            + "order by count(t.id) desc, t.id asc")
    List<Object[]> topTagsForCoaches(@Param("coachIds") Collection<Long> coachIds);
}
