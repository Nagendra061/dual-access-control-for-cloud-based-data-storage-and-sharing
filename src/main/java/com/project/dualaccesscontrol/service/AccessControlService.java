package com.project.dualaccesscontrol.service;

import com.project.dualaccesscontrol.model.*;
import com.project.dualaccesscontrol.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dual Access Control Service
 * Implements both RBAC (Role-Based Access Control) and ABAC (Attribute-Based Access Control)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessControlService {
    
    private final AccessPolicyRepository accessPolicyRepository;
    private final UserAttributeRepository userAttributeRepository;
    private final AccessLogRepository accessLogRepository;
    
    /**
     * Check if user has access to a file based on dual access control
     * Both RBAC and ABAC must pass for access to be granted
     */
    @Transactional
    public AccessDecision checkAccess(User user, FileEntity file, String action) {
        log.debug("Checking access for user: {} on file: {} for action: {}", 
                  user.getUsername(), file.getFileName(), action);
        
        // Get all active policies for the file
        List<AccessPolicy> policies = accessPolicyRepository.findByFileAndIsActiveTrue(file);
        
        if (policies.isEmpty()) {
            // No policies defined - check if user is the owner
            if (file.getOwner().getId().equals(user.getId())) {
                return AccessDecision.granted("User is file owner");
            }
            return AccessDecision.denied("No access policies defined and user is not owner");
        }
        
        boolean rbacPassed = false;
        boolean abacPassed = false;
        String rbacReason = "No RBAC policy found";
        String abacReason = "No ABAC policy found";
        
        for (AccessPolicy policy : policies) {
            if (policy.getPolicyType() == AccessPolicy.PolicyType.RBAC || 
                policy.getPolicyType() == AccessPolicy.PolicyType.DUAL) {
                AccessDecision rbacDecision = checkRbacAccess(user, policy, action);
                if (rbacDecision.isGranted()) {
                    rbacPassed = true;
                    rbacReason = rbacDecision.getReason();
                } else {
                    rbacReason = rbacDecision.getReason();
                }
            }
            
            if (policy.getPolicyType() == AccessPolicy.PolicyType.ABAC || 
                policy.getPolicyType() == AccessPolicy.PolicyType.DUAL) {
                AccessDecision abacDecision = checkAbacAccess(user, policy, action);
                if (abacDecision.isGranted()) {
                    abacPassed = true;
                    abacReason = abacDecision.getReason();
                } else {
                    abacReason = abacDecision.getReason();
                }
            }
        }
        
        // Dual access control: Both RBAC and ABAC must pass
        boolean hasRbacPolicy = policies.stream()
            .anyMatch(p -> p.getPolicyType() == AccessPolicy.PolicyType.RBAC || 
                          p.getPolicyType() == AccessPolicy.PolicyType.DUAL);
        boolean hasAbacPolicy = policies.stream()
            .anyMatch(p -> p.getPolicyType() == AccessPolicy.PolicyType.ABAC || 
                          p.getPolicyType() == AccessPolicy.PolicyType.DUAL);
        
        AccessDecision decision;
        if (hasRbacPolicy && hasAbacPolicy) {
            // Dual control: both must pass
            if (rbacPassed && abacPassed) {
                decision = AccessDecision.granted("RBAC and ABAC both passed");
            } else {
                String reason = String.format("RBAC: %s, ABAC: %s", rbacReason, abacReason);
                decision = AccessDecision.denied(reason);
            }
        } else if (hasRbacPolicy) {
            decision = rbacPassed ? 
                AccessDecision.granted(rbacReason) : 
                AccessDecision.denied(rbacReason);
        } else if (hasAbacPolicy) {
            decision = abacPassed ? 
                AccessDecision.granted(abacReason) : 
                AccessDecision.denied(abacReason);
        } else {
            decision = AccessDecision.denied("No valid policies found");
        }
        
        // Log the access attempt
        logAccess(user, file, action, decision);
        
        return decision;
    }
    
    /**
     * Check RBAC - Role-Based Access Control
     */
    private AccessDecision checkRbacAccess(User user, AccessPolicy policy, String action) {
        Set<RbacRule> rbacRules = policy.getRbacRules();
        
        if (rbacRules.isEmpty()) {
            return AccessDecision.denied("No RBAC rules defined");
        }
        
        Set<String> userRoles = user.getRoles().stream()
            .map(Role::getRoleName)
            .collect(Collectors.toSet());
        
        for (RbacRule rule : rbacRules) {
            String roleName = rule.getRole().getRoleName();
            String permission = rule.getPermission().name();
            
            if (userRoles.contains(roleName) && permission.equalsIgnoreCase(action)) {
                log.debug("RBAC passed: User has role {} with permission {}", roleName, permission);
                return AccessDecision.granted("RBAC: User has required role and permission");
            }
        }
        
        return AccessDecision.denied("RBAC: User does not have required role or permission");
    }
    
    /**
     * Check ABAC - Attribute-Based Access Control
     */
    private AccessDecision checkAbacAccess(User user, AccessPolicy policy, String action) {
        Set<AbacRule> abacRules = policy.getAbacRules();
        
        if (abacRules.isEmpty()) {
            return AccessDecision.denied("No ABAC rules defined");
        }
        
        // Get user attributes
        List<UserAttribute> userAttributes = userAttributeRepository.findByUser(user);
        
        for (AbacRule rule : abacRules) {
            String permission = rule.getPermission().name();
            
            if (!permission.equalsIgnoreCase(action)) {
                continue;
            }
            
            // Find matching user attribute
            UserAttribute userAttr = userAttributes.stream()
                .filter(ua -> ua.getAttribute().getId().equals(rule.getAttribute().getId()))
                .findFirst()
                .orElse(null);
            
            if (userAttr == null) {
                log.debug("ABAC failed: User does not have required attribute: {}", 
                         rule.getAttribute().getAttributeName());
                continue;
            }
            
            // Evaluate the attribute condition
            boolean conditionMet = evaluateCondition(
                userAttr.getAttributeValue(),
                rule.getOperator(),
                rule.getRequiredValue(),
                rule.getAttribute().getAttributeType()
            );
            
            if (conditionMet) {
                log.debug("ABAC passed: Attribute {} meets condition", 
                         rule.getAttribute().getAttributeName());
                return AccessDecision.granted("ABAC: User attributes meet policy requirements");
            }
        }
        
        return AccessDecision.denied("ABAC: User attributes do not meet policy requirements");
    }
    
    /**
     * Evaluate attribute condition based on operator
     */
    private boolean evaluateCondition(String userValue, AbacRule.Operator operator, 
                                     String requiredValue, String attributeType) {
        try {
            switch (operator) {
                case EQUALS:
                    return userValue.equals(requiredValue);
                    
                case NOT_EQUALS:
                    return !userValue.equals(requiredValue);
                    
                case CONTAINS:
                    return userValue.toLowerCase().contains(requiredValue.toLowerCase());
                    
                case NOT_CONTAINS:
                    return !userValue.toLowerCase().contains(requiredValue.toLowerCase());
                    
                case GREATER_THAN:
                    if ("INTEGER".equals(attributeType)) {
                        return Integer.parseInt(userValue) > Integer.parseInt(requiredValue);
                    }
                    return userValue.compareTo(requiredValue) > 0;
                    
                case LESS_THAN:
                    if ("INTEGER".equals(attributeType)) {
                        return Integer.parseInt(userValue) < Integer.parseInt(requiredValue);
                    }
                    return userValue.compareTo(requiredValue) < 0;
                    
                case GREATER_THAN_OR_EQUAL:
                    if ("INTEGER".equals(attributeType)) {
                        return Integer.parseInt(userValue) >= Integer.parseInt(requiredValue);
                    }
                    return userValue.compareTo(requiredValue) >= 0;
                    
                case LESS_THAN_OR_EQUAL:
                    if ("INTEGER".equals(attributeType)) {
                        return Integer.parseInt(userValue) <= Integer.parseInt(requiredValue);
                    }
                    return userValue.compareTo(requiredValue) <= 0;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating condition: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Log access attempt
     */
    private void logAccess(User user, FileEntity file, String action, AccessDecision decision) {
        AccessLog accessLog = AccessLog.builder()
            .user(user)
            .file(file)
            .action(action)
            .accessGranted(decision.isGranted())
            .denialReason(decision.isGranted() ? null : decision.getReason())
            .build();
        
        accessLogRepository.save(accessLog);
        log.info("Access logged: User={}, File={}, Action={}, Granted={}", 
                 user.getUsername(), file.getFileName(), action, decision.isGranted());
    }
    
    /**
     * Access Decision Result
     */
    public static class AccessDecision {
        private final boolean granted;
        private final String reason;
        
        private AccessDecision(boolean granted, String reason) {
            this.granted = granted;
            this.reason = reason;
        }
        
        public static AccessDecision granted(String reason) {
            return new AccessDecision(true, reason);
        }
        
        public static AccessDecision denied(String reason) {
            return new AccessDecision(false, reason);
        }
        
        public boolean isGranted() {
            return granted;
        }
        
        public String getReason() {
            return reason;
        }
    }
}
