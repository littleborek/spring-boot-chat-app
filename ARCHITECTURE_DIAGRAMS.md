# Architecture Diagrams

## Overall Design Pattern Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT (Browser/App)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ HTTP/REST + WebSocket
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                      CONTROLLERS                             │
│  ┌──────────────┐  ┌────────────┐  ┌──────────────┐        │
│  │   Message    │  │ Moderation │  │  WebSocket   │        │
│  │  Controller  │  │ Controller │  │  Controller  │        │
│  └──────────────┘  └────────────┘  └──────────────┘        │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    SERVICE LAYER                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         MessageServiceImpl                            │  │
│  │  Uses: Factory + Observer + Strategy                  │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         ModerationServiceImpl                         │  │
│  │  Uses: Command Pattern                                │  │
│  └───────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                  DESIGN PATTERNS                             │
│                                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │ Observer       │  │ Factory        │  │ Singleton    │  │
│  │ Pattern        │  │ Pattern        │  │ Pattern      │  │
│  │ ┌──────────┐   │  │ ┌──────────┐   │  │ WebSocket   │  │
│  │ │ Subject  │   │  │ │ Channel  │   │  │ Connection  │  │
│  │ │Observers │   │  │ │ Message  │   │  │ Manager     │  │
│  │ └──────────┘   │  │ │Factories │   │  └──────────────┘  │
│  └────────────────┘  │ └──────────┘   │                    │
│                      └────────────────┘                     │
│  ┌────────────────┐  ┌────────────────┐                    │
│  │ Strategy       │  │ Command        │                    │
│  │ Pattern        │  │ Pattern        │                    │
│  │ ┌──────────┐   │  │ ┌──────────┐   │                    │
│  │ │ Context  │   │  │ │ Invoker  │   │                    │
│  │ │Strategies│   │  │ │ Commands │   │                    │
│  │ └──────────┘   │  │ └──────────┘   │                    │
│  └────────────────┘  └────────────────┘                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                  REPOSITORIES                                │
│  Message │ Channel │ Server │ Membership │ Notification     │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    DATABASE (PostgreSQL)                     │
└──────────────────────────────────────────────────────────────┘
```

---

## Message Flow with Design Patterns

```
┌─────────┐
│  User   │ Sends message
└────┬────┘
     │
     ▼
┌──────────────────┐
│ MessageController│
└────┬─────────────┘
     │
     ▼
┌─────────────────────────────────────────┐
│     MessageServiceImpl                  │
│                                         │
│  1. FACTORY PATTERN                     │
│     ┌────────────────────────┐          │
│     │ MessageFactoryProvider │          │
│     │ createMessage()        │          │
│     └────────────────────────┘          │
│              │                          │
│              ▼                          │
│         [Message Created]               │
│              │                          │
│              ▼                          │
│  2. OBSERVER PATTERN                    │
│     ┌────────────────────────┐          │
│     │   MessageSubject       │          │
│     │ notifyMessageCreated() │          │
│     └──────┬─────────────────┘          │
│            │                            │
│     ┌──────┴───────┐                    │
│     ▼              ▼                    │
│  Notification   WebSocket               │
│   Observer      Observer                │
│     │              │                    │
│     ▼              ▼                    │
│  Create DB    Broadcast                 │
│  Notification via WebSocket             │
│                                         │
│  3. STRATEGY PATTERN                    │
│     ┌────────────────────────┐          │
│     │  MessagingContext      │          │
│     │ executeStrategy()      │          │
│     └──────┬─────────────────┘          │
│            │                            │
│     ┌──────┴───────┬──────────┐         │
│     ▼              ▼          ▼         │
│  Channel      Private    Announcement   │
│  Strategy     Strategy    Strategy      │
│     │              │          │         │
│     └──────────────┴──────────┘         │
│                    │                    │
│                    ▼                    │
│         Message Delivered               │
└─────────────────────────────────────────┘
```

---

## Moderation Action Flow

```
┌──────────────┐
│  Moderator   │ Executes action (kick/ban/mute)
└──────┬───────┘
       │
       ▼
