package com.project.dualaccesscontrol.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;
    
    @Column(nullable = false, length = 50)
    private String action; // VIEW, DOWNLOAD, UPLOAD, DELETE, SHARE
    
    @Column(name = "access_granted", nullable = false)
    private Boolean accessGranted;
    
    @Column(name = "denial_reason")
    private String denialReason;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "accessed_at")
    private LocalDateTime accessedAt;
    
    @PrePersist
    protected void onCreate() {
        accessedAt = LocalDateTime.now();
    }
}
