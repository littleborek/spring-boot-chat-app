# Swagger UI Integration Guide

## 🎯 Overview

Swagger UI has been successfully integrated into your Spring Boot Chat Application. This provides an interactive API documentation interface where you can explore and test all endpoints.

---

## 📦 What Was Added

### 1. **Dependency** (Already in pom.xml)
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

### 2. **Swagger Configuration**
Created: `src/main/java/com/example/chatapp/config/SwaggerConfig.java`

Features:
- ✅ JWT Bearer token authentication
- ✅ API metadata (title, version, description)
- ✅ Server configurations (local & production)
- ✅ Contact information
- ✅ MIT License
- ✅ Design patterns documentation in description

### 3. **Security Configuration**
Updated: `SecurityConfig.java` (Already configured)

Whitelisted endpoints:
```java
"/v3/api-docs/**",    // OpenAPI JSON
"/swagger-ui/**",      // Swagger UI static resources
```

### 4. **Controller Annotations**
All controllers now have:
- `@Tag` - Groups endpoints by feature
- `@Operation` - Describes each endpoint
- `@ApiResponses` - Documents response codes (on AuthController)
- `@SecurityRequirement` - Marks protected endpoints
- `@Parameter(hidden = true)` - Hides Authorization headers from UI

---

## 🚀 How to Access Swagger UI

### Step 1: Start the Application
```bash
mvn spring-boot:run
```

### Step 2: Open Swagger UI
Navigate to:
```
http://localhost:8080/swagger-ui/index.html
```

Or use the shorter URL:
```
http://localhost:8080/swagger-ui.html
```

### Step 3: View OpenAPI JSON
For programmatic access:
```
http://localhost:8080/v3/api-docs
```

---

## 🔐 Testing Protected Endpoints

Most endpoints require JWT authentication. Here's how to test them:

### 1. Register a New User
1. Expand **Authentication** section
2. Click on `POST /api/users/register`
3. Click **Try it out**
4. Enter user data:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```
5. Click **Execute**

### 2. Login to Get JWT Token
1. Click on `POST /api/users/login`
2. Click **Try it out**
3. Enter credentials:
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```
4. Click **Execute**
5. **Copy the token** from the response

### 3. Authorize Swagger UI
1. Click the **🔓 Authorize** button (top right)
2. Enter: `Bearer <your-token>` (replace `<your-token>` with actual token)
3. Click **Authorize**
4. Click **Close**

### 4. Test Protected Endpoints
Now you can test any endpoint! For example:
1. Expand **Servers** section
2. Click `POST /api/servers`
3. Click **Try it out**
4. Enter server data:
```json
{
  "name": "My Server",
  "description": "Test server",
  "settings": "{}"
}
```
5. Click **Execute**
6. See the response with created server!

---

## 📚 API Organization

Swagger UI organizes endpoints into these groups:

| Tag | Description | Design Pattern |
|-----|-------------|----------------|
| **Authentication** | Register & Login | - |
| **Servers** | Server management & membership | - |
| **Channels** | Channel creation & retrieval | **Factory Pattern** |
| **Messages** | Messaging with search | **Factory + Observer + Strategy** |
| **Notifications** | User notifications | **Observer Pattern** |
| **Presence** | Online status tracking | **Singleton Pattern** |
| **Moderation** | Kick, ban, mute actions | **Command Pattern** |

---

## 🎨 Features in Swagger UI

### Interactive Testing
- ✅ **Try it out** - Execute real API calls
- ✅ **View responses** - See actual response data
- ✅ **Request/Response schemas** - Understand data structures
- ✅ **Example values** - Pre-filled sample data

### Documentation
- ✅ **Endpoint descriptions** - What each endpoint does
- ✅ **Parameter details** - Required vs optional fields
- ✅ **Response codes** - 200, 400, 401, 404, 500 explained
- ✅ **Model schemas** - DTOs and entities documented

### Authentication
- ✅ **JWT Bearer token** - Global authorization
- ✅ **Padlock icons** - Shows which endpoints need auth
- ✅ **Auto-header injection** - Authorization header added automatically

