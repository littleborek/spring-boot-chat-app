# Design Patterns Implementation Guide

## Overview

This Spring Boot chat application implements **six design patterns** to support the core functionality outlined in your task analysis. The patterns work together to create a maintainable, scalable, and extensible real-time chat system.

---

## Design Patterns Implemented

### 1. **Observer Pattern** - Message Notifications 🔔

**Location:** `com.example.chatapp.pattern.observer`

**Purpose:** Automatically notify interested parties when messages are created, updated, or deleted.

**Components:**
- `MessageObserver` - Interface for observers
- `MessageSubject` - Subject that manages and notifies observers
- `NotificationObserver` - Creates notifications for message events
- `WebSocketObserver` - Broadcasts messages via WebSocket

**How it works:**
```java
// When a message is created
messageSubject.notifyMessageCreated(message);

// All attached observers are automatically notified
// - NotificationObserver creates database notifications
// - WebSocketObserver broadcasts to WebSocket clients
```

**Benefits:**
- Loose coupling between message creation and notification logic
- Easy to add new observers without modifying existing code
- Supports the "Receive Notifications" task requirement

---

### 2. **Factory Pattern** - Channel & Message Creation 🏭

**Location:** `com.example.chatapp.pattern.factory`

**Purpose:** Create different types of channels (TEXT, VOICE, ANNOUNCEMENT) and messages (TEXT, SYSTEM) with appropriate configurations.

**Components:**

**Channel Factories:**
- `ChannelFactory` - Interface
- `TextChannelFactory` - Creates text channels
- `VoiceChannelFactory` - Creates voice channels with bitrate settings
- `AnnouncementChannelFactory` - Creates read-only announcement channels
- `ChannelFactoryProvider` - Manages all channel factories

**Message Factories:**
- `MessageFactory` - Interface
- `TextMessageFactory` - Creates standard text messages
- `SystemMessageFactory` - Creates system messages (joins, leaves)
- `MessageFactoryProvider` - Manages all message factories

**Usage:**
```java
// Create a channel
Channel channel = channelFactoryProvider.createChannel(
    ChannelType.TEXT, "general", server, null
);

// Create a message
Message message = messageFactoryProvider.createMessage(
    "TEXT", "Hello world", channel, author
);
```

**Benefits:**
- Centralized creation logic for different object types
- Easy to add new channel/message types
- Ensures proper initialization and configuration
- Supports "Create Channel" and "Send Message" tasks

---

### 3. **Singleton Pattern** - WebSocket Connection Manager 🔌

**Location:** `com.example.chatapp.pattern.singleton`

**Purpose:** Maintain a single, thread-safe instance that manages all active WebSocket connections.

**Component:**
- `WebSocketConnectionManager` - Thread-safe singleton using Spring's `@Component`

**Features:**
- Track active WebSocket sessions per user
- Check user online status
- Get all connected users
- Thread-safe using `ConcurrentHashMap`

**Usage:**
```java
// Register a connection
connectionManager.registerSession(userId, session);

// Check if user is online
boolean isOnline = connectionManager.isUserConnected(userId);

// Get active users
Set<UUID> activeUsers = connectionManager.getActiveUserIds();
```

**Benefits:**
- Single source of truth for connection state
- Thread-safe for concurrent access
- Supports "View Online Members" task

---

### 4. **Strategy Pattern** - Messaging Strategies 📨

**Location:** `com.example.chatapp.pattern.strategy`

**Purpose:** Use different messaging strategies based on the channel type (public channel, private DM, announcement).

**Components:**
- `MessagingStrategy` - Strategy interface
- `ChannelMessagingStrategy` - Broadcasts to all channel members
- `PrivateMessagingStrategy` - Sends to specific users only
- `AnnouncementMessagingStrategy` - Broadcasts server-wide announcements
- `MessagingContext` - Selects and executes the appropriate strategy

**How it works:**
```java
// Context automatically selects strategy based on channel type
String strategyType = determineStrategyType(channel);
messagingContext.executeStrategy(strategyType, message, recipients);
```

**Strategy Selection:**
- `DM` channels → `PrivateMessagingStrategy`
- `ANNOUNCEMENT` channels → `AnnouncementMessagingStrategy`
- `TEXT` channels → `ChannelMessagingStrategy`

**Benefits:**
- Different messaging behaviors without complex conditionals
- Easy to add new messaging strategies
- Supports different communication patterns (public, private, announcements)

---

### 5. **Command Pattern** - Moderation & User Actions ⚡

**Location:** `com.example.chatapp.pattern.command`

**Purpose:** Encapsulate moderation actions as objects, enabling execution, undo, and audit logging.

**Components:**
- `Command` - Command interface with execute/undo/getName
- `KickUserCommand` - Removes user from server
- `BanUserCommand` - Permanently removes user
- `MuteUserCommand` - Prevents user from sending messages
- `DeleteMessageCommand` - Deletes a message
- `JoinChannelCommand` - Adds user to a channel
- `CommandInvoker` - Executes commands and maintains history

