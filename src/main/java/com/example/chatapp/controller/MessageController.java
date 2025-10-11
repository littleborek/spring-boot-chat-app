package com.example.chatapp.controller;

import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.model.Message;
import com.example.chatapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;


    @GetMapping("/rooms/{roomId}/messages")
    public List<MessageDTO> getMessagesForRoom(@PathVariable Long roomId) {
        List<Message> messages = messageRepository.findAllByRoomIdOrderByTimestampAsc(roomId);
        

        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            dto.setUsername(message.getUser().getUsername());
            dto.setContent(message.getContent());
            dto.setTimestamp(message.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }
}