package com.group4.expense_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 'user_id' có thể là NULL
    @OnDelete(action = OnDeleteAction.SET_NULL) // Phù hợp với 'ON DELETE SET NULL'
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(name = "target_entity", length = 100)
    private String targetEntity;

    @Column(name = "target_id")
    private Integer targetId;

    @Lob
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}