**Usage:**
```java
// Create command
Command kickCommand = new KickUserCommand(user, server, repository);

// Execute through invoker
commandInvoker.executeCommand(kickCommand);

// Undo if needed
commandInvoker.undoLastCommand();
```

**Benefits:**
- Actions can be undone
- Command history for audit trails
- Decouples action execution from business logic
- Supports "Moderation Actions" tasks (kick, ban, mute)

---

## Pattern Integration

### Message Creation Flow
```
1. User sends message → MessageController
2. MessageServiceImpl uses MessageFactory to create message
3. Message saved to database
4. MessageSubject notifies all observers
5. WebSocketObserver broadcasts via WebSocket
6. NotificationObserver creates notifications
7. MessagingContext selects strategy based on channel type
8. Strategy sends message to appropriate recipients
```

### Moderation Action Flow
```
1. Moderator triggers action → ModerationController
2. ModerationServiceImpl validates permissions
3. Creates appropriate Command object
4. CommandInvoker executes command
5. Command modifies database
6. Action logged in command history
7. Can be undone via commandInvoker.undoLastCommand()
```

---

## How Patterns Support Task Requirements

### 1. **Onboarding & Authentication**
- Uses existing JWT security
- User entities and repositories

### 2. **Core Communication**

| Task | Pattern(s) Used |
|------|----------------|
| Send Message | Factory (create) + Observer (notify) + Strategy (deliver) |
| Edit/Delete Message | Command (delete) + Observer (notify) |
| Upload File | Factory (create with attachment metadata) |
| View Online Members | Singleton (WebSocket manager) |
| Search Messages | Repository pattern (MessageRepository) |
| Receive Notifications | Observer (automatic notification creation) |

### 3. **Community Management**

| Task | Pattern(s) Used |
|------|----------------|
| Create Channel | Factory (channel creation) |
| Kick/Ban/Mute | Command (moderation actions) |
| Join Channel | Command (join action) |

---

## Key Classes and Their Roles

### Services
- `MessageServiceImpl` - Integrates Factory, Observer, and Strategy patterns
- `ModerationServiceImpl` - Uses Command pattern for all moderation actions

### Repositories
- `MessageRepository` - Message CRUD and search
- `ChannelRepository` - Channel management
- `MembershipRepository` - Server membership tracking
- `NotificationRepository` - Notification storage

### Controllers
- `MessageController` - REST API for messages
- `ModerationController` - REST API for moderation
- `WebSocketController` - WebSocket event handling

### Configuration
- `WebSocketConfig` - Configures STOMP/WebSocket
- `ObserverConfig` - Registers observers with subject

---

## WebSocket Topics

```
/topic/channel/{channelId}          - Channel messages
/topic/channel/{channelId}/updates  - Message updates
/topic/server/{serverId}/announcements - Server announcements
/queue/user/{userId}                - Private messages
/topic/typing                       - Typing indicators
/topic/public                       - General notifications
```

---

## Extension Points

### Adding New Channel Type
1. Create new `ChannelFactory` implementation
2. Add enum to `ChannelType`
3. Spring auto-registers with `ChannelFactoryProvider`

### Adding New Moderation Action
1. Create new `Command` implementation
2. Add method to `ModerationService`
3. Use `CommandInvoker` to execute

### Adding New Observer
1. Implement `MessageObserver`
2. Register in `ObserverConfig`
3. Automatically receives all message events

### Adding New Messaging Strategy
1. Implement `MessagingStrategy`
2. Spring auto-registers with `MessagingContext`
3. Update strategy selection logic if needed

---

## Testing Recommendations

1. **Factory Pattern**: Test each factory creates correct object types
2. **Observer Pattern**: Verify all observers are notified
3. **Command Pattern**: Test execute and undo functionality
4. **Strategy Pattern**: Test correct strategy selection
5. **Singleton Pattern**: Verify thread-safety and single instance
6. **Integration**: Test complete message flow end-to-end

---

## Performance Considerations

- **Observer Pattern**: Async notification processing recommended for production
- **Singleton Pattern**: `ConcurrentHashMap` ensures thread-safety
- **Strategy Pattern**: No performance overhead, just delegation
- **Command Pattern**: Consider command history size limits

---

## Summary

This implementation demonstrates professional use of design patterns to solve real-world problems in a chat application:

✅ **Observer** - Decoupled event handling  
✅ **Factory** - Flexible object creation  
✅ **Singleton** - Shared connection management  
✅ **Strategy** - Dynamic behavior selection  
✅ **Command** - Undoable actions with history  

The patterns work together seamlessly to support all the tasks outlined in your requirements while maintaining clean, maintainable, and extensible code.
