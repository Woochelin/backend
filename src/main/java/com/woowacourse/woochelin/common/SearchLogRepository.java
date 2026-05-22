package com.woowacourse.woochelin.common;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query(value = "select target_id, count(id) from search_log "
            + "where target_type = :targetType and created_at >= :from "
            + "group by target_id order by count(id) desc, target_id asc limit :limit", nativeQuery = true)
    List<Object[]> ranking(@Param("targetType") String targetType, @Param("from") LocalDateTime from, @Param("limit") int limit);
}