┌─────────────────────┐
│ ModerationController│
└──────┬──────────────┘
       │
       ▼
┌────────────────────────────────────┐
│   ModerationServiceImpl            │
│                                    │
│  1. Validate Permissions           │
│     ├─ Check server owner          │
│     └─ Verify target user          │
│                                    │
│  2. COMMAND PATTERN                │
│     ┌─────────────────┐            │
│     │ CommandInvoker  │            │
│     │ executeCommand()│            │
│     └────────┬────────┘            │
│              │                     │
│              ▼                     │
│     ┌──────────────────┐           │
│     │   KickCommand    │           │
│     │   BanCommand     │           │
│     │   MuteCommand    │           │
│     │ DeleteMessage    │           │
│     └────────┬─────────┘           │
│              │                     │
│              ▼                     │
│         execute()                  │
│              │                     │
│              ▼                     │
│     Database Updated               │
│              │                     │
│              ▼                     │
│   Command saved in history         │
│   (Can be undone later)            │
└────────────────────────────────────┘
```

---

## Observer Pattern Detail

```
┌──────────────────────┐
│   MessageSubject     │ (Observable)
│  ┌────────────────┐  │
│  │ List<Observer> │  │
│  └────────────────┘  │
│                      │
│  + attach(observer)  │
│  + detach(observer)  │
│  + notifyCreated()   │
│  + notifyUpdated()   │
│  + notifyDeleted()   │
└──────────┬───────────┘
           │ notifies
           │
    ┌──────┴────────┐
    │               │
    ▼               ▼
┌─────────────┐ ┌──────────────┐
│Notification │ │  WebSocket   │
│  Observer   │ │   Observer   │
├─────────────┤ ├──────────────┤
│onCreate()   │ │onCreate()    │
│onUpdate()   │ │onUpdate()    │
│onDelete()   │ │onDelete()    │
└─────────────┘ └──────────────┘
     │               │
     ▼               ▼
Creates DB      Broadcasts
Notification    via WebSocket
```

---

## Factory Pattern Detail

```
┌────────────────────────────┐
│ ChannelFactoryProvider     │
│  ┌──────────────────────┐  │
│  │ Map<Type, Factory>   │  │
│  └──────────────────────┘  │
│                            │
│  + createChannel(type,     │
│      name, server)         │
└─────────────┬──────────────┘
              │ delegates to
              │
    ┌─────────┴─────────┬───────────────┐
    ▼                   ▼               ▼
┌────────────┐  ┌──────────────┐  ┌────────────────┐
│   Text     │  │    Voice     │  │  Announcement  │
│  Channel   │  │   Channel    │  │    Channel     │
│  Factory   │  │   Factory    │  │    Factory     │
├────────────┤  ├──────────────┤  ├────────────────┤
│create()    │  │create()      │  │create()        │
│  └─TEXT    │  │  └─VOICE     │  │  └─ANNOUNCEMENT│
│   settings │  │   + bitrate  │  │   + readOnly   │
└────────────┘  └──────────────┘  └────────────────┘

Similar structure for MessageFactoryProvider:
- TextMessageFactory
- SystemMessageFactory
```

---

## Strategy Pattern Detail

```
┌───────────────────────┐
│  MessagingContext     │
│  ┌─────────────────┐  │
│  │Map<Type,        │  │
│  │    Strategy>    │  │
│  └─────────────────┘  │
│                       │
│  + executeStrategy(   │
│      type, message,   │
│      recipients)      │
└──────────┬────────────┘
           │ selects based on channel type
           │
    ┌──────┴───────┬─────────────────┐
    ▼              ▼                 ▼
