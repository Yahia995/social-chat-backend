package com.socialchat.controller;

import com.socialchat.dto.FileUploadResponse;
import com.socialchat.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload/profile")
    public ResponseEntity<FileUploadResponse> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String filePath = fileStorageService.uploadFile(file, "profiles/" + userId);
            
            FileUploadResponse response = FileUploadResponse.builder()
                    .filename(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileUrl(fileStorageService.getFileUrl(filePath))
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading profile photo", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/upload/post")
    public ResponseEntity<FileUploadResponse> uploadPostImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String filePath = fileStorageService.uploadFile(file, "posts/" + userId);
            
            FileUploadResponse response = FileUploadResponse.builder()
                    .filename(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileUrl(fileStorageService.getFileUrl(filePath))
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading post image", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/download/{subdirectory}/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subdirectory,
            @PathVariable String filename) {
        try {
            String filePath = subdirectory + "/" + filename;
            byte[] fileContent = fileStorageService.downloadFile(filePath);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (IOException e) {
            log.error("Error downloading file", e);
            return ResponseEntity.status(404).build();
        }
    }

    @DeleteMapping("/delete/{subdirectory}/{filename}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String subdirectory,
            @PathVariable String filename,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String filePath = subdirectory + "/" + filename;
            
            // Verify user owns the file (basic check)
            if (!filePath.contains(userId.toString())) {
                return ResponseEntity.status(403).build();
            }
            
            fileStorageService.deleteFile(filePath);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            log.error("Error deleting file", e);
            return ResponseEntity.status(500).build();
        }
    }
}
