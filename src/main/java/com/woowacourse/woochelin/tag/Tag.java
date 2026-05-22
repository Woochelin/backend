package com.woowacourse.woochelin.tag;

import com.woowacourse.woochelin.common.TargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"target_type", "name"}))
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(nullable = false)
    private String name;

    protected Tag() {
    }

    public Tag(TargetType targetType, String name) {
        this.targetType = targetType;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public String getName() {
        return name;
    }
}
