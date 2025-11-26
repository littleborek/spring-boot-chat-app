# Design Patterns Usage Examples

## Complete Code Examples

### 1. Observer Pattern - Receiving Notifications

#### Setup (Done in ObserverConfig)
```java
@Configuration
public class ObserverConfig {
    @PostConstruct
    public void init() {
        // Register observers - happens automatically on startup
        messageSubject.attach(notificationObserver);
        messageSubject.attach(webSocketObserver);
    }
}
```

#### Usage in MessageService
```java
@Service
public class MessageServiceImpl {
    private final MessageSubject messageSubject;
    
    public MessageDTO createMessage(CreateMessageRequest request, UUID userId) {
        // Create and save message
        Message message = messageRepository.save(newMessage);
        
        // Notify all observers automatically
        messageSubject.notifyMessageCreated(message);
        
        // Both NotificationObserver and WebSocketObserver
        // are notified without any additional code!
        
        return convertToDTO(message);
    }
}
```

#### Adding a New Observer
```java
@Component
public class EmailObserver implements MessageObserver {
    
    @Override
    public void onMessageCreated(Message message) {
        // Send email notification
        emailService.sendNotification(message);
    }
    
    @Override
    public void onMessageUpdated(Message message) {
        // Handle update
    }
    
    @Override
    public void onMessageDeleted(Long messageId) {
        // Handle deletion
    }
}

// Just register in ObserverConfig
messageSubject.attach(emailObserver);
```

---

### 2. Factory Pattern - Creating Channels

#### Using Channel Factory
```java
@Service
public class ChannelServiceImpl {
    private final ChannelFactoryProvider channelFactory;
    
    public ChannelDTO createChannel(CreateChannelRequest request, UUID userId) {
        Server server = serverRepository.findById(request.serverId())
            .orElseThrow();
        
        // Factory automatically creates the right type with correct settings
        Channel channel = channelFactory.createChannel(
            request.type(),        // TEXT, VOICE, or ANNOUNCEMENT
            request.name(),        // Channel name
            server,                // Parent server
            request.settings()     // Optional custom settings
        );
        
        channel = channelRepository.save(channel);
        return convertToDTO(channel);
    }
}
```

#### What Each Factory Creates
```java
// Text Channel
Channel textChannel = channelFactory.createChannel(
    ChannelType.TEXT, "general", server, null
);
// Result: Basic text channel with default settings

// Voice Channel
Channel voiceChannel = channelFactory.createChannel(
    ChannelType.VOICE, "Gaming Voice", server, null
);
// Result: Voice channel with bitrate: 64000, userLimit: 0

// Announcement Channel
Channel announcementChannel = channelFactory.createChannel(
    ChannelType.ANNOUNCEMENT, "announcements", server, null
);
// Result: Read-only channel for admins only
```

#### Creating Messages with Factory
```java
@Service
public class MessageServiceImpl {
    private final MessageFactoryProvider messageFactory;
    
    public MessageDTO createMessage(CreateMessageRequest request, UUID userId) {
        // Factory handles different message types
        Message message = messageFactory.createMessage(
            request.type(),      // "TEXT" or "SYSTEM"
            request.content(),   // Message content
            channel,             // Target channel
            author               // Message author
        );
        
        // TEXT messages get basic metadata
        // SYSTEM messages get automated flag
        
        return convertToDTO(messageRepository.save(message));
    }
}
```

---

### 3. Singleton Pattern - Managing WebSocket Connections

#### Registering Connections
```java
@Controller
public class WebSocketController {
    private final WebSocketConnectionManager connectionManager;
    
    @MessageMapping("/chat.connect")
    public void connect(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        UUID userUuid = UUID.fromString(userId);
        WebSocketSession session = headerAccessor.getSessionId();
        
        // Singleton manages all connections
        connectionManager.registerSession(userUuid, session);
        
        log.info("User connected: {}", userId);
    }
    
    @MessageMapping("/chat.disconnect")
    public void disconnect(@Payload String userId) {
        UUID userUuid = UUID.fromString(userId);
        
        // Remove from singleton
        connectionManager.removeSession(userUuid);
    }
}
```

#### Checking Online Status
```java
@Service
public class PresenceServiceImpl {
    private final WebSocketConnectionManager connectionManager;
    
    public List<User> getOnlineMembers(UUID serverId) {
        List<Membership> members = membershipRepository.findByServer(server);
        
        return members.stream()
            .filter(m -> connectionManager.isUserConnected(m.getUser().getId()))
            .map(Membership::getUser)
            .collect(Collectors.toList());
    }
    
    public int getActiveUserCount() {
        return connectionManager.getActiveConnectionCount();
    }
}
```

---

### 4. Strategy Pattern - Different Messaging Strategies

