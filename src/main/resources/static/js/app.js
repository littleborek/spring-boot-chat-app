'use strict';

// HTML elements
const authContainer = document.querySelector('#auth-container');
const chatView = document.querySelector('#chat-view');
// Login
const loginForm = document.querySelector('#loginForm');
const loginUsernameInput = document.querySelector('#login-username');
const loginPasswordInput = document.querySelector('#login-password');
const loginError = document.querySelector('#login-error');
// Signup
const signupForm = document.querySelector('#signupForm');
const signupUsernameInput = document.querySelector('#signup-username');
const signupPasswordInput = document.querySelector('#signup-password');
const signupError = document.querySelector('#signup-error');
// Form toggling links
const showSignupLink = document.querySelector('#show-signup');
const showLoginLink = document.querySelector('#show-login');
const loginFormContainer = document.querySelector('#login-form-container');
const signupFormContainer = document.querySelector('#signup-form-container');
// Chat elements
const logoutButton = document.querySelector('#logout-button');
const roomList = document.querySelector('#room-list');
const newRoomForm = document.querySelector('#new-room-form');
const newRoomNameInput = document.querySelector('#new-room-name');
const messageForm = document.querySelector('#messageForm'); 
const messageInput = document.querySelector('#messageInput'); 
const messageArea = document.querySelector('#messageArea');
const roomTitle = document.querySelector('#room-title');
const usernameDisplay = document.querySelector('#username-display');
const welcomeScreen = document.querySelector('#welcome-screen');
const chatScreen = document.querySelector('#chat-screen'); 
const createInviteButton = document.querySelector('#create-invite-button'); 
const joinRoomForm = document.querySelector('#join-room-form');
const inviteCodeInput = document.querySelector('#invite-code-input');

// Global Variables
const API_BASE_URL = 'http://localhost:8080/api';
let jwtToken = null;
let stompClient = null;
let currentRoomId = null;
let username = null;

// =================================================================
// EVENT LISTENERS
// =================================================================
loginForm.addEventListener('submit', (e) => { e.preventDefault(); login(); });
signupForm.addEventListener('submit', (e) => { e.preventDefault(); signup(); });
logoutButton.addEventListener('click', () => logout());
newRoomForm.addEventListener('submit', (e) => { e.preventDefault(); createNewRoom(); });
messageForm.addEventListener('submit', (e) => { e.preventDefault(); sendMessage(); });
showSignupLink.addEventListener('click', (e) => { e.preventDefault(); toggleAuthForms(); });
showLoginLink.addEventListener('click', (e) => { e.preventDefault(); toggleAuthForms(); });
createInviteButton.addEventListener('click', () => createInvite());
joinRoomForm.addEventListener('submit', (e) => { e.preventDefault(); acceptInvite(); }); 

// =================================================================
// AUTH FUNCTIONS (No changes needed here from previous version)
// =================================================================
async function signup() {
    const signupRequest = { username: signupUsernameInput.value.trim(), password: signupPasswordInput.value.trim() };
    signupError.textContent = ''; // Clear previous errors
    try {
        const response = await fetch(`${API_BASE_URL}/auth/signup`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(signupRequest) });
        if (response.ok) {
            alert('Kayıt başarılı! Lütfen giriş yapın.');
            toggleAuthForms(); // Switch to login form
            signupUsernameInput.value = ''; // Clear inputs
            signupPasswordInput.value = '';
        } else {
            const errorText = await response.text();
            signupError.textContent = errorText || 'Kayıt sırasında bir hata oluştu.';
        }
    } catch (error) {
        signupError.textContent = 'Sunucuya bağlanılamadı.';
        console.error('Signup error:', error);
    }
}

async function login() {
    const loginRequest = { username: loginUsernameInput.value.trim(), password: loginPasswordInput.value.trim() };
    loginError.textContent = ''; // Clear previous errors
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' ,'Accept': 'application/json' }, body: JSON.stringify(loginRequest) });
        if (response.ok) {
            const data = await response.json();
            jwtToken = data.token;
            username = data.username; // Use username from response
            localStorage.setItem('jwt_token', jwtToken);
            localStorage.setItem('username', username);
            loginUsernameInput.value = ''; // Clear inputs
            loginPasswordInput.value = '';
            showChatView();
        } else {
            loginError.textContent = 'Kullanıcı adı veya şifre hatalı.';
        }
    } catch (error) {
        loginError.textContent = 'Sunucuya bağlanılamadı.';
        console.error('Login error:', error);
    }
}

function logout() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => { console.log("Disconnected WebSocket."); });
    }
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    showLoginView(); // Go back to login screen
}

