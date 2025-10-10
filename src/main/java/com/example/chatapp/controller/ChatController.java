package com.example.chatapp.controller;

import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageDTO receiveMessage(MessageDTO messageDTO) {

        //User
        User user = userRepository.findByUsername(messageDTO.getUsername());
        if (user == null) {
            user = new User();
            user.setUsername(messageDTO.getUsername());
            userRepository.save(user);
        }


        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setUser(user);
        messageRepository.save(message);


        messageDTO.setTimestamp(message.getTimestamp());
        return messageDTO;
    }
}