#### Automatic Strategy Selection
```java
@Service
public class MessageServiceImpl {
    private final MessagingContext messagingContext;
    
    public MessageDTO createMessage(CreateMessageRequest request, UUID userId) {
        // ... create message ...
        
        // Determine strategy based on channel type
        String strategyType = switch (channel.getType()) {
            case DM -> "PRIVATE";
            case ANNOUNCEMENT -> "ANNOUNCEMENT";
            default -> "CHANNEL";
        };
        
        // Get recipients
        List<User> recipients = getChannelMembers(channel);
        
        // Execute appropriate strategy
        messagingContext.executeStrategy(strategyType, message, recipients);
        
        // Strategy handles delivery automatically!
        
        return convertToDTO(message);
    }
}
```

#### What Each Strategy Does

**Channel Strategy** (Public broadcast)
```java
// Sends to: /topic/channel/{channelId}
// All subscribers receive the message
// Use for: Regular text channels
```

**Private Strategy** (Direct message)
```java
// Sends to: /queue/user/{userId} for each recipient
// Only specific users receive the message
// Use for: DMs, private conversations
```

**Announcement Strategy** (Server-wide)
```java
// Sends to: /topic/server/{serverId}/announcements
// All server members receive the message
// Use for: Important server announcements
```

#### Adding a New Strategy
```java
@Component
public class ThreadMessagingStrategy implements MessagingStrategy {
    
    @Override
    public void sendMessage(Message message, List<User> recipients) {
        // Send only to thread participants
        String destination = "/topic/thread/" + message.getThreadId();
        messagingTemplate.convertAndSend(destination, message);
    }
    
    @Override
    public String getStrategyType() {
        return "THREAD";
    }
}

// Spring automatically registers it!
// Just use: messagingContext.executeStrategy("THREAD", message, recipients);
```

---

### 5. Command Pattern - Moderation Actions

#### Executing Commands
```java
@Service
public class ModerationServiceImpl {
    private final CommandInvoker commandInvoker;
    
    public void kickUser(UUID targetUserId, UUID serverId, UUID moderatorId) {
        // Validate permissions
        validateServerOwner(moderatorId, server);
        
        // Create command
        Command kickCommand = new KickUserCommand(
            targetUser, 
            server, 
            membershipRepository
        );
        
        // Execute through invoker
        commandInvoker.executeCommand(kickCommand);
        
        // Command is automatically added to history!
    }
    
    public void undoLastAction() {
        // Undo the last moderation action
        commandInvoker.undoLastCommand();
    }
}
```

#### All Available Commands

**Kick User**
```java
Command kick = new KickUserCommand(user, server, membershipRepo);
commandInvoker.executeCommand(kick);
// Removes user from server
// Can be undone: user is re-added
```

**Ban User**
```java
Command ban = new BanUserCommand(user, server, membershipRepo);
commandInvoker.executeCommand(ban);
// Removes user and adds to ban list
// Can be undone: user removed from ban list
```

**Mute User**
```java
Command mute = new MuteUserCommand(user, membership, membershipRepo, 30);
commandInvoker.executeCommand(mute);
// Mutes user for 30 minutes
// Can be undone: mute removed
```

**Delete Message**
```java
Command delete = new DeleteMessageCommand(message, messageRepo);
commandInvoker.executeCommand(delete);
// Deletes message from database
// Can be undone: message restored
```

**Join Channel**
```java
Command join = new JoinChannelCommand(user, channel, membershipRepo);
commandInvoker.executeCommand(join);
// Adds user to channel
// Can be undone: user removed from channel
```

#### Command History and Audit Trail
```java
@Service
public class AuditService {
    private final CommandInvoker commandInvoker;
    
    public List<String> getCommandHistory() {
        return commandInvoker.getCommandHistory()
            .stream()
            .map(Command::getCommandName)
            .collect(Collectors.toList());
        
        // Returns: ["KICK", "MUTE", "DELETE_MESSAGE", ...]
    }
    
    public void undoLastThreeActions() {
        for (int i = 0; i < 3; i++) {
            commandInvoker.undoLastCommand();
        }
    }
}
```

---

## Complete Real-World Scenarios

### Scenario 1: User Sends a Message

```java
// Client sends POST request
POST /api/messages
{
    "channelId": "channel-uuid",
    "content": "Hello everyone!",
    "type": "TEXT"
}

// Flow:
1. MessageController receives request
2. MessageServiceImpl.createMessage() is called
3. MessageFactory creates TEXT message
4. Message saved to database
5. MessageSubject.notifyMessageCreated() triggers:
   - NotificationObserver: Creates DB notifications
   - WebSocketObserver: Broadcasts to /topic/channel/{id}
6. MessagingContext selects ChannelMessagingStrategy
7. All channel subscribers receive message in real-time
8. Response returned to client
```

