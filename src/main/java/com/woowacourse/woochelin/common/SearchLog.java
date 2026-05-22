package com.woowacourse.woochelin.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected SearchLog() {
    }

    public SearchLog(String keyword, TargetType targetType, Long targetId) {
        this.keyword = keyword;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
