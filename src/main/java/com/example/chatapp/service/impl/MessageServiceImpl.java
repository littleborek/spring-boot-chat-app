package com.example.chatapp.service.impl;

import com.example.chatapp.dto.CreateMessageRequest;
import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.Channel;
import com.example.chatapp.entity.Message;
import com.example.chatapp.entity.User;
import com.example.chatapp.exception.BadRequestException;
import com.example.chatapp.pattern.factory.MessageFactoryProvider;
import com.example.chatapp.pattern.observer.MessageSubject;
import com.example.chatapp.pattern.strategy.MessagingContext;
import com.example.chatapp.repository.ChannelRepository;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.MembershipRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation that integrates design patterns:
 * - Factory Pattern for creating messages
 * - Observer Pattern for notifying about message events
 * - Strategy Pattern for different messaging strategies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    
    // Design Pattern Components
    private final MessageFactoryProvider messageFactory;
    private final MessageSubject messageSubject;
    private final MessagingContext messagingContext;
    
    @Override
    @Transactional
    public MessageDTO createMessage(CreateMessageRequest request, UUID userId) {
        // Validate user and channel
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new BadRequestException("Channel not found"));
        
        // Check if user is member of the server
        if (channel.getServer() != null) {
            boolean isMember = membershipRepository.existsByUserAndServer(author, channel.getServer());
            if (!isMember) {
                throw new BadRequestException("User is not a member of this server");
            }
        }
        
        // Use Factory Pattern to create message
        String messageType = request.type() != null ? request.type() : "TEXT";
        Message message = messageFactory.createMessage(messageType, request.content(), channel, author);
        
        // Save message
        message = messageRepository.save(message);
        log.info("Message created: {} in channel: {}", message.getId(), channel.getId());
        
        // Use Observer Pattern to notify observers
        messageSubject.notifyMessageCreated(message);
        
        // Use Strategy Pattern to send message based on channel type
        String strategyType = determineStrategyType(channel);
        List<User> recipients = getChannelMembers(channel);
        messagingContext.executeStrategy(strategyType, message, recipients);
        
        return convertToDTO(message);
    }
    
    @Override
    @Transactional
    public MessageDTO updateMessage(Long messageId, String newContent, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BadRequestException("Message not found"));
        
        // Check if user is the author
        if (!message.getAuthor().getId().equals(userId)) {
            throw new BadRequestException("Only the author can edit this message");
        }
        
        message.setContext(newContent);
        message.setEditedAt(LocalDateTime.now());
        message = messageRepository.save(message);
        
        // Notify observers
        messageSubject.notifyMessageUpdated(message);
        
        return convertToDTO(message);
    }
    
    @Override
    @Transactional
    public void deleteMessage(Long messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BadRequestException("Message not found"));
        
        // Check if user is the author or server owner
        if (!message.getAuthor().getId().equals(userId) && 
            !isServerOwner(userId, message.getChannel().getServer())) {
            throw new BadRequestException("You don't have permission to delete this message");
        }
        
        messageRepository.delete(message);
        messageSubject.notifyMessageDeleted(messageId);
    }
    
    @Override
    public List<MessageDTO> getChannelMessages(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BadRequestException("Channel not found"));
        
        List<Message> messages = messageRepository.findByChannelOrderByCreatedAtDesc(channel);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MessageDTO> searchMessages(UUID channelId, String keyword) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BadRequestException("Channel not found"));
        
        List<Message> messages = messageRepository.findByChannelAndContextContainingIgnoreCase(channel, keyword);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private String determineStrategyType(Channel channel) {
        return switch (channel.getType()) {
            case DM -> "PRIVATE";
            case ANNOUNCEMENT -> "ANNOUNCEMENT";
            default -> "CHANNEL";
        };
    }
    
    private List<User> getChannelMembers(Channel channel) {
        if (channel.getServer() != null) {
            return membershipRepository.findByServer(channel.getServer())
                    .stream()
                    .map(membership -> membership.getUser())
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    private boolean isServerOwner(UUID userId, com.example.chatapp.entity.Server server) {
        return server != null && server.getOwner().getId().equals(userId);
    }
    
    private MessageDTO convertToDTO(Message message) {
        UserDTO authorDTO = new UserDTO(
            message.getAuthor().getId(),
            message.getAuthor().getUsername(),
            message.getAuthor().getAvatarUrl()
        );
        
        return new MessageDTO(
            message.getId(),
            message.getChannel().getId(),
            authorDTO,
            message.getContext(),
            message.getCreatedAt(),
            message.getEditedAt(),
            message.getReplyToMessage() != null ? message.getReplyToMessage().getId() : null,
            List.of() // Attachments - simplified
        );
    }
}
