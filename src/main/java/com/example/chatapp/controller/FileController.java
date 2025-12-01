package com.example.chatapp.controller;

import com.example.chatapp.dto.AttachmentDTO;
import com.example.chatapp.dto.FileUploadResponse;
import com.example.chatapp.entity.Attachment;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.impl.FileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and download endpoints")
public class FileController {

    private final FileServiceImpl fileService;

    @PostMapping("/upload/{messageId}")
    @Operation(summary = "Upload a file", description = "Uploads a file and attaches it to a message")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @PathVariable Long messageId,
            @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadFile(file, messageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-multiple/{messageId}")
    @Operation(summary = "Upload multiple files", description = "Uploads multiple files and attaches them to a message")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @PathVariable Long messageId,
            @RequestParam("files") List<MultipartFile> files) {
        List<FileUploadResponse> responses = fileService.uploadFiles(files, messageId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/message/{messageId}")
    @Operation(summary = "Get message attachments", description = "Returns all attachments for a message")
    public ResponseEntity<List<AttachmentDTO>> getMessageAttachments(@PathVariable Long messageId) {
        List<AttachmentDTO> attachments = fileService.getMessageAttachments(messageId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}")
    @Operation(summary = "Download file", description = "Downloads a file by attachment ID")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID attachmentId) {
        byte[] fileContent = fileService.getFileContent(attachmentId);
        Attachment attachment = fileService.getAttachment(attachmentId);
        
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + attachment.getStorageKey() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Deletes an attachment (only by owner)")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable UUID attachmentId,
            @AuthenticationPrincipal User user) {
        fileService.deleteAttachment(attachmentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