// =================================================================
// ROOM FUNCTIONS (No changes needed here from previous version)
// =================================================================
async function loadRooms() {
    roomList.innerHTML = ''; // Clear previous list
    if (!jwtToken) return;
    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, { headers: { 'Authorization': `Bearer ${jwtToken}` } });
        if (response.ok) {
             const rooms = await response.json();
             if (rooms.length === 0) {
                 roomList.innerHTML = '<li class="no-rooms">Henüz oda yok.</li>';
             } else {
                rooms.forEach(addRoomToList);
             }
        } else {
             console.error('Failed to load rooms:', response.status);
             roomList.innerHTML = '<li class="error-message">Odalar yüklenemedi.</li>';
        }
    } catch (error) {
         console.error('Error loading rooms:', error);
         roomList.innerHTML = '<li class="error-message">Odalar yüklenemedi.</li>';
     }
}

async function createNewRoom() {
    const roomName = newRoomNameInput.value.trim();
    if (!roomName || !jwtToken) return;
    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, { method: 'POST', headers: { 'Authorization': `Bearer ${jwtToken}`, 'Content-Type': 'application/x-www-form-urlencoded' }, body: `name=${encodeURIComponent(roomName)}` });
        if (response.ok) {
            const noRoomsLi = roomList.querySelector('.no-rooms');
            if (noRoomsLi) noRoomsLi.remove();
            addRoomToList(await response.json());
            newRoomNameInput.value = '';
        } else {
            const errorText = await response.text();
            alert(`Oda oluşturulamadı: ${errorText || 'Bir hata oluştu.'}`);
        }
    } catch (error) {
        console.error('Error creating room:', error);
        alert('Oda oluştururken bir hata oluştu.');
     }
}

function addRoomToList(room) {
	
	
	
    const roomElement = document.createElement('li');
    roomElement.textContent = room.name;
    roomElement.dataset.roomId = room.id;
    roomElement.addEventListener('click', () => joinRoom(room));
    roomList.appendChild(roomElement);
}

async function createInvite() {
    if (!currentRoomId || !jwtToken) return;
    console.log("createInvite called for room ID:", currentRoomId); // Debug log

    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${currentRoomId}/invites`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        if (response.ok) {
            const inviteCode = await response.text();
            prompt(`Oda için davet kodu (Kopyala):`, inviteCode);
        } else {
            console.error('Failed to create invite:', response.status, await response.text());
            alert('Davet kodu oluşturulamadı. (Yalnızca oda sahibi oluşturabilir)');
        }
    } catch (error) {
        console.error('Error creating invite:', error);
        alert('Davet kodu oluşturulurken bir hata oluştu.');
    }
	
	
}

async function acceptInvite() {
    const inviteCode = inviteCodeInput.value.trim();
    if (!inviteCode || !jwtToken) return;

    try {
        const response = await fetch(`${API_BASE_URL}/invites/${inviteCode}/accept`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });

        if (response.ok) {
            const joinedRoom = await response.json();
            alert(`Başarıyla '${joinedRoom.name}' odasına katıldınız!`);
            inviteCodeInput.value = '';
            // Remove "no rooms" message if present
             const noRoomsLi = roomList.querySelector('.no-rooms');
             if (noRoomsLi) noRoomsLi.remove();
            // Check if room is already in the list before adding
            if (!document.querySelector(`#room-list li[data-room-id='${joinedRoom.id}']`)) {
                addRoomToList(joinedRoom);
            }
             
             joinRoom(joinedRoom);
        } else {
            const errorText = await response.text();
            alert(`Odaya katılamadınız: ${errorText || 'Geçersiz kod veya bir hata oluştu.'}`);
        }
    } catch (error) {
        console.error('Error accepting invite:', error);
        alert('Davet kabul edilirken bir hata oluştu.');
    }
}


// =================================================================
// CHAT & WEBSOCKET FUNCTIONS (Corrected and final versions)
// =================================================================
async function joinRoom(room) {
    if (currentRoomId === room.id && stompClient && stompClient.connected) {
        console.log("Already in room:", room.name);
        return;
    }

    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
            console.log(`Disconnected from previous room ${currentRoomId}`);
            connectAndLoadMessages(room);
        });
    } else {
        connectAndLoadMessages(room);
    }
}

