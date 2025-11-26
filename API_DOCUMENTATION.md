# REST API Documentation

## Overview
This document describes the REST API endpoints for the Spring Boot Chat Application. All endpoints require JWT authentication via the `Authorization` header (except auth endpoints).

**Base URL**: `http://localhost:8080`

**Authentication**: Include JWT token in header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 🔐 Authentication Endpoints

### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 🖥️ Server Endpoints

### Create Server
```http
POST /api/servers
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Gaming Server",
  "description": "A server for gaming enthusiasts",
  "settings": "{\"public\": true}"
}

Response: 201 Created
{
  "id": "uuid",
  "ownerId": "uuid",
  "name": "Gaming Server",
  "description": "A server for gaming enthusiasts",
  "createdAt": "2025-11-25T20:00:00"
}
```

### Get User's Servers
```http
GET /api/servers/user
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": "uuid",
    "ownerId": "uuid",
    "name": "Gaming Server",
    "description": "...",
    "createdAt": "2025-11-25T20:00:00"
  }
]
```

### Get Server by ID
```http
GET /api/servers/{serverId}

Response: 200 OK
{
  "id": "uuid",
  "ownerId": "uuid",
  "name": "Gaming Server",
  "description": "...",
  "createdAt": "2025-11-25T20:00:00"
}
```

### Join Server
```http
POST /api/servers/{serverId}/join
Authorization: Bearer <token>

Response: 200 OK
```

### Leave Server
```http
POST /api/servers/{serverId}/leave
Authorization: Bearer <token>

Response: 200 OK
```

---

## 📺 Channel Endpoints

### Create Channel
```http
POST /api/channels
Authorization: Bearer <token>
Content-Type: application/json

{
  "serverId": "uuid",
  "name": "general",
  "type": "TEXT",
  "settings": "{\"topic\": \"General discussion\"}"
}

Response: 201 Created
{
  "id": "uuid",
  "serverId": "uuid",
  "name": "general",
  "type": "TEXT",
  "settings": "{\"topic\": \"General discussion\"}"
}
```

**Channel Types**: `TEXT`, `VOICE`, `ANNOUNCEMENT`

### Get Server Channels
```http
GET /api/channels/server/{serverId}

Response: 200 OK
[
  {
    "id": "uuid",
    "serverId": "uuid",
    "name": "general",
    "type": "TEXT",
    "settings": "..."
  }
]
```

### Get Channel by ID
```http
GET /api/channels/{channelId}

Response: 200 OK
{
  "id": "uuid",
  "serverId": "uuid",
  "name": "general",
  "type": "TEXT",
  "settings": "..."
}
```

---

## 💬 Message Endpoints

### Send Message
```http
POST /api/messages
Authorization: Bearer <token>
Content-Type: application/json

{
  "channelId": "uuid",
  "content": "Hello everyone!",
  "type": "TEXT"
}

Response: 200 OK
{
  "id": 1,
  "author": {
    "id": "uuid",
    "username": "john_doe",
    "avatarUrl": "..."
  },
  "channelId": "uuid",
  "content": "Hello everyone!",
  "type": "TEXT",
  "createdAt": "2025-11-25T20:00:00"
}
```

**Message Types**: `TEXT`, `SYSTEM`

### Update Message
```http
PUT /api/messages/{messageId}
Authorization: Bearer <token>
Content-Type: application/json

"Updated message content"

Response: 200 OK
{
  "id": 1,
  "content": "Updated message content",
  ...
}
```

### Delete Message
```http
DELETE /api/messages/{messageId}
Authorization: Bearer <token>

Response: 200 OK
```

### Get Channel Messages
```http
GET /api/messages/channel/{channelId}

Response: 200 OK
[
  {
    "id": 1,
    "author": {...},
    "channelId": "uuid",
    "content": "Hello!",
    "type": "TEXT",
    "createdAt": "..."
  }
]
```

### Search Messages
```http
GET /api/messages/search?channelId={channelId}&keyword={keyword}

Example:
GET /api/messages/search?channelId=123&keyword=hello

Response: 200 OK
[
  {
    "id": 1,
    "content": "Hello everyone!",
    ...
  }
]
```

---

## 🔔 Notification Endpoints

### Get User Notifications
```http
GET /api/notifications
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": "uuid",
    "recipientId": "uuid",
    "senderId": "uuid",
    "type": "MESSAGE",
    "messageId": 1,
    "channelId": "uuid",
    "serverId": "uuid",
    "isRead": false,
    "createdAt": "2025-11-25T20:00:00"
  }
]
```

**Notification Types**: `MESSAGE`, `CHANNEL_UPDATE`, `MEMBER_JOIN`, `MEMBER_LEAVE`

### Mark Notification as Read
```http
PUT /api/notifications/{notificationId}/read

Response: 200 OK
```

