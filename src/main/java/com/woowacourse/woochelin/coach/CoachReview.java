package com.woowacourse.woochelin.coach;

import com.woowacourse.woochelin.tag.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class CoachReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Coach coach;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int rating;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToMany
    @JoinTable(
            name = "coach_review_tag_map",
            joinColumns = @JoinColumn(name = "review_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    protected CoachReview() {
    }

    public CoachReview(Coach coach, String nickname, String password, int rating, String content, Set<Tag> tags) {
        this(coach, nickname, password, rating, content, tags, null);
    }

    public CoachReview(
            Coach coach,
            String nickname,
            String password,
            int rating,
            String content,
            Set<Tag> tags,
            LocalDateTime createdAt
    ) {
        this.coach = coach;
        this.nickname = nickname;
        this.password = password;
        this.rating = rating;
        this.content = content;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void delete() {
        deletedAt = LocalDateTime.now();
    }

    public void update(String nickname, int rating, String content, Set<Tag> tags) {
        this.nickname = nickname;
        this.rating = rating;
        this.content = content;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public Coach getCoach() {
        return coach;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
