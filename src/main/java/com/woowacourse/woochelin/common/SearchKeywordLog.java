package com.woowacourse.woochelin.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class SearchKeywordLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    private String badge;

    private String trendType;

    @Column(nullable = false)
    private int searchCount;

    @Column(nullable = false)
    private LocalDateTime rankedAt;

    protected SearchKeywordLog() {
    }

    public SearchKeywordLog(String keyword, String badge, String trendType, int searchCount, LocalDateTime rankedAt) {
        this.keyword = keyword;
        this.badge = badge;
        this.trendType = trendType;
        this.searchCount = searchCount;
        this.rankedAt = rankedAt;
    }

    public Long getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getBadge() {
        return badge;
    }

    public String getTrendType() {
        return trendType;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public LocalDateTime getRankedAt() {
        return rankedAt;
    }
}
