package com.project.dualaccesscontrol.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption Service using AES-256 encryption for file security
 */
@Service
@Slf4j
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    
    /**
     * Generate a new AES-256 encryption key
     */
    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }
    
    /**
     * Generate an initialization vector for CBC mode
     */
    public byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    
    /**
     * Encrypt file data using AES-256
     */
    public byte[] encrypt(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        
        byte[] encrypted = cipher.doFinal(data);
        
        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        
        log.debug("Encrypted data of size: {} bytes", data.length);
        return combined;
    }
    
    /**
     * Decrypt file data using AES-256
     */
    public byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        // Extract IV from the beginning of encrypted data
        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[encryptedData.length - IV_SIZE];
        
        System.arraycopy(encryptedData, 0, iv, 0, IV_SIZE);
        System.arraycopy(encryptedData, IV_SIZE, encrypted, 0, encrypted.length);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        
        byte[] decrypted = cipher.doFinal(encrypted);
        log.debug("Decrypted data of size: {} bytes", decrypted.length);
        return decrypted;
    }
    
    /**
     * Convert SecretKey to Base64 string for storage
     */
    public String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * Convert Base64 string back to SecretKey
     */
    public SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }
    
    /**
     * Generate SHA-256 hash of the encryption key for verification
     */
    public String hashKey(String keyString) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(keyString.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Generate a secure random password for key derivation
     */
    public String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Derive a key from password using PBKDF2
     */
    public SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
            password.toCharArray(), 
            salt, 
            65536, 
            KEY_SIZE
        );
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }
    
    /**
     * Generate salt for password-based key derivation
     */
    public byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
