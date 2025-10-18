package com.example.chatapp.command;

import com.example.chatapp.service.MessageService;
import com.example.chatapp.model.User;


public interface MessageCommand {
    
    void execute(MessageService messageService, User executingUser);
}