package com.project.dualaccesscontrol.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rbac_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RbacRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private AccessPolicy policy;
    
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Permission permission;
    
    public enum Permission {
        READ,
        WRITE,
        DELETE,
        SHARE
    }
}
