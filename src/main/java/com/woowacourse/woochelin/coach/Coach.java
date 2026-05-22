package com.woowacourse.woochelin.coach;

import com.woowacourse.woochelin.common.Part;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

@Entity
public class Coach {

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

    @Column(nullable = false, unique = true)
    private String botId;

    @Column(nullable = false)
    private String botDescription;

    @Lob
    @Column(nullable = false)
    private String personaPrompt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Coach() {
    }

    public Coach(String name, Part part, String profileImageUrl, String slackUrl, String botId, String personaPrompt) {
        this(name, part, profileImageUrl, slackUrl, botId, "따뜻한 위로와 명언", personaPrompt);
    }

    public Coach(
            String name,
            Part part,
            String profileImageUrl,
            String slackUrl,
            String botId,
            String botDescription,
            String personaPrompt
    ) {
        this.name = name;
        this.part = part;
        this.profileImageUrl = profileImageUrl;
        this.slackUrl = slackUrl;
        this.botId = botId;
        this.botDescription = botDescription;
        this.personaPrompt = personaPrompt;
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

    public String getBotId() {
        return botId;
    }

    public String getBotDescription() {
        return botDescription;
    }

    public String getPersonaPrompt() {
        return personaPrompt;
    }
}
