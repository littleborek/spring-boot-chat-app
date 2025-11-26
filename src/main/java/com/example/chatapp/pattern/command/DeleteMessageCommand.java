package com.example.chatapp.pattern.command;

import com.example.chatapp.entity.Message;
import com.example.chatapp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Command to delete a message
 */
@Slf4j
@RequiredArgsConstructor
public class DeleteMessageCommand implements Command {
    
    private final Message message;
    private final MessageRepository messageRepository;
    
    private Message deletedMessage;
    
    @Override
    public void execute() {
        deletedMessage = message;
        messageRepository.delete(message);
        log.info("Message {} deleted", message.getId());
    }
    
    @Override
    public void undo() {
        if (deletedMessage != null) {
            messageRepository.save(deletedMessage);
            log.info("Message {} restored", deletedMessage.getId());
        }
    }
    
    @Override
    public String getCommandName() {
        return "DELETE_MESSAGE";
    }
}
