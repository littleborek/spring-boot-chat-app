package com.example.chatapp.dto;

public record CreateMessageRequest(
      String context,
      Long replyToMessageId
) {
  
}
