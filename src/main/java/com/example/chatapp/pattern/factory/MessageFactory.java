package com.example.chatapp.pattern.factory;

import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.User;

/**
 * Factory interface for creating different types of messages
 */
public interface MessageFactory {
    Message createMessage(String content, Channel channel, User author);
    String getMessageType();
}