### Mark All Notifications as Read
```http
PUT /api/notifications/read-all
Authorization: Bearer <token>

Response: 200 OK
```

---

## 👤 Presence Endpoints

### Update Presence Status
```http
PUT /api/presence/status?status={status}
Authorization: Bearer <token>

Example:
PUT /api/presence/status?status=ONLINE

Response: 200 OK
```

**Presence Status**: `ONLINE`, `OFFLINE`, `AWAY`, `DND`

### Get Online Members
```http
GET /api/presence/server/{serverId}/online

Response: 200 OK
[
  {
    "id": "uuid",
    "username": "john_doe",
    "avatarUrl": "..."
  }
]
```

---

## 🛡️ Moderation Endpoints

### Kick User
```http
POST /api/moderation/kick
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetUserId": "uuid",
  "serverId": "uuid",
  "reason": "Violation of rules"
}

Response: 200 OK
```

### Ban User
```http
POST /api/moderation/ban
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetUserId": "uuid",
  "serverId": "uuid",
  "reason": "Repeated violations",
  "duration": 7
}

Response: 200 OK
```

### Mute User
```http
POST /api/moderation/mute
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetUserId": "uuid",
  "serverId": "uuid",
  "reason": "Spamming",
  "duration": 24
}

Response: 200 OK
```

### Delete Message (Moderation)
```http
DELETE /api/moderation/messages/{messageId}
Authorization: Bearer <token>

Response: 200 OK
```

### Undo Last Action
```http
POST /api/moderation/undo
Authorization: Bearer <token>

Response: 200 OK
```

---

## 🔌 WebSocket Endpoints

### WebSocket Connection
```
ws://localhost:8080/ws
```

### STOMP Topics

#### Subscribe to Channel Messages
```javascript
stompClient.subscribe('/topic/messages/{channelId}', (message) => {
  console.log('New message:', JSON.parse(message.body));
});
```

#### Subscribe to Personal Notifications
```javascript
stompClient.subscribe('/user/queue/notifications', (notification) => {
  console.log('New notification:', JSON.parse(notification.body));
});
```

#### Send Message via WebSocket
```javascript
stompClient.send('/app/message', {}, JSON.stringify({
  channelId: 'uuid',
  content: 'Hello via WebSocket!',
  type: 'TEXT'
}));
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "timestamp": "2025-11-25T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-25T20:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing JWT token"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-25T20:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-25T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-11-25T20:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Testing with cURL

### Example: Create Server
```bash
curl -X POST http://localhost:8080/api/servers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Server",
    "description": "Test server",
    "settings": "{}"
  }'
```

### Example: Get User Servers
```bash
curl -X GET http://localhost:8080/api/servers/user \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Example: Create Channel
```bash
curl -X POST http://localhost:8080/api/channels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": "your-server-uuid",
    "name": "general",
    "type": "TEXT",
    "settings": "{}"
  }'
```

---

## Rate Limiting

Currently, there are no rate limits implemented. It's recommended to add rate limiting in production environments.

---

## CORS Configuration

The application allows all origins by default (configured for development). Update CORS settings in production:

```java
// In WebSocketConfig or a separate CORS configuration
.setAllowedOrigins("https://your-production-domain.com")
```

---

## Summary of All Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login user | No |
| POST | `/api/servers` | Create server | Yes |
| GET | `/api/servers/user` | Get user's servers | Yes |
| GET | `/api/servers/{id}` | Get server by ID | No |
| POST | `/api/servers/{id}/join` | Join server | Yes |
| POST | `/api/servers/{id}/leave` | Leave server | Yes |
| POST | `/api/channels` | Create channel | Yes |
| GET | `/api/channels/server/{id}` | Get server channels | No |
| GET | `/api/channels/{id}` | Get channel by ID | No |
| POST | `/api/messages` | Send message | Yes |
| PUT | `/api/messages/{id}` | Update message | Yes |
| DELETE | `/api/messages/{id}` | Delete message | Yes |
| GET | `/api/messages/channel/{id}` | Get channel messages | No |
| GET | `/api/messages/search` | Search messages | No |
| GET | `/api/notifications` | Get notifications | Yes |
| PUT | `/api/notifications/{id}/read` | Mark as read | No |
| PUT | `/api/notifications/read-all` | Mark all as read | Yes |
| PUT | `/api/presence/status` | Update presence | Yes |
| GET | `/api/presence/server/{id}/online` | Get online members | No |
| POST | `/api/moderation/kick` | Kick user | Yes |
| POST | `/api/moderation/ban` | Ban user | Yes |
| POST | `/api/moderation/mute` | Mute user | Yes |
| DELETE | `/api/moderation/messages/{id}` | Delete message | Yes |
| POST | `/api/moderation/undo` | Undo last action | Yes |
