package com.example.chatapp.service;

import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.dto.MessageDTO;
import org.slf4j.Logger; // Ensure imports
import org.slf4j.LoggerFactory; // Ensure imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    public MessageService(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }


    /**
     * Deletes a message if the executing user is the owner.
     * Notifies the room about the deletion.
     * @param messageId ID of the message to delete.
     * @param executingUser User attempting the deletion.
     * @throws AccessDeniedException if user is not the owner.
     * @throws RuntimeException if message is not found.
     */
    @Transactional
    public void deleteMessage(Long messageId, User executingUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with ID: " + messageId));

        // Authorization Check
        if (!message.getUser().getId().equals(executingUser.getId())) {
             logger.warn("User {} attempted to delete message {} owned by {}",
                         executingUser.getUsername(), messageId, message.getUser().getUsername()); 
            throw new AccessDeniedException("You do not have permission to delete this message.");
        }

        Long roomId = message.getRoom().getId(); 

        messageRepository.delete(message);
        logger.info("Message {} deleted by user {}", messageId, executingUser.getUsername()); 

        
        MessageDTO notificationDto = new MessageDTO();
        notificationDto.setMessageId(messageId);
        notificationDto.setUsername("System");
        notificationDto.setContent("Message deleted");
        notificationDto.setTimestamp(LocalDateTime.now());

        messagingTemplate.convertAndSend(String.format("/topic/rooms/%d", roomId), notificationDto);
    }

    /**
     * Edits the content of a message if the executing user is the owner.
     * Notifies the room with the updated message content.
     * @param messageId ID of the message to edit.
     * @param newContent The new content for the message.
     * @param executingUser User attempting the edit.
     * @return A DTO containing the updated message information.
     * @throws AccessDeniedException if user is not the owner.
     * @throws RuntimeException if message is not found.
     */
    @Transactional
    public MessageDTO editMessage(Long messageId, String newContent, User executingUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with ID: " + messageId));

        // Authorization Check
        if (!message.getUser().getId().equals(executingUser.getId())) {
             logger.warn("User {} attempted to edit message {} owned by {}",
                         executingUser.getUsername(), messageId, message.getUser().getUsername()); // Keep warning
            throw new AccessDeniedException("You do not have permission to edit this message.");
        }

        message.setContent(newContent);
        Message savedMessage = messageRepository.save(message);
        logger.info("Message {} edited by user {}", messageId, executingUser.getUsername());

        MessageDTO updatedDto = new MessageDTO();
        updatedDto.setMessageId(savedMessage.getId());
        updatedDto.setUsername(savedMessage.getUser().getUsername());
        updatedDto.setContent(savedMessage.getContent());
        updatedDto.setTimestamp(savedMessage.getTimestamp());


        // Notify room
        messagingTemplate.convertAndSend(String.format("/topic/rooms/%d", savedMessage.getRoom().getId()), updatedDto);

        return updatedDto;
    }
}