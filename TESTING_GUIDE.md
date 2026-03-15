# Testing Guide - Dual Access Control System

## 📋 Table of Contents
1. [Setup Instructions](#setup-instructions)
2. [Testing Authentication](#testing-authentication)
3. [Testing File Operations](#testing-file-operations)
4. [Testing Access Control](#testing-access-control)
5. [Testing Encryption](#testing-encryption)
6. [Database Verification](#database-verification)

## 🚀 Setup Instructions

### Step 1: Start the Application

```bash
cd dual-access-control
mvn spring-boot:run
```

Wait for the message: `Started DualAccessControlApplication`

### Step 2: Verify Application is Running

Open browser: `http://localhost:8080`

You should see a Whitelabel Error Page (this is normal - it means the backend is running)

### Step 3: Access H2 Console (Optional)

URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:dacdb`
- Username: `sa`
- Password: (leave blank)

## 🔐 Testing Authentication

### Test Case 1: User Registration

**Using Postman/cURL:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

**Expected Response:**
```
User registered successfully
```

### Test Case 2: User Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@dualaccesscontrol.com",
  "fullName": "System Administrator",
  "roles": ["ADMIN"]
}
```

**Save the token** - you'll need it for subsequent requests!

### Test Case 3: Invalid Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

**Expected Response:**
```
Invalid username or password
```

## 📁 Testing File Operations

### Test Case 4: Upload File

Create a test file first:
```bash
echo "This is a test file" > test.txt
```

Upload using curl:
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@test.txt" \
  -F "description=Test file upload"
```

**Expected Response:**
```
File uploaded successfully: test.txt
```

**Verification Steps:**
1. Check if file is encrypted in `./storage/files/` directory
2. Check if encryption key is stored in `./storage/files/keys/`
3. Verify file entry in database (H2 console)

### Test Case 5: List My Files

```bash
curl -X GET http://localhost:8080/api/files/my-files \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "fileName": "uuid-random.txt",
    "originalName": "test.txt",
    "fileSize": 21,
    "fileType": "text/plain",
    "encrypted": true,
    "uploadedAt": "2024-01-15T10:30:00",
    "description": "Test file upload"
  }
]
```

### Test Case 6: Download File

```bash
curl -X GET http://localhost:8080/api/files/1/download \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -o downloaded_file.txt
```

**Verification:**
```bash
cat downloaded_file.txt
```
Should show: "This is a test file"

### Test Case 7: Delete File

```bash
curl -X DELETE http://localhost:8080/api/files/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected Response:**
```
File deleted successfully
```

## 🔒 Testing Access Control

### Test Case 8: RBAC - Role-Based Access Control

**Scenario:** User without proper role tries to access file

1. Create a regular user (DATA_USER role)
2. Upload file as admin
3. Try to delete the file as regular user

**Expected:** Access denied (only file owner or admin can delete)

### Test Case 9: ABAC - Attribute-Based Access Control

This requires setting up policies. Here's how to test:

1. **Add attributes to a user** (using SQL):
```sql
INSERT INTO user_attributes (user_id, attribute_id, attribute_value)
VALUES (1, 1, 'Engineering');

INSERT INTO user_attributes (user_id, attribute_id, attribute_value)
VALUES (1, 2, '3');
```

2. **Create an access policy with ABAC rules** (using SQL):
```sql
-- Create policy for file
INSERT INTO access_policies (policy_name, file_id, policy_type, created_by)
VALUES ('Engineering Access', 1, 'ABAC', 1);

-- Create ABAC rule
INSERT INTO abac_rules (policy_id, attribute_id, operator, required_value, permission)
VALUES (1, 1, 'EQUALS', 'Engineering', 'READ');
```

3. **Test access:**
   - User with department='Engineering' → Access granted
   - User with department='Marketing' → Access denied

### Test Case 10: Dual Access Control

**Scenario:** Both RBAC and ABAC must pass

1. Create a policy with both RBAC and ABAC rules
2. Test with user who:
   - Has correct role but wrong attributes → Denied
   - Has correct attributes but wrong role → Denied
   - Has both correct role and attributes → Granted

## 🔐 Testing Encryption

### Test Case 11: Verify File Encryption

1. Upload a file
2. Navigate to `./storage/files/` directory
3. Open the encrypted file with a text editor

**Expected:** File content is encrypted (unreadable binary data)

### Test Case 12: Verify Decryption

1. Download a previously uploaded file
2. Compare with original file

**Expected:** Files should be identical

```bash
diff original.txt downloaded.txt
# Should show no differences
```

### Test Case 13: Key Management

1. Upload a file (note the file ID)
2. Check `./storage/files/keys/{fileId}.key` exists
3. Content should be a Base64-encoded AES key

## 📊 Database Verification

### Test Case 14: Verify Access Logs

Query in H2 Console:
```sql
SELECT * FROM access_logs ORDER BY accessed_at DESC;
```

**Verify:**
- Each file access is logged
- Access granted/denied is recorded
- User, file, and action are tracked

### Test Case 15: Verify User Roles

```sql
SELECT u.username, r.role_name 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
```

### Test Case 16: Verify Encrypted Files

```sql
SELECT id, file_name, original_name, encrypted, encryption_key_hash
FROM files
WHERE is_deleted = false;
```

**Verify:**
- All files have encrypted = true
- encryption_key_hash is populated

## 🧪 Complete Test Sequence

### Run this complete test sequence:

1. **Setup**
   ```bash
   mvn spring-boot:run
   ```

2. **Register User**
   ```bash
   # Register user 1
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","email":"alice@test.com","password":"pass123","fullName":"Alice"}'
   
   # Register user 2
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"bob","email":"bob@test.com","password":"pass123","fullName":"Bob"}'
   ```

3. **Login as Alice**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"pass123"}'
   # Save token as ALICE_TOKEN
   ```

4. **Upload File as Alice**
   ```bash
   echo "Confidential Data" > confidential.txt
   curl -X POST http://localhost:8080/api/files/upload \
     -H "Authorization: Bearer $ALICE_TOKEN" \
     -F "file=@confidential.txt" \
     -F "description=Confidential file"
   ```

5. **Login as Bob**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"bob","password":"pass123"}'
   # Save token as BOB_TOKEN
   ```

6. **Try to Download Alice's File as Bob**
   ```bash
   curl -X GET http://localhost:8080/api/files/1/download \
     -H "Authorization: Bearer $BOB_TOKEN"
   # Should be denied (no access policy)
   ```

7. **Verify Access Logs**
   ```sql
   SELECT * FROM access_logs WHERE file_id = 1;
   ```

## ✅ Test Result Checklist

- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Invalid credentials are rejected
- [ ] File upload creates encrypted file
- [ ] Files are listed correctly
- [ ] File download decrypts correctly
- [ ] Unauthorized access is denied
- [ ] RBAC policies work
- [ ] ABAC policies work
- [ ] Dual control works (both must pass)
- [ ] Access logs are created
- [ ] Encryption keys are stored securely

## 🐛 Common Issues and Solutions

### Issue 1: "Port 8080 already in use"
**Solution:** Change port in application.properties or kill process on port 8080

### Issue 2: "JWT token expired"
**Solution:** Login again to get a new token (tokens expire after 24 hours)

### Issue 3: "File not found"
**Solution:** Check if storage directory exists and has write permissions

### Issue 4: "Access denied"
**Solution:** Verify user has correct roles and attributes for the file

## 📸 Screenshots to Include in Report

1. Postman showing successful login
2. File upload response
3. H2 console showing encrypted files
4. Access logs table
5. Access denied scenario
6. Successful file download

## 📝 Performance Metrics to Record

1. File upload time (with encryption)
2. File download time (with decryption)
3. Access control check time
4. Database query performance

---

**Note:** All test cases should be executed and documented in your final year project report with screenshots and results.
