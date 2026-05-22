package com.woowacourse.woochelin.reviewer;

import com.woowacourse.woochelin.common.Part;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewerRepository extends JpaRepository<Reviewer, Long> {

    List<Reviewer> findByPart(Part part);

    List<Reviewer> findByNameContainingIgnoreCase(String keyword);

    List<Reviewer> findByPartAndNameContainingIgnoreCase(Part part, String keyword);

    java.util.Optional<Reviewer> findByName(String name);

    boolean existsByName(String name);
}
