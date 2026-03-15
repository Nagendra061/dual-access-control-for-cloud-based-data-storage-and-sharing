package com.project.dualaccesscontrol.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "attribute_name", nullable = false, unique = true, length = 50)
    private String attributeName;
    
    @Column(name = "attribute_type", nullable = false, length = 50)
    private String attributeType; // STRING, INTEGER, BOOLEAN, DATE
    
    @Column(length = 255)
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
