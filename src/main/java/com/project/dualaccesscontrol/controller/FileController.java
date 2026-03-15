package com.project.dualaccesscontrol.controller;

import com.project.dualaccesscontrol.model.FileEntity;
import com.project.dualaccesscontrol.model.User;
import com.project.dualaccesscontrol.repository.UserRepository;
import com.project.dualaccesscontrol.service.AccessControlService;
import com.project.dualaccesscontrol.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File Management Controller
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    
    private final FileStorageService fileStorageService;
    private final AccessControlService accessControlService;
    private final UserRepository userRepository;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        try {
            User user = getUserFromAuthentication(authentication);
            FileEntity uploadedFile = fileStorageService.uploadFile(file, user, description);
            
            return ResponseEntity.ok()
                .body("File uploaded successfully: " + uploadedFile.getOriginalName());
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File upload failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        try {
            User user = getUserFromAuthentication(authentication);
            FileEntity file = fileStorageService.getAllFiles().stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found"));
            
            // Check access control
            AccessControlService.AccessDecision decision = 
                accessControlService.checkAccess(user, file, "READ");
            
            if (!decision.isGranted()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: " + decision.getReason());
            }
            
            byte[] fileData = fileStorageService.downloadFile(file);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
        } catch (Exception e) {
            log.error("File download failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File download failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/my-files")
    public ResponseEntity<?> getMyFiles(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            List<FileEntity> files = fileStorageService.getUserFiles(user);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("Failed to retrieve files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve files");
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllFiles(Authentication authentication) {
        try {
            List<FileEntity> files = fileStorageService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("Failed to retrieve files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve files");
        }
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        try {
            User user = getUserFromAuthentication(authentication);
            FileEntity file = fileStorageService.getAllFiles().stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found"));
            
            // Check access control
            AccessControlService.AccessDecision decision = 
                accessControlService.checkAccess(user, file, "DELETE");
            
            if (!decision.isGranted()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: " + decision.getReason());
            }
            
            fileStorageService.deleteFile(file);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            log.error("File deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File deletion failed: " + e.getMessage());
        }
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
