# Design Patterns Quick Reference

## Pattern Overview

This chat application implements **5 core design patterns**:

### 🔔 Observer Pattern
**Package:** `pattern.observer`
- **Purpose:** Notify multiple components when messages are created/updated/deleted
- **Key Classes:** `MessageSubject`, `NotificationObserver`, `WebSocketObserver`

### 🏭 Factory Pattern
**Package:** `pattern.factory`
- **Purpose:** Create different types of channels and messages
- **Key Classes:** `ChannelFactoryProvider`, `MessageFactoryProvider`
- **Types:** TEXT, VOICE, ANNOUNCEMENT channels

### 🔌 Singleton Pattern
**Package:** `pattern.singleton`
- **Purpose:** Manage WebSocket connections globally
- **Key Class:** `WebSocketConnectionManager`
- **Thread-Safe:** Yes (ConcurrentHashMap)

### 📨 Strategy Pattern
**Package:** `pattern.strategy`
- **Purpose:** Different messaging strategies for different channel types
- **Key Classes:** `MessagingContext`, `ChannelMessagingStrategy`, `PrivateMessagingStrategy`, `AnnouncementMessagingStrategy`

### ⚡ Command Pattern
**Package:** `pattern.command`
- **Purpose:** Encapsulate moderation actions (kick, ban, mute, delete)
- **Key Classes:** `CommandInvoker`, `KickUserCommand`, `BanUserCommand`, `MuteUserCommand`, `DeleteMessageCommand`
- **Features:** Undo support, command history

## Quick Usage Examples

### Send a Message (Factory + Observer + Strategy)
```java
// Service automatically uses all three patterns
MessageDTO message = messageService.createMessage(request, userId);
```

### Kick a User (Command)
```java
moderationService.kickUser(targetUserId, serverId, moderatorId);
// Can undo: commandInvoker.undoLastCommand();
```

### Check Online Status (Singleton)
```java
boolean online = connectionManager.isUserConnected(userId);
Set<UUID> activeUsers = connectionManager.getActiveUserIds();
```

### Create a Channel (Factory)
```java
Channel channel = channelFactoryProvider.createChannel(
    ChannelType.VOICE, "Voice Chat", server, settings
);
```

## Project Structure

```
com.example.chatapp/
├── pattern/
│   ├── observer/       # Observer Pattern
│   ├── factory/        # Factory Pattern
│   ├── singleton/      # Singleton Pattern
│   ├── strategy/       # Strategy Pattern
│   └── command/        # Command Pattern
├── service/
│   └── impl/
│       ├── MessageServiceImpl.java      # Uses Factory + Observer + Strategy
│       └── ModerationServiceImpl.java   # Uses Command
├── controller/
│   ├── MessageController.java
│   ├── ModerationController.java
│   └── WebSocketController.java
├── repository/
│   ├── MessageRepository.java
│   ├── ChannelRepository.java
│   ├── MembershipRepository.java
│   └── ...
└── config/
    ├── WebSocketConfig.java
    └── ObserverConfig.java
```

## WebSocket Endpoints

- **Connect:** `/ws` (with SockJS fallback)
- **Topics:**
  - `/topic/channel/{id}` - Channel messages
  - `/topic/server/{id}/announcements` - Server announcements
  - `/queue/user/{id}` - Private messages

## API Endpoints

### Messages
- `POST /api/messages` - Create message
- `PUT /api/messages/{id}` - Update message
- `DELETE /api/messages/{id}` - Delete message
- `GET /api/messages/channel/{id}` - Get channel messages
- `GET /api/messages/search` - Search messages

### Moderation
- `POST /api/moderation/kick` - Kick user
- `POST /api/moderation/ban` - Ban user
- `POST /api/moderation/mute` - Mute user
- `DELETE /api/moderation/message/{id}` - Delete message
- `POST /api/moderation/join-channel` - Join channel

## Task → Pattern Mapping

| Task | Patterns Used |
|------|--------------|
| Send Message | Factory + Observer + Strategy |
| Edit/Delete Message | Observer + Command |
| View Online Members | Singleton |
| Receive Notifications | Observer |
| Create Channel | Factory |
| Kick/Ban/Mute | Command |

See **DESIGN_PATTERNS.md** for detailed documentation.
