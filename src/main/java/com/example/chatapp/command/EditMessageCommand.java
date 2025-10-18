package com.example.chatapp.command;

import com.example.chatapp.service.MessageService;
import com.example.chatapp.model.User;


public class EditMessageCommand implements MessageCommand {

    private final Long messageId;    
    private final String newContent; 

    public EditMessageCommand(Long messageId, String newContent) {
        this.messageId = messageId;
        this.newContent = newContent;
    }

    @Override
    public void execute(MessageService messageService, User executingUser) {
        //MessageService
        messageService.editMessage(this.messageId, this.newContent, executingUser);
    }


    public Long getMessageId() { return messageId; }
    public String getNewContent() { return newContent; }
}