---

## 🔧 Customization Options

### Change API Information
Edit `SwaggerConfig.java`:
```java
.info(new Info()
    .title("Your Custom Title")
    .version("2.0.0")
    .description("Your custom description")
```

### Add More Servers
```java
.servers(List.of(
    new Server().url("http://localhost:8080").description("Local"),
    new Server().url("https://staging.example.com").description("Staging"),
    new Server().url("https://api.example.com").description("Production")
))
```

### Customize Theme
Add to `application.properties`:
```properties
# Swagger UI customization
springdoc.swagger-ui.path=/api-docs
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true
```

---

## 📖 Example Workflow

Here's a complete test scenario:

### 1. Create a Server
```
POST /api/servers
{
  "name": "Gaming Server",
  "description": "A server for gamers",
  "settings": "{\"public\": true}"
}
```

### 2. Create a Channel
```
POST /api/channels
{
  "serverId": "<server-id-from-step-1>",
  "name": "general",
  "type": "TEXT",
  "settings": "{}"
}
```

### 3. Send a Message
```
POST /api/messages
{
  "channelId": "<channel-id-from-step-2>",
  "content": "Hello everyone!",
  "type": "TEXT"
}
```

### 4. Check Notifications
```
GET /api/notifications
```

### 5. Update Presence
```
PUT /api/presence/status?status=ONLINE
```

### 6. See Online Members
```
GET /api/presence/server/<server-id>/online
```

---

## 🐛 Troubleshooting

### Swagger UI Not Loading
**Problem**: 404 when accessing `/swagger-ui.html`

**Solution**:
1. Check if app is running: `mvn spring-boot:run`
2. Try full path: `http://localhost:8080/swagger-ui/index.html`
3. Verify dependency in `pom.xml`
4. Check SecurityConfig whitelists Swagger URLs

### Authentication Not Working
**Problem**: 401 Unauthorized on protected endpoints

**Solution**:
1. Ensure you're logged in and have a valid token
2. Click **Authorize** button
3. Enter: `Bearer <token>` (with "Bearer " prefix)
4. Token expires after configured time - login again if expired

### Endpoints Not Showing
**Problem**: Some controllers missing

**Solution**:
1. Ensure controller has `@RestController` annotation
2. Check if controller is in scanned package (`com.example.chatapp`)
3. Rebuild: `mvn clean compile`
4. Restart application

---

## 🌐 Production Deployment

Before deploying to production:

### 1. Disable Swagger in Production (Optional)
```properties
# application-prod.properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

### 2. Or Secure Swagger
Add authentication to Swagger endpoints in `SecurityConfig`:
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
    .hasRole("ADMIN")
```

### 3. Update Server URLs
In `SwaggerConfig.java`, update production URL:
```java
new Server()
    .url("https://your-actual-domain.com")
    .description("Production Server")
```

---

## 📊 Additional Resources

### OpenAPI Specification
Your API spec is available at:
```
http://localhost:8080/v3/api-docs
```

Download and use with:
- **Postman** - Import OpenAPI spec
- **Insomnia** - Import OpenAPI spec
- **OpenAPI Generator** - Generate client SDKs

### Swagger Annotations Reference
```java
@Tag(name = "Name", description = "Description")
@Operation(summary = "Short description", description = "Long description")
@ApiResponse(responseCode = "200", description = "Success")
@Parameter(description = "Parameter info", required = true)
@SecurityRequirement(name = "bearer-jwt")
```

---

## ✅ Summary

**Swagger UI is now fully integrated!**

- ✅ Access at: `http://localhost:8080/swagger-ui.html`
- ✅ All 7 controllers documented
- ✅ JWT authentication configured
- ✅ Design patterns highlighted in descriptions
- ✅ Interactive testing enabled
- ✅ Production-ready configuration

**Next Steps:**
1. Start the app: `mvn spring-boot:run`
2. Open Swagger UI: `http://localhost:8080/swagger-ui.html`
3. Register a user
4. Login to get token
5. Authorize with token
6. Test all endpoints!

Enjoy your interactive API documentation! 🚀
