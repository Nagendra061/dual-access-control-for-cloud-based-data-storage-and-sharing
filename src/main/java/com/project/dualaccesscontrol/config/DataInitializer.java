package com.project.dualaccesscontrol.config;

import com.project.dualaccesscontrol.model.Attribute;
import com.project.dualaccesscontrol.model.Role;
import com.project.dualaccesscontrol.model.User;
import com.project.dualaccesscontrol.repository.AttributeRepository;
import com.project.dualaccesscontrol.repository.RoleRepository;
import com.project.dualaccesscontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Initializer - Creates default roles, attributes, and admin user
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final AttributeRepository attributeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAttributes();
        initializeAdminUser();
    }
    
    private void initializeRoles() {
        String[] roleNames = {"ADMIN", "DATA_OWNER", "DATA_USER", "GUEST"};
        String[] descriptions = {
            "System Administrator with full access",
            "User who can upload and manage their own files",
            "Regular user with read access to shared files",
            "Limited access guest user"
        };
        
        for (int i = 0; i < roleNames.length; i++) {
            if (roleRepository.findByRoleName(roleNames[i]).isEmpty()) {
                Role role = Role.builder()
                    .roleName(roleNames[i])
                    .description(descriptions[i])
                    .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleNames[i]);
            }
        }
    }
    
    private void initializeAttributes() {
        String[][] attributes = {
            {"department", "STRING", "User department"},
            {"clearance_level", "INTEGER", "Security clearance level (1-5)"},
            {"location", "STRING", "User location"},
            {"project", "STRING", "Assigned project"},
            {"job_title", "STRING", "User job title"}
        };
        
        for (String[] attr : attributes) {
            if (attributeRepository.findByAttributeName(attr[0]).isEmpty()) {
                Attribute attribute = Attribute.builder()
                    .attributeName(attr[0])
                    .attributeType(attr[1])
                    .description(attr[2])
                    .build();
                attributeRepository.save(attribute);
                log.info("Created attribute: {}", attr[0]);
            }
        }
    }
    
    private void initializeAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                .username("admin")
                .email("admin@dualaccesscontrol.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .isActive(true)
                .build();
            
            // Assign ADMIN role
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            log.info("Created default admin user - username: admin, password: admin123");
        }
    }
}
