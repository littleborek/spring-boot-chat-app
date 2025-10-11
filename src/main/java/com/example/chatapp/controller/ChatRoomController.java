package com.example.chatapp.controller;

import com.example.chatapp.dto.ChatRoomDTO;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController 
@RequestMapping("/api") 
public class ChatRoomController {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    
    @GetMapping("/rooms")
    public List<ChatRoomDTO> getChatRooms() {
        return chatRoomRepository.findAll()
                .stream()
                .map(room -> new ChatRoomDTO(room.getId(), room.getName()))
                .collect(Collectors.toList());
    }


    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestParam String name) {

        if (chatRoomRepository.findByName(name).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        ChatRoom newRoom = new ChatRoom();
        newRoom.setName(name);
        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        return ResponseEntity.ok(new ChatRoomDTO(savedRoom.getId(), savedRoom.getName()));
    }
}