### Scenario 2: Admin Kicks a User

```java
// Client sends POST request
POST /api/moderation/kick?targetUserId=user-1&serverId=server-1&moderatorId=admin-1

// Flow:
1. ModerationController receives request
2. ModerationServiceImpl.kickUser() validates admin permissions
3. KickUserCommand is created
4. CommandInvoker executes the command
5. Command removes membership from database
6. Command is added to history
7. Admin can undo: commandInvoker.undoLastCommand()
8. Membership would be restored
```

### Scenario 3: Creating a Voice Channel

```java
// Client sends POST request
POST /api/channels
{
    "serverId": "server-uuid",
    "name": "Gaming Voice",
    "type": "VOICE",
    "settings": null
}

// Flow:
1. ChannelController receives request
2. ChannelServiceImpl.createChannel() is called
3. ChannelFactoryProvider selects VoiceChannelFactory
4. Factory creates channel with:
   - type = VOICE
   - settings = {"bitrate": 64000, "userLimit": 0}
5. Channel saved to database
6. Response with channel DTO returned
```

### Scenario 4: Checking Who's Online

```java
// Client requests online members
GET /api/servers/{serverId}/online-members

// Flow:
1. ServerController receives request
2. PresenceServiceImpl.getOnlineMembers() is called
3. Gets all server members from database
4. For each member, checks WebSocketConnectionManager.isUserConnected()
5. Filters only connected users
6. Returns list of online users
```

---

## Testing the Patterns

### Testing Observer Pattern
```java
@Test
public void testObserverPattern() {
    // Arrange
    MessageObserver mockObserver = mock(MessageObserver.class);
    messageSubject.attach(mockObserver);
    
    // Act
    messageSubject.notifyMessageCreated(message);
    
    // Assert
    verify(mockObserver).onMessageCreated(message);
}
```

### Testing Factory Pattern
```java
@Test
public void testChannelFactory() {
    // Act
    Channel voiceChannel = channelFactory.createChannel(
        ChannelType.VOICE, "Test Voice", server, null
    );
    
    // Assert
    assertEquals(ChannelType.VOICE, voiceChannel.getType());
    assertTrue(voiceChannel.getSettings().contains("bitrate"));
}
```

### Testing Singleton Pattern
```java
@Test
public void testSingletonConnectionManager() {
    // Act
    connectionManager.registerSession(userId1, session1);
    connectionManager.registerSession(userId2, session2);
    
    // Assert
    assertEquals(2, connectionManager.getActiveConnectionCount());
    assertTrue(connectionManager.isUserConnected(userId1));
}
```

### Testing Strategy Pattern
```java
@Test
public void testStrategySelection() {
    // Arrange
    Channel dmChannel = new Channel();
    dmChannel.setType(ChannelType.DM);
    
    // Act
    String strategy = determineStrategyType(dmChannel);
    
    // Assert
    assertEquals("PRIVATE", strategy);
}
```

### Testing Command Pattern
```java
@Test
public void testCommandUndo() {
    // Arrange
    Command kickCommand = new KickUserCommand(user, server, repo);
    
    // Act
    commandInvoker.executeCommand(kickCommand);
    verify(repo).delete(membership);
    
    commandInvoker.undoLastCommand();
    
    // Assert
    verify(repo).save(membership);
}
```

---

## Common Patterns in Controllers

### Message Controller
```java
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(
            @RequestBody CreateMessageRequest request,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = extractUserIdFromToken(token);
        
        // Service handles: Factory + Observer + Strategy
        MessageDTO message = messageService.createMessage(request, userId);
        
        return ResponseEntity.ok(message);
    }
}
```

### Moderation Controller
```java
@RestController
@RequestMapping("/api/moderation")
public class ModerationController {
    
    @PostMapping("/kick")
    public ResponseEntity<Void> kickUser(
            @RequestParam UUID targetUserId,
            @RequestParam UUID serverId,
            @RequestParam UUID moderatorId) {
        
        // Service handles: Command pattern
        moderationService.kickUser(targetUserId, serverId, moderatorId);
        
        return ResponseEntity.ok().build();
    }
}
```

---

## WebSocket Client Examples

### JavaScript Client
```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to channel messages (Strategy Pattern in action!)
    stompClient.subscribe('/topic/channel/' + channelId, function(message) {
        displayMessage(JSON.parse(message.body));
    });
    
    // Subscribe to private messages
    stompClient.subscribe('/queue/user/' + userId, function(message) {
        displayPrivateMessage(JSON.parse(message.body));
    });
});

// Send message
function sendMessage(content) {
    stompClient.send("/app/chat.message", {}, JSON.stringify({
        'channelId': channelId,
        'content': content,
        'type': 'TEXT'
    }));
}
```

---

This comprehensive guide shows how all design patterns work together in your chat application!
