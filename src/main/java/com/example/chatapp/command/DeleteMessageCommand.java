package com.example.chatapp.command;

import com.example.chatapp.service.MessageService;
import com.example.chatapp.model.User;


public class DeleteMessageCommand implements MessageCommand {

    private final Long messageId;

    public DeleteMessageCommand(Long messageId) {
        this.messageId = messageId;
    }

    @Override
    public void execute(MessageService messageService, User executingUser) {
        //MessageService
        messageService.deleteMessage(this.messageId, executingUser);
    }

    public Long getMessageId() {
        return messageId;
    }
}