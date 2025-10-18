package com.example.chatapp.controller;

import com.example.chatapp.command.DeleteMessageCommand; 
import com.example.chatapp.command.EditMessageCommand; 
import com.example.chatapp.dto.EditMessageRequest; 
import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.MessageRepository; 
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.MessageService; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload; 
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    
    @Autowired private MessageRepository messageRepository; 
    @Autowired private UserRepository userRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private MessageService messageService; 

    /**
     * Handles sending a new message to a room.
     * Saves the message and broadcasts it.
     */
    @MessageMapping("/chat/{roomId}/sendMessage")
    @Transactional // Contains DB operation
    public void sendMessage(@DestinationVariable Long roomId, @Payload MessageDTO messageDTO, Principal principal) {
        if (principal == null || principal.getName() == null) {
            logger.error("sendMessage: Principal is null for room {}", roomId);
            return;
        }
        String username = principal.getName();

        try {
            User user = userRepository.findByUsername(username);
            if(user == null) throw new RuntimeException("User not found: " + username);

            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));

            // Create and save the message entity
            Message message = new Message();
            message.setContent(messageDTO.getContent());
            message.setTimestamp(LocalDateTime.now());
            message.setUser(user);
            message.setRoom(room);
            Message savedMessage = messageRepository.save(message);

            // Convert to DTO and broadcast to the room topic
            MessageDTO responseDto = convertToDto(savedMessage);
            messagingTemplate.convertAndSend(getRoomTopic(roomId), responseDto);
            

        } catch (Exception e) {
            logger.error("Error sending message in room {}: {}", roomId, e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", "Error sending message: " + e.getMessage());
        }
    }

    /**
     * Handles request to delete a message.
     * Delegates logic and authorization check to MessageService.
     */
    @MessageMapping("/chat/{roomId}/deleteMessage")
    public void deleteMessage(@DestinationVariable Long roomId, @Payload Long messageId, Principal principal) {
        if (principal == null || principal.getName() == null) {
             logger.error("deleteMessage: Principal is null for room {}, msg {}", roomId, messageId); return;
        }
        String username = principal.getName();
       
        try {
            User user = userRepository.findByUsername(username);
            if(user == null) throw new RuntimeException("User not found: " + username);

            
            DeleteMessageCommand command = new DeleteMessageCommand(messageId);
            command.execute(messageService, user);
            

        } catch (AccessDeniedException e) {
            
            logger.warn("Access denied for deleteMessage: User {}, Msg {}", username, messageId);
             messagingTemplate.convertAndSendToUser(username, "/queue/errors", e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting message {} in room {}: {}", messageId, roomId, e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", "Error deleting message: " + e.getMessage());
        }
    }

    /**
     * Handles request to edit a message.
     * Delegates logic and authorization check to MessageService.
     */
    @MessageMapping("/chat/{roomId}/editMessage")
    public void editMessage(@DestinationVariable Long roomId, @Payload EditMessageRequest editRequest, Principal principal) {
         if (principal == null || principal.getName() == null) {
              logger.error("editMessage: Principal is null for room {}, msg {}", roomId, editRequest.getMessageId()); return;
         }
        String username = principal.getName();
        

        try {
            User user = userRepository.findByUsername(username);
             if(user == null) throw new RuntimeException("User not found: " + username);

            
            EditMessageCommand command = new EditMessageCommand(editRequest.getMessageId(), editRequest.getNewContent());
            command.execute(messageService, user);
            

        } catch (AccessDeniedException e) {
            // Logged in service, send specific error back to user
            logger.warn("Access denied for editMessage: User {}, Msg {}", username, editRequest.getMessageId());
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", e.getMessage());
        } catch (Exception e) {
            logger.error("Error editing message {} in room {}: {}", editRequest.getMessageId(), roomId, e.getMessage(), e);
             messagingTemplate.convertAndSendToUser(username, "/queue/errors", "Error editing message: " + e.getMessage());
        }
    }

    /**
     * General exception handler for WebSocket mappings in this controller.
     * Sends error message back to the specific user who caused the error.
     */
    @MessageExceptionHandler({ AccessDeniedException.class, RuntimeException.class }) 
    @SendToUser("/queue/errors") // Send return value to user's private error queue
    public String handleException(Exception exception, Principal principal) {
        String username = (principal != null) ? principal.getName() : "UNKNOWN";
        logger.error("WebSocket Exception for user {}: {}", username, exception.getMessage());
        
        if (exception instanceof AccessDeniedException) {
            return "Permission denied: " + exception.getMessage();
        } else {
             return "An error occurred: " + exception.getMessage();
        }
    }



    private String getRoomTopic(Long roomId) {
        return String.format("/topic/rooms/%d", roomId);
    }

    private MessageDTO convertToDto(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getId()); // Ensure messageId is set
        dto.setUsername(message.getUser().getUsername());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        return dto;
    }
}