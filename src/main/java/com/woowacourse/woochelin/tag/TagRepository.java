package com.woowacourse.woochelin.tag;

import com.woowacourse.woochelin.common.TargetType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByTargetTypeOrderById(TargetType targetType);

    List<Tag> findByIdInAndTargetType(Collection<Long> ids, TargetType targetType);

    boolean existsByTargetTypeAndName(TargetType targetType, String name);
}
