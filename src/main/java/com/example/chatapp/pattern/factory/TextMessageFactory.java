package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import org.springframework.stereotype.Component;

/**
 * Factory for creating standard text messages
 */
@Component
public class TextMessageFactory implements MessageFactory {
    
    @Override
    public Message createMessage(String content, Channel channel, User author) {
        Message message = new Message();
        message.setContext(content);
        message.setChannel(channel);
        message.setAuthor(author);
        message.setContentMeta("{\"type\": \"text\"}");
        return message;
    }
    
    @Override
    public String getMessageType() {
        return "TEXT";
    }
}
