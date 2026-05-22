package com.woowacourse.woochelin.coach;

import com.woowacourse.woochelin.common.Part;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {

    List<Coach> findByPart(Part part);

    List<Coach> findByNameContainingIgnoreCase(String keyword);

    List<Coach> findByPartAndNameContainingIgnoreCase(Part part, String keyword);

    Optional<Coach> findByBotId(String botId);

    boolean existsByName(String name);
}