┌─────────┐  ┌──────────┐  ┌──────────────┐
│ Channel │  │ Private  │  │ Announcement │
│Strategy │  │ Strategy │  │  Strategy    │
├─────────┤  ├──────────┤  ├──────────────┤
│send()   │  │send()    │  │send()        │
│  │      │  │  │       │  │  │           │
│  ▼      │  │  ▼       │  │  ▼           │
│Broadcast│  │Direct to │  │Server-wide   │
│to all   │  │specific  │  │broadcast     │
│members  │  │users     │  │              │
└─────────┘  └──────────┘  └──────────────┘
```

---

## Command Pattern Detail

```
┌──────────────────────┐
│  CommandInvoker      │
│  ┌────────────────┐  │
│  │ List<Command>  │  │  Command History
│  │   history      │  │
│  └────────────────┘  │
│                      │
│  + execute(cmd)      │
│  + undoLast()        │
└──────────┬───────────┘
           │ executes
           │
    ┌──────┴──────┬───────────┬──────────┐
    ▼             ▼           ▼          ▼
┌────────┐  ┌─────────┐  ┌──────┐  ┌────────┐
│  Kick  │  │   Ban   │  │ Mute │  │ Delete │
│Command │  │ Command │  │Command│ │Message │
├────────┤  ├─────────┤  ├──────┤  ├────────┤
│execute │  │execute  │  │execute│ │execute │
│undo    │  │undo     │  │ undo  │ │undo    │
└────────┘  └─────────┘  └──────┘  └────────┘
    │            │           │          │
    ▼            ▼           ▼          ▼
 Remove      Remove &    Prevent    Remove
membership   add to     messaging  from DB
            ban list
```

---

## Singleton Pattern Detail

```
┌──────────────────────────────────────┐
│   WebSocketConnectionManager         │
│   (Spring @Component = Singleton)    │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ ConcurrentHashMap              │  │
│  │   <UUID, WebSocketSession>     │  │  Thread-safe
│  │ activeSessions                 │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │ ConcurrentHashMap              │  │
│  │   <UUID, UUID>                 │  │
│  │ userToSession                  │  │
│  └────────────────────────────────┘  │
│                                      │
│  + registerSession(userId, session)  │
│  + removeSession(userId)             │
│  + getSession(userId)                │
│  + isUserConnected(userId)           │
│  + getActiveUserIds()                │
└──────────────────────────────────────┘
         ▲
         │ Single instance
         │ accessed by all
         │
    ┌────┴───────┬──────────┐
    │            │          │
 Service     Controller  Observer
 Layer       Layer       Pattern
```

---

## Data Flow Diagram

```
Client Request
    │
    ▼
┌─────────────┐
│ Controller  │
└─────┬───────┘
      │
      ▼
┌─────────────┐     ┌──────────────┐
│  Service    │────▶│  Patterns    │
└─────┬───────┘     │ (Orchestrate)│
      │             └──────────────┘
      ▼
┌─────────────┐
│ Repository  │
└─────┬───────┘
      │
      ▼
┌─────────────┐
│  Database   │
└─────────────┘
      │
      ▼
   Response
```

---

## Pattern Interaction Matrix

```
┌────────────┬──────────┬─────────┬───────────┬──────────┬─────────┐
│            │ Observer │ Factory │ Singleton │ Strategy │ Command │
├────────────┼──────────┼─────────┼───────────┼──────────┼─────────┤
│ Observer   │    -     │   ✓     │     ✓     │    ✓     │    -    │
├────────────┼──────────┼─────────┼───────────┼──────────┼─────────┤
│ Factory    │    ✓     │    -    │     -     │    ✓     │    -    │
├────────────┼──────────┼─────────┼───────────┼──────────┼─────────┤
│ Singleton  │    ✓     │    -    │     -     │    ✓     │    -    │
├────────────┼──────────┼─────────┼───────────┼──────────┼─────────┤
│ Strategy   │    ✓     │    ✓    │     ✓     │    -     │    -    │
├────────────┼──────────┼─────────┼───────────┼──────────┼─────────┤
│ Command    │    -     │    -    │     -     │    -     │    -    │
└────────────┴──────────┴─────────┴───────────┴──────────┴─────────┘

✓ = These patterns interact in the implementation
```

This architecture demonstrates a clean separation of concerns with design patterns working together harmoniously!
