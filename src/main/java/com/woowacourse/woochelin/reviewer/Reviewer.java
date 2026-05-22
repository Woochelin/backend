package com.woowacourse.woochelin.reviewer;

import com.woowacourse.woochelin.common.Part;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

@Entity
public class Reviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Part part;

    @Column(nullable = false)
    private String profileImageUrl;

    @Column(nullable = false)
    private String slackUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Reviewer() {
    }

    public Reviewer(String name, Part part, String profileImageUrl, String slackUrl) {
        this.name = name;
        this.part = part;
        this.profileImageUrl = profileImageUrl;
        this.slackUrl = slackUrl;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Part getPart() {
        return part;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getSlackUrl() {
        return slackUrl;
    }
}
