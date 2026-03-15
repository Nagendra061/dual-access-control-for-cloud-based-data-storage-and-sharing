package com.project.dualaccesscontrol.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "abac_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbacRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private AccessPolicy policy;
    
    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Operator operator;
    
    @Column(name = "required_value", nullable = false)
    private String requiredValue;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Permission permission;
    
    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN_OR_EQUAL
    }
    
    public enum Permission {
        READ,
        WRITE,
        DELETE,
        SHARE
    }
}
