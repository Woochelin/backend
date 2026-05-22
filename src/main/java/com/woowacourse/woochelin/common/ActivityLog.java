package com.woowacourse.woochelin.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String actorSuffix;

    @Column(nullable = false)
    private String target;

    @Column(nullable = false)
    private String action;

    private String highlight;

    private String highlightType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ActivityLog() {
    }

    public ActivityLog(
            String type,
            String color,
            String actor,
            String actorSuffix,
            String target,
            String action,
            String highlight,
            String highlightType,
            LocalDateTime createdAt
    ) {
        this.type = type;
        this.color = color;
        this.actor = actor;
        this.actorSuffix = actorSuffix;
        this.target = target;
        this.action = action;
        this.highlight = highlight;
        this.highlightType = highlightType;
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public String getActor() {
        return actor;
    }

    public String getActorSuffix() {
        return actorSuffix;
    }

    public String getTarget() {
        return target;
    }

    public String getAction() {
        return action;
    }

    public String getHighlight() {
        return highlight;
    }

    public String getHighlightType() {
        return highlightType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
