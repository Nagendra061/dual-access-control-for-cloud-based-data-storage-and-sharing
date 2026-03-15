-- Database Schema for Dual Access Control System

-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Roles Table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Roles Mapping (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Attributes Table (for ABAC)
CREATE TABLE attributes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attribute_name VARCHAR(50) UNIQUE NOT NULL,
    attribute_type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Attributes (Many-to-Many with values)
CREATE TABLE user_attributes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    attribute_value VARCHAR(255) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_id) REFERENCES attributes(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_attribute (user_id, attribute_id)
);

-- Files Table
CREATE TABLE files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    encrypted BOOLEAN DEFAULT TRUE,
    encryption_key_hash VARCHAR(255),
    owner_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_owner (owner_id),
    INDEX idx_file_name (file_name)
);

-- Access Policies Table
CREATE TABLE access_policies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_name VARCHAR(100) NOT NULL,
    file_id BIGINT NOT NULL,
    policy_type VARCHAR(20) NOT NULL, -- 'RBAC' or 'ABAC'
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_file (file_id)
);

-- RBAC Policy Rules
CREATE TABLE rbac_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    permission VARCHAR(50) NOT NULL, -- 'READ', 'WRITE', 'DELETE', 'SHARE'
    FOREIGN KEY (policy_id) REFERENCES access_policies(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_policy_role (policy_id, role_id, permission)
);

-- ABAC Policy Rules
CREATE TABLE abac_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    operator VARCHAR(20) NOT NULL, -- 'EQUALS', 'NOT_EQUALS', 'CONTAINS', 'GREATER_THAN', etc.
    required_value VARCHAR(255) NOT NULL,
    permission VARCHAR(50) NOT NULL, -- 'READ', 'WRITE', 'DELETE', 'SHARE'
    FOREIGN KEY (policy_id) REFERENCES access_policies(id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_id) REFERENCES attributes(id) ON DELETE CASCADE
);

-- Access Logs Table
CREATE TABLE access_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'VIEW', 'DOWNLOAD', 'UPLOAD', 'DELETE', 'SHARE'
    access_granted BOOLEAN NOT NULL,
    denial_reason VARCHAR(255),
    ip_address VARCHAR(45),
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    INDEX idx_user_access (user_id, accessed_at),
    INDEX idx_file_access (file_id, accessed_at)
);

-- Shared Links Table
CREATE TABLE shared_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL,
    shared_by BIGINT NOT NULL,
    share_token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP,
    max_downloads INT DEFAULT -1, -- -1 means unlimited
    download_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (shared_by) REFERENCES users(id),
    INDEX idx_token (share_token)
);

-- Insert Default Roles
INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'System Administrator with full access'),
('DATA_OWNER', 'User who can upload and manage their own files'),
('DATA_USER', 'Regular user with read access to shared files'),
('GUEST', 'Limited access guest user');

-- Insert Default Attributes for ABAC
INSERT INTO attributes (attribute_name, attribute_type, description) VALUES
('department', 'STRING', 'User department'),
('clearance_level', 'INTEGER', 'Security clearance level (1-5)'),
('location', 'STRING', 'User location'),
('project', 'STRING', 'Assigned project'),
('job_title', 'STRING', 'User job title');
