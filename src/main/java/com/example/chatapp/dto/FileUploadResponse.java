package com.example.chatapp.dto;

import java.util.UUID;

public record FileUploadResponse(
    UUID id,
    String fileName,
    String fileUrl,
    String mimeType,
    long size
) {}
