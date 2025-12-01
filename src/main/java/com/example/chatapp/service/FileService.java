package com.example.chatapp.service;

import com.example.chatapp.dto.AttachmentDTO;
import com.example.chatapp.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileService {
    
    /**
     * Upload a file and attach it to a message
     */
    FileUploadResponse uploadFile(MultipartFile file, Long messageId);
    
    /**
     * Upload multiple files and attach them to a message
     */
    List<FileUploadResponse> uploadFiles(List<MultipartFile> files, Long messageId);
    
    /**
     * Get all attachments for a message
     */
    List<AttachmentDTO> getMessageAttachments(Long messageId);
    
    /**
     * Delete an attachment
     */
    void deleteAttachment(UUID attachmentId, UUID userId);
    
    /**
     * Get file download URL
     */
    String getFileUrl(UUID attachmentId);
}
