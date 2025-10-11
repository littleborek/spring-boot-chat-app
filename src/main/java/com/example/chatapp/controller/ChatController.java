package com.example.chatapp.controller;

import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.ChatRoomRepository;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ChatRoomRepository chatRoomRepository; 


    @Autowired private SimpMessagingTemplate messagingTemplate;

    /**
     * Belirli bir sohbet odasına mesaj gönderir.
     * Örnek adres: /app/chat/1/sendMessage (1 numaralı odaya mesaj gönderir)
     */
    @MessageMapping("/chat/{roomId}/sendMessage")
    @Transactional
    public void sendMessage(@DestinationVariable Long roomId, MessageDTO messageDTO, Principal principal) {

 
        String username = principal.getName();
        User user = userRepository.findByUsername(username);


        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));


        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setUser(user);
        message.setRoom(room);
        messageRepository.save(message);


        MessageDTO responseDto = new MessageDTO();
        responseDto.setContent(message.getContent());
        responseDto.setTimestamp(message.getTimestamp());
        responseDto.setUsername(user.getUsername());

        // Mesajı sadece doğru odanın WebSocket konusuna (/topic/rooms/{roomId}) gönder.
        messagingTemplate.convertAndSend(String.format("/topic/rooms/%d", roomId), responseDto);
    }
}