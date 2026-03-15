package com.project.dualaccesscontrol.service;

import com.project.dualaccesscontrol.model.FileEntity;
import com.project.dualaccesscontrol.model.User;
import com.project.dualaccesscontrol.repository.FileEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * File Storage Service with Encryption
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    
    private final FileEntityRepository fileEntityRepository;
    private final EncryptionService encryptionService;
    
    @Value("${file.storage.location:./storage/files}")
    private String storageLocation;
    
    /**
     * Upload and encrypt a file
     */
    public FileEntity uploadFile(MultipartFile file, User owner, String description) throws Exception {
        // Create storage directory if not exists
        Path storagePath = Paths.get(storageLocation);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Generate encryption key
        SecretKey encryptionKey = encryptionService.generateKey();
        byte[] iv = encryptionService.generateIV();
        
        // Encrypt file data
        byte[] fileData = file.getBytes();
        byte[] encryptedData = encryptionService.encrypt(fileData, encryptionKey, iv);
        
        // Save encrypted file
        Path filePath = storagePath.resolve(uniqueFilename);
        Files.write(filePath, encryptedData);
        
        // Hash the encryption key for storage
        String keyString = encryptionService.keyToString(encryptionKey);
        String keyHash = encryptionService.hashKey(keyString);
        
        // Create file entity
        FileEntity fileEntity = FileEntity.builder()
            .fileName(uniqueFilename)
            .originalName(originalFilename)
            .filePath(filePath.toString())
            .fileSize(file.getSize())
            .fileType(file.getContentType())
            .encrypted(true)
            .encryptionKeyHash(keyHash)
            .owner(owner)
            .description(description)
            .isDeleted(false)
            .build();
        
        FileEntity savedFile = fileEntityRepository.save(fileEntity);
        
        log.info("File uploaded and encrypted: {} by user: {}", uniqueFilename, owner.getUsername());
        
        // Store encryption key securely (in production, use a key management system)
        // For now, we'll store it in a separate secure location
        saveEncryptionKey(savedFile.getId(), keyString);
        
        return savedFile;
    }
    
    /**
     * Download and decrypt a file
     */
    public byte[] downloadFile(FileEntity fileEntity) throws Exception {
        Path filePath = Paths.get(fileEntity.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileEntity.getFileName());
        }
        
        byte[] encryptedData = Files.readAllBytes(filePath);
        
        // Retrieve encryption key
        String keyString = getEncryptionKey(fileEntity.getId());
        SecretKey key = encryptionService.stringToKey(keyString);
        
        // Decrypt file
        byte[] decryptedData = encryptionService.decrypt(encryptedData, key);
        
        log.info("File decrypted and downloaded: {}", fileEntity.getFileName());
        
        return decryptedData;
    }
    
    /**
     * Delete a file (soft delete)
     */
    public void deleteFile(FileEntity fileEntity) {
        fileEntity.setIsDeleted(true);
        fileEntityRepository.save(fileEntity);
        log.info("File soft deleted: {}", fileEntity.getFileName());
    }
    
    /**
     * Permanently delete a file
     */
    public void permanentlyDeleteFile(FileEntity fileEntity) throws IOException {
        Path filePath = Paths.get(fileEntity.getFilePath());
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Delete encryption key
        deleteEncryptionKey(fileEntity.getId());
        
        fileEntityRepository.delete(fileEntity);
        log.info("File permanently deleted: {}", fileEntity.getFileName());
    }
    
    /**
     * Get all files for a user
     */
    public List<FileEntity> getUserFiles(User user) {
        return fileEntityRepository.findByOwnerAndIsDeletedFalse(user);
    }
    
    /**
     * Get all files
     */
    public List<FileEntity> getAllFiles() {
        return fileEntityRepository.findByIsDeletedFalse();
    }
    
    /**
     * Save encryption key securely
     * In production, use a proper Key Management System (KMS)
     */
    private void saveEncryptionKey(Long fileId, String keyString) throws IOException {
        Path keyStoragePath = Paths.get(storageLocation, "keys");
        if (!Files.exists(keyStoragePath)) {
            Files.createDirectories(keyStoragePath);
        }
        
        Path keyFilePath = keyStoragePath.resolve(fileId + ".key");
        Files.write(keyFilePath, keyString.getBytes());
    }
    
    /**
     * Retrieve encryption key
     */
    private String getEncryptionKey(Long fileId) throws IOException {
        Path keyFilePath = Paths.get(storageLocation, "keys", fileId + ".key");
        
        if (!Files.exists(keyFilePath)) {
            throw new IOException("Encryption key not found for file: " + fileId);
        }
        
        return new String(Files.readAllBytes(keyFilePath));
    }
    
    /**
     * Delete encryption key
     */
    private void deleteEncryptionKey(Long fileId) throws IOException {
        Path keyFilePath = Paths.get(storageLocation, "keys", fileId + ".key");
        
        if (Files.exists(keyFilePath)) {
            Files.delete(keyFilePath);
        }
    }
}