async function connectAndLoadMessages(room) {
    console.log("Joining room:", room.name, room.id);
    currentRoomId = room.id;
    messageArea.innerHTML = '<li><i>Mesajlar yükleniyor...</i></li>';
    roomTitle.textContent = room.name;

    document.querySelectorAll('#room-list li').forEach(li => li.classList.remove('active'));
    const roomLi = document.querySelector(`#room-list li[data-room-id='${room.id}']`);
    if (roomLi) roomLi.classList.add('active');

    welcomeScreen.classList.add('hidden');
    chatScreen.classList.remove('hidden'); // Show the main chat screen container
    createInviteButton.classList.remove('hidden'); // Show the invite button
    messageForm.classList.remove('hidden'); 
    console.log("Chat screen, invite button, and message form should be visible.");

    // Load old messages
    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${currentRoomId}/messages`, {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        messageArea.innerHTML = ''; // Clear loading message
        if (response.ok) {
            const oldMessages = await response.json();
            if (oldMessages.length === 0) {
                 messageArea.innerHTML = '<li><i>Bu odada henüz mesaj yok. İlk mesajı sen at!</i></li>';
            } else {
                oldMessages.forEach(displayMessage);
            }
        } else {
             messageArea.innerHTML = '<li><i>Mesajlar yüklenemedi.</i></li>';
             console.error("Failed to load messages:", response.status);
        }
    } catch (error) {
        console.error('Error loading old messages:', error);
        messageArea.innerHTML = '<li><i>Mesajlar yüklenirken hata oluştu.</i></li>';
    }

    // Connect to WebSocket AFTER UI is ready
    connectToChat();
}

function connectToChat() {
    const socket = new SockJS('/ws'); // Use SockJS
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Turn off verbose logging

    const headers = { 'Authorization': `Bearer ${jwtToken}` };
    stompClient.connect(headers, onConnected, onError);
}

function onConnected() {
    console.log(`WebSocket connected. Subscribing to room ${currentRoomId}`);
    stompClient.subscribe(`/topic/rooms/${currentRoomId}`, onMessageReceived);
}

function onError(error) {
    console.error('WebSocket Connection Error:', error);
    if (!messageArea.innerHTML.includes('Bağlantı hatası')) {
        messageArea.innerHTML += '<li class="error-message"><i>❗️ Mesajlaşma sunucusuna bağlanılamadı. Lütfen sayfayı yenileyin.</i></li>';
    }
}

function sendMessage() {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient && stompClient.connected) {
        stompClient.send(`/app/chat/${currentRoomId}/sendMessage`, {}, JSON.stringify({ content: messageContent }));
        messageInput.value = '';
    } else {
        console.error('Cannot send message. Stomp Client not connected.');
        alert('Mesaj gönderilemedi. Bağlantı aktif değil.'); // User feedback
    }
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);

    const placeholder = messageArea.querySelector('li > i');
    if (placeholder && (placeholder.textContent.includes('mesaj yok') || placeholder.textContent.includes('Yükleniyor'))) {
        messageArea.innerHTML = ''; // Clear placeholder
    }

    // Handle system message for deletion
    if (message.username === "System" && message.content === "Message deleted" && message.messageId) {
        const messageElement = messageArea.querySelector(`li[data-message-id='${message.messageId}']`);
        if (messageElement) messageElement.remove();
    }
    // Handle edited message (Update existing or display new)
    else if (message.messageId) { // Check if messageId exists
         const existingMessageElement = messageArea.querySelector(`li[data-message-id='${message.messageId}']`);
         if (existingMessageElement) {
             // Update existing message bubble
             const contentP = existingMessageElement.querySelector('.content');
             if (contentP) contentP.textContent = message.content + " (Düzenlendi)";
             
         } else {
             
             displayMessage(message);
         }
    }

    else {
        displayMessage(message);
    }
}

function displayMessage(message) {
    const messageId = message.messageId || message.timestamp; // Use timestamp as fallback ID

    const messageElement = document.createElement('li');
    messageElement.classList.add('message-bubble');
    messageElement.dataset.messageId = messageId; // Use the determined ID

    const messageHeader = document.createElement('div');
    messageHeader.classList.add('message-header');

    const usernameElement = document.createElement('span');
    usernameElement.classList.add('username');
    usernameElement.textContent = message.username;

    const timestampElement = document.createElement('span');
    timestampElement.classList.add('timestamp');
    const messageTime = message.timestamp ? new Date(message.timestamp) : new Date();
    timestampElement.textContent = messageTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    messageHeader.appendChild(usernameElement);
    messageHeader.appendChild(timestampElement);
    messageElement.appendChild(messageHeader);

    const contentElement = document.createElement('p');
    contentElement.classList.add('content');
    contentElement.textContent = message.content;
    messageElement.appendChild(contentElement);

    if (message.username === username) {
        messageElement.classList.add('sender');
        const actionsElement = document.createElement('div');
        actionsElement.classList.add('message-actions');
        // Pass the determined ID to the edit/delete functions
        actionsElement.innerHTML = `
            <button onclick="promptEditMessage('${messageId}', this.closest('.message-bubble').querySelector('.content'))" title="Düzenle"><i class='bx bx-pencil'></i></button>
            <button onclick="deleteMessage('${messageId}')" title="Sil"><i class='bx bx-trash'></i></button>
        `;
        messageElement.appendChild(actionsElement);
    } else {
        messageElement.classList.add('receiver');
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight; // Scroll to bottom
}


function deleteMessage(messageId) {
    // Convert ID back to number only if it's not a timestamp string
    const idToSend = (String(messageId).includes('T')) ? null : Number(messageId);
    if (idToSend === null && !String(messageId).includes('T')) { // Check if conversion failed for non-timestamp
         console.error("Invalid message ID for deletion:", messageId);
         return;
    }
    // Backend needs to handle potential timestamp IDs if messageId is missing in DTO
     if (idToSend === null){
        console.warn("Attempting to delete using timestamp ID. Ensure backend supports this or DTO includes messageId.");
        // If backend only supports Long ID, we should stop here.
        // For now, let's assume backend might handle it or fail gracefully.
        // return; // Uncomment this to prevent sending timestamp IDs
     }


    if (confirm('Bu mesajı silmek istediğinize emin misiniz?') && stompClient && stompClient.connected) {
        // Send the ID (could be number or timestamp string based on fallback)
        stompClient.send(`/app/chat/${currentRoomId}/deleteMessage`, {}, JSON.stringify(messageId)); // Send original ID
    }
}

function promptEditMessage(messageId, contentElement) {
    const currentContent = contentElement.textContent.replace(" (Düzenlendi)", "");
    const newContent = prompt("Mesajı düzenle:", currentContent);

    // Convert ID back to number only if it's not a timestamp string
    const idToSend = (String(messageId).includes('T')) ? null : Number(messageId);
     if (idToSend === null && !String(messageId).includes('T')) {
         console.error("Invalid message ID for editing:", messageId);
         return;
     }
     // Backend needs real ID
      if (idToSend === null){
         console.error("Cannot edit message with timestamp ID. Backend DTO requires messageId.");
         alert("Mesaj düzenlenemiyor. Backend DTO güncellemesi gerekli (messageId eksik).");
         return;
     }

    if (newContent !== null && newContent.trim() !== '' && newContent !== currentContent && stompClient && stompClient.connected) {
        const editRequest = { messageId: idToSend, newContent: newContent.trim() }; // Send numeric ID
        stompClient.send(`/app/chat/${currentRoomId}/editMessage`, {}, JSON.stringify(editRequest));
    }
}


// =================================================================
// UI MANAGEMENT
// =================================================================
function showChatView() {
    authContainer.classList.add('hidden');
    chatView.classList.remove('hidden');
    username = localStorage.getItem('username');
    if (username) {
        usernameDisplay.textContent = username;
        loadRooms(); // Load rooms when chat view is shown
    } else {
        console.error("Username not found in localStorage. Logging out.");
        logout(); // Logout if essential data is missing
    }
}

function showLoginView() {
    chatView.classList.add('hidden');
    authContainer.classList.remove('hidden');
    // Clear state thoroughly
    jwtToken = null;
    currentRoomId = null;
    username = null;
    stompClient = null;
    // Reset UI
    loginError.textContent = '';
    signupError.textContent = '';
    if(roomList) roomList.innerHTML = ''; // Ensure elements exist before clearing
    if(messageArea) messageArea.innerHTML = '';
    if(roomTitle) roomTitle.textContent = 'Bir oda seçin...';
    if(welcomeScreen) welcomeScreen.classList.remove('hidden');
    if(chatScreen) chatScreen.classList.add('hidden');
    if(createInviteButton) createInviteButton.classList.add('hidden');
    if(messageForm) messageForm.classList.add('hidden'); // Also hide message form on logout
}

function toggleAuthForms() {
    loginFormContainer.classList.toggle('hidden');
    signupFormContainer.classList.toggle('hidden');
    loginError.textContent = ''; // Clear errors on toggle
    signupError.textContent = '';
}

// =================================================================
// APP INITIALIZATION
// =================================================================
const savedToken = localStorage.getItem('jwt_token');
const savedUsername = localStorage.getItem('username');

if (savedToken && savedUsername) {
    jwtToken = savedToken;
    username = savedUsername;
    showChatView();
} else {
    localStorage.removeItem('jwt_token'); // Clean up partial storage
    localStorage.removeItem('username');
    showLoginView();
}