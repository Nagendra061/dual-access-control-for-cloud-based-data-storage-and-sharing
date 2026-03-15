# ⚡ Quick Start Guide - 5 Minutes to Running System

## Step 1: Prerequisites Check ✓

Make sure you have:
- [ ] Java 17 installed (`java -version`)
- [ ] Maven installed (`mvn -version`)

## Step 2: Run the Application 🚀

```bash
cd dual-access-control
mvn spring-boot:run
```

Wait for: `Started DualAccessControlApplication`

## Step 3: Test with cURL 🧪

### Test 1: Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Copy the token from response!**

### Test 2: Create a Test File
```bash
echo "Hello, Dual Access Control!" > myfile.txt
```

### Test 3: Upload File (Replace YOUR_TOKEN_HERE)
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@myfile.txt" \
  -F "description=My first upload"
```

### Test 4: List Your Files
```bash
curl -X GET http://localhost:8080/api/files/my-files \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## ✅ You're Done!

The system is working if you see:
- Login returned a JWT token ✓
- File uploaded successfully ✓
- File listed in response ✓

## 🎯 Next Steps

1. **Read full documentation:** `README.md`
2. **Run complete tests:** `TESTING_GUIDE.md`
3. **Prepare presentation:** `PRESENTATION_GUIDE.md`
4. **Import Postman collection:** `Postman_Collection.json`

## 🔍 View Database

1. Open browser: `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:dacdb`
3. Username: `sa`
4. Password: (leave blank)
5. Click "Connect"

## 📂 Check Encrypted Files

```bash
ls -la ./storage/files/
cat ./storage/files/*.txt  # You'll see encrypted data!
```

## 🐛 Quick Troubleshooting

**Error: Port 8080 in use**
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Error: Maven not found**
```bash
# Download from: https://maven.apache.org/download.cgi
# Or use wrapper:
./mvnw spring-boot:run
```

## 🎓 Project Checklist

- [ ] Application runs successfully
- [ ] Can register new users
- [ ] Can login and get JWT token
- [ ] Can upload files (they get encrypted)
- [ ] Can download files (they get decrypted)
- [ ] Access control works
- [ ] Logs are created in database

## 📞 Need Help?

1. Check `README.md` for detailed docs
2. Check `TESTING_GUIDE.md` for all test cases
3. Review code comments
4. Check application logs

---

**Your system is ready! Start testing and preparing your presentation! 🎉**
