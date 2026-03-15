# Dual Access Control for Cloud-Based Data Storage and Sharing

## 📋 Project Overview

This is a comprehensive final year project implementing a **Dual Access Control System** for cloud-based data storage. The system combines both **RBAC (Role-Based Access Control)** and **ABAC (Attribute-Based Access Control)** to provide enhanced security for file storage and sharing.

## 🎯 Key Features

1. **Dual Access Control**
   - RBAC: Role-based permissions (Admin, Data Owner, Data User, Guest)
   - ABAC: Attribute-based policies (department, clearance level, location, etc.)
   - Combined enforcement: Both policies must pass for access

2. **Strong Encryption**
   - AES-256 encryption for all files
   - Secure key management
   - Encrypted storage

3. **User Management**
   - JWT-based authentication
   - Role assignment
   - Attribute management

4. **File Operations**
   - Secure upload with encryption
   - Download with decryption
   - Access control verification
   - File sharing capabilities

5. **Audit & Logging**
   - Complete access logs
   - Success/failure tracking
   - Security event monitoring

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Client Layer                     │
│         (REST API / Postman / Frontend)             │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│              Security Layer (JWT)                   │
│          Authentication & Authorization             │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│              Controller Layer                       │
│   AuthController | FileController | AdminController │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│               Service Layer                         │
│  AccessControlService | EncryptionService |         │
│  FileStorageService                                 │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│           Repository Layer (JPA)                    │
│   UserRepo | FileRepo | PolicyRepo | LogRepo        │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│              Database (H2/MySQL)                    │
└─────────────────────────────────────────────────────┘
```

## 🔧 Technology Stack

- **Backend Framework**: Spring Boot 3.2.0
- **Security**: Spring Security + JWT
- **Database**: H2 (Development) / MySQL (Production)
- **Encryption**: Java Cryptography Extension (JCE) - AES-256
- **Build Tool**: Maven
- **Java Version**: 17

## 📦 Project Structure

```
dual-access-control/
├── src/main/java/com/project/dualaccesscontrol/
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfig.java
│   │   └── DataInitializer.java
│   ├── controller/          # REST Controllers
│   │   ├── AuthController.java
│   │   └── FileController.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── AuthResponse.java
│   ├── model/               # Entity Models
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Attribute.java
│   │   ├── FileEntity.java
│   │   ├── AccessPolicy.java
│   │   ├── RbacRule.java
│   │   ├── AbacRule.java
│   │   └── AccessLog.java
│   ├── repository/          # JPA Repositories
│   ├── security/            # Security Components
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   └── service/             # Business Logic
│       ├── AccessControlService.java
│       ├── EncryptionService.java
│       └── FileStorageService.java
├── src/main/resources/
│   └── application.properties
├── database_schema.sql      # Database Schema
├── pom.xml                  # Maven Dependencies
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (for production) or use H2 (embedded)

### Installation

1. **Clone/Download the project**
   ```bash
   cd dual-access-control
   ```

2. **Configure Database** (Optional - uses H2 by default)
   
   For MySQL, edit `src/main/resources/application.properties`:
   ```properties
   # Uncomment and configure MySQL
   spring.datasource.url=jdbc:mysql://localhost:3306/dual_access_control_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Default Credentials

- **Username**: `admin`
- **Password**: `admin123`

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "roles": ["DATA_USER"]
}
```

### File Management Endpoints

#### Upload File
```http
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [binary file]
description: "Confidential report"
```

#### Download File
```http
GET /api/files/{fileId}/download
Authorization: Bearer {token}
```

#### Get My Files
```http
GET /api/files/my-files
Authorization: Bearer {token}
```

#### Get All Files
```http
GET /api/files/all
Authorization: Bearer {token}
```

#### Delete File
```http
DELETE /api/files/{fileId}
Authorization: Bearer {token}
```

## 🔐 Access Control Implementation

### How Dual Access Control Works

1. **RBAC (Role-Based Access Control)**
   - User must have the required role
   - Role must have the necessary permission (READ, WRITE, DELETE, SHARE)
   
2. **ABAC (Attribute-Based Access Control)**
   - User attributes are evaluated against policy rules
   - Supports multiple operators: EQUALS, GREATER_THAN, CONTAINS, etc.

3. **Dual Control**
   - **Both** RBAC and ABAC policies must pass
   - If either fails, access is denied
   - All access attempts are logged

### Example Access Control Flow

```
User requests to download a file
         ↓
Check RBAC: Does user have role with READ permission?
         ↓
Check ABAC: Do user attributes match policy requirements?
         ↓
Both Passed? → Grant Access
Either Failed? → Deny Access
         ↓
Log the access attempt
```

## 🧪 Testing the Application

### Using Postman

1. **Register a new user**
2. **Login to get JWT token**
3. **Upload a file** (include token in Authorization header)
4. **Try to download** (access will be granted/denied based on policies)

### H2 Database Console

Access the H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:dacdb`
- Username: `sa`
- Password: (leave empty)

## 📊 Database Schema

The system uses the following main tables:
- `users` - User information
- `roles` - Role definitions
- `user_roles` - User-Role mapping
- `attributes` - Attribute definitions
- `user_attributes` - User-Attribute values
- `files` - File metadata
- `access_policies` - Access control policies
- `rbac_rules` - RBAC policy rules
- `abac_rules` - ABAC policy rules
- `access_logs` - Audit logs

See `database_schema.sql` for complete schema.

## 🔒 Security Features

1. **Password Encryption**: BCrypt hashing
2. **JWT Authentication**: Stateless authentication
3. **File Encryption**: AES-256 encryption
4. **Secure Key Storage**: Separate key storage
5. **Access Logging**: Complete audit trail
6. **HTTPS Ready**: Can be configured for HTTPS

## 📈 Future Enhancements

1. **Cloud Storage Integration**
   - AWS S3
   - Google Cloud Storage
   - Azure Blob Storage

2. **Advanced Features**
   - File versioning
   - Real-time sharing
   - Advanced search
   - File preview

3. **UI Development**
   - React/Angular frontend
   - Dashboard analytics
   - User management interface

4. **Enhanced Security**
   - Two-factor authentication
   - Biometric authentication
   - Advanced threat detection

## 🐛 Troubleshooting

### Common Issues

1. **Port already in use**
   - Change port in `application.properties`: `server.port=8081`

2. **Database connection error**
   - Verify MySQL is running
   - Check credentials in `application.properties`

3. **File upload fails**
   - Check file size limit
   - Verify storage directory permissions

## 📝 Project Report Guidelines

### Include These Sections

1. **Introduction**
   - Problem statement
   - Objectives
   - Scope

2. **Literature Review**
   - Access control models
   - Cloud security
   - Encryption techniques

3. **System Design**
   - Architecture diagram
   - Database design
   - Security model

4. **Implementation**
   - Technologies used
   - Code structure
   - Key algorithms

5. **Testing**
   - Test cases
   - Results
   - Screenshots

6. **Conclusion**
   - Achievements
   - Limitations
   - Future work

## 👥 Contributors

- Your Name
- Roll Number
- Department

## 📄 License

This project is for educational purposes.

## 📧 Support

For questions or issues, contact: your-email@example.com

---

**Note**: This is a complete, production-ready implementation suitable for final year project submission. Make sure to test thoroughly and customize according to your requirements.
