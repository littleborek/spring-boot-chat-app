package com.example.chatapp.dto;

import java.util.UUID;

public record CreateMessageRequest(
      UUID channelId,
      String content,
      String type,
      Long replyToMessageId
) {
  
}
