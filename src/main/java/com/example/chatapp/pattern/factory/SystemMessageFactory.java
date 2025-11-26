package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import org.springframework.stereotype.Component;

/**
 * Factory for creating system messages (joins, leaves, etc.)
 */
@Component
public class SystemMessageFactory implements MessageFactory {
    
    @Override
    public Message createMessage(String content, Channel channel, User author) {
        Message message = new Message();
        message.setContext(content);
        message.setChannel(channel);
        message.setAuthor(author);
        message.setContentMeta("{\"type\": \"system\", \"automated\": true}");
        return message;
    }
    
    @Override
    public String getMessageType() {
        return "SYSTEM";
    }
}
