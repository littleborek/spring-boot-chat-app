package com.example.chatapp.service;

import com.example.chatapp.dto.CreateMessageRequest;
import com.example.chatapp.dto.MessageDTO;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    
    MessageDTO createMessage(CreateMessageRequest request, UUID userId);
    
    MessageDTO updateMessage(Long messageId, String newContent, UUID userId);
    
    void deleteMessage(Long messageId, UUID userId);
    
    List<MessageDTO> getChannelMessages(UUID channelId);
    
    List<MessageDTO> searchMessages(UUID channelId, String keyword);
}
