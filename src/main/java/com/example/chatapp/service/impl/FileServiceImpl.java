package com.example.chatapp.service.impl;

import com.example.chatapp.dto.AttachmentDTO;
import com.example.chatapp.dto.FileUploadResponse;
import com.example.chatapp.entity.Attachment;
import com.example.chatapp.entity.Message;
import com.example.chatapp.repository.AttachmentRepository;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {

    private final AttachmentRepository attachmentRepository;
    private final MessageRepository messageRepository;
    
    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${app.file.base-url:http://localhost:8080/api/files}")
    private String baseUrl;
    
    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // Allowed MIME types
    private static final List<String> ALLOWED_TYPES = List.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf", "text/plain",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip", "application/x-rar-compressed"
    );

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, Long messageId) {
        validateFile(file);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        String fileName = generateFileName(file.getOriginalFilename());
        String storageKey = saveFile(file, fileName);
        
        Attachment attachment = new Attachment();
        attachment.setMessage(message);
        attachment.setStorageKey(storageKey);
        attachment.setMimeType(file.getContentType());
        attachment.setSize((int) file.getSize());
        
        attachment = attachmentRepository.save(attachment);
        
        log.info("File uploaded: {} for message {}", fileName, messageId);
        
        return new FileUploadResponse(
                attachment.getId(),
                file.getOriginalFilename(),
                baseUrl + "/" + attachment.getId(),
                attachment.getMimeType(),
                attachment.getSize()
        );
    }

    @Override
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files, Long messageId) {
        List<FileUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadFile(file, messageId));
        }
        return responses;
    }

    @Override
    public List<AttachmentDTO> getMessageAttachments(Long messageId) {
        return attachmentRepository.findByMessageId(messageId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(UUID attachmentId, UUID userId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        
        // Check if user is the message author
        if (!attachment.getMessage().getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own attachments");
        }
        
        // Delete file from storage
        deleteFileFromStorage(attachment.getStorageKey());
        
        // Delete from database
        attachmentRepository.delete(attachment);
        
        log.info("Attachment deleted: {}", attachmentId);
    }

    @Override
    public String getFileUrl(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        return baseUrl + "/" + attachment.getId();
    }
    
    public byte[] getFileContent(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        
        try {
            Path filePath = Paths.get(uploadDir).resolve(attachment.getStorageKey());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    public Attachment getAttachment(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum allowed size (10MB)");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("File type not allowed: " + contentType);
        }
    }
    
    private String generateFileName(String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    private String saveFile(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }
    
    private void deleteFileFromStorage(String storageKey) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(storageKey);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storageKey, e);
        }
    }
    
    private AttachmentDTO toDTO(Attachment attachment) {
        return new AttachmentDTO(
                attachment.getId(),
                attachment.getStorageKey(),
                attachment.getMimeType(),
                attachment.getSize()
        );
    }
}
