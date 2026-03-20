package vn.bxh.jobhunter.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    
    @Column(columnDefinition = "MEDIUMTEXT")
    private String message;
    
    private String senderName; // E.g., "Admin", "HR - Mobifone"
    
    private boolean isRead;
    
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void handleBeforeCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.isRead = false;
    }
}
