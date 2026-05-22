package com.woowacourse.woochelin.common;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchKeywordLogRepository extends JpaRepository<SearchKeywordLog, Long> {

    List<SearchKeywordLog> findTop10ByOrderBySearchCountDescIdAsc();
}
