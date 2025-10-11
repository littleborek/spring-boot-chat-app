'use strict';


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

const showSignupLink = document.querySelector('#show-signup');
const showLoginLink = document.querySelector('#show-login');
const loginFormContainer = document.querySelector('#login-form-container');
const signupFormContainer = document.querySelector('#signup-form-container');
// Chat
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

// =================================================================
// AUTH
// =================================================================
async function signup() {  }
async function login() {  }
function logout() {  }

async function signup() { const signupRequest = { username: signupUsernameInput.value.trim(), password: signupPasswordInput.value.trim() }; try { const response = await fetch(`${API_BASE_URL}/auth/signup`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(signupRequest) }); if (response.ok) { alert('Kayıt başarılı! Lütfen giriş yapın.'); toggleAuthForms(); signupError.textContent = ''; } else { signupError.textContent = await response.text(); } } catch (error) { signupError.textContent = 'Bir hata oluştu.'; } }
async function login() { const loginRequest = { username: loginUsernameInput.value.trim(), password: loginPasswordInput.value.trim() }; try { const response = await fetch(`${API_BASE_URL}/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(loginRequest) }); if (response.ok) { const data = await response.json(); jwtToken = data.token; username = data.username; localStorage.setItem('jwt_token', jwtToken); localStorage.setItem('username', username); loginError.textContent = ''; showChatView(); } else { loginError.textContent = 'Kullanıcı adı veya şifre hatalı.'; } } catch (error) { loginError.textContent = 'Bir hata oluştu.'; } }
function logout() { if (stompClient) { stompClient.disconnect(); } localStorage.removeItem('jwt_token'); localStorage.removeItem('username'); showLoginView(); }
function toggleAuthForms() { loginFormContainer.classList.toggle('hidden'); signupFormContainer.classList.toggle('hidden'); }

// =================================================================
// ROOMS
// =================================================================
async function loadRooms() {  }
async function createNewRoom() {  }
function addRoomToList(room) {  }


async function loadRooms() { roomList.innerHTML = ''; try { const response = await fetch(`${API_BASE_URL}/rooms`, { headers: { 'Authorization': `Bearer ${jwtToken}` } }); if (response.ok) (await response.json()).forEach(addRoomToList); } catch (error) { console.error('Odalar yüklenirken hata:', error); } }
async function createNewRoom() { const roomName = newRoomNameInput.value.trim(); if (!roomName) return; try { const response = await fetch(`${API_BASE_URL}/rooms`, { method: 'POST', headers: { 'Authorization': `Bearer ${jwtToken}`, 'Content-Type': 'application/x-www-form-urlencoded' }, body: `name=${encodeURIComponent(roomName)}` }); if (response.ok) { addRoomToList(await response.json()); newRoomNameInput.value = ''; } else { alert('Oda oluşturulamadı.'); } } catch (error) { console.error('Oda oluştururken hata:', error); } }
function addRoomToList(room) { const roomElement = document.createElement('li'); roomElement.textContent = room.name; roomElement.dataset.roomId = room.id; roomElement.addEventListener('click', () => joinRoom(room)); roomList.appendChild(roomElement); }

// =================================================================
// CHAT & WEBSOCKET
// =================================================================
async function joinRoom(room) { // <-async 
    if (currentRoomId === room.id) return;
    if (stompClient) { stompClient.disconnect(); }

    currentRoomId = room.id;
    messageArea.innerHTML = '';
    roomTitle.textContent = room.name;
    
    document.querySelectorAll('#room-list li').forEach(li => li.classList.remove('active'));
    document.querySelector(`#room-list li[data-room-id='${room.id}']`).classList.add('active');
    
    // --- load old message
    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${currentRoomId}/messages`, {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        if (response.ok) {
            const oldMessages = await response.json();
            oldMessages.forEach(message => displayMessage(message)); 
        }
    } catch (error) {
        console.error('Eski mesajlar yüklenirken hata:', error);
    }
    // ---------------------------------------------

    welcomeScreen.classList.add('hidden');
    chatScreen.classList.remove('hidden');

    connectToChat();
}



function connectToChat() {
    if (stompClient) {
        stompClient.disconnect();
    }

    // new SockJS
    const socket = new SockJS('/ws'); 
    stompClient = Stomp.over(socket);

    const headers = {
        'Authorization': 'Bearer ' + jwtToken
    };

    stompClient.connect(headers, onConnected, onError);
}

function onConnected() {
    stompClient.subscribe(`/topic/rooms/${currentRoomId}`, onMessageReceived);
}

function onError(error) { console.error('WebSocket Error:', error); }

function sendMessage() {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient && stompClient.connected) { // <-- stompClient.connected
        stompClient.send(`/app/chat/${currentRoomId}/sendMessage`, {}, JSON.stringify({ content: messageContent }));
        messageInput.value = '';
    } else {
        console.error('Mesaj gönderilemedi. Stomp Client bağlı değil.');
    }
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    displayMessage(message); 
}


function displayMessage(message) {
    const messageElement = document.createElement('li');
    messageElement.classList.add('message-bubble');

    const messageHeader = document.createElement('div');
    messageHeader.classList.add('message-header');

    const usernameElement = document.createElement('span');
    usernameElement.classList.add('username');
    usernameElement.textContent = message.username;

    const timestampElement = document.createElement('span');
    timestampElement.classList.add('timestamp');

    const messageTime = new Date(message.timestamp);
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
    } else {
        messageElement.classList.add('receiver');
    }
    
    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}
// ----------------------------------------------------

// =================================================================
// UI MANAGEMENT
// =================================================================
function showChatView() {  }
function showLoginView() {  }

function showChatView() { authContainer.classList.add('hidden'); chatView.classList.remove('hidden'); username = localStorage.getItem('username'); usernameDisplay.textContent = username; loadRooms(); }
function showLoginView() { chatView.classList.add('hidden'); authContainer.classList.remove('hidden'); jwtToken = null; currentRoomId = null; username = null; welcomeScreen.classList.remove('hidden'); chatScreen.classList.add('hidden'); }

// =================================================================
// APP INITIALIZATION
// =================================================================
const savedToken = localStorage.getItem('jwt_token');
if (savedToken) { jwtToken = savedToken; showChatView(); }
else { showLoginView(); }