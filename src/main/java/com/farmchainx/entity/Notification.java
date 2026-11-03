package com.farmchainx.entity;

import com.farmchainx.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * In-app notification sent to a user by the system.
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notif_user",     columnList = "user_id"),
        @Index(name = "idx_notif_is_read",  columnList = "is_read"),
        @Index(name = "idx_notif_created",  columnList = "created_at")
    }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType = NotificationType.INFO;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public Notification() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
