const socket = io();

const $ = (id) => document.getElementById(id);
const loginScreen = $('login-screen');
const chatScreen = $('chat-screen');
const messages = $('messages');
const typingIndicator = $('typing-indicator');

// === STAŁE ===
const MAX_FILE_SIZE_MB = 5;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
const TYPING_TIMEOUT_MS = 2000;
const TOAST_DURATION_MS = 3000;

let myNick = null;
let typingTimeout = null;
let lastJoin = null;
let isCurrentlyTyping = false;
const typingUsers = new Set();

// === POŁĄCZENIE ===
socket.on('connect', () => {
    console.log('✅ Połączono z serwerem, socket.id:', socket.id);
    console.log('🔌 Transport:', socket.io.engine.transport.name);

    socket.io.engine.on('upgrade', (transport) => {
        console.log('⬆️ Transport upgraded do:', transport.name);
    });

    socket.emit('list-rooms');

    if (lastJoin) {
        console.log('🔄 Auto-rejoin:', lastJoin);
        socket.emit('join', lastJoin);
    }
});

socket.on('disconnect', (reason) => {
    console.log('❌ Rozłączono:', reason);
});

// === LISTA POKOI ===
socket.on('rooms-list', (roomsArr) => {
    console.log('🏠 Dostępne pokoje:', roomsArr);

    const select = $('room-select');
    const currentValue = select.value;

    select.innerHTML = '<option value="">— Utwórz nowy pokój —</option>';

    roomsArr.forEach(room => {
        const opt = document.createElement('option');
        opt.value = room;
        opt.textContent = room;
        select.appendChild(opt);
    });

    if (roomsArr.includes(currentValue)) {
        select.value = currentValue;
    }
});

// === LOGIN ===
$('join-btn').addEventListener('click', join);
$('nick-input').addEventListener('keydown', (e) => e.key === 'Enter' && join());
$('room-input').addEventListener('keydown', (e) => e.key === 'Enter' && join());

$('room-select').addEventListener('change', () => {
    if ($('room-select').value) {
        $('room-input').value = '';
    }
});
$('room-input').addEventListener('input', () => {
    if ($('room-input').value) {
        $('room-select').value = '';
    }
});

function join() {
    const nick = $('nick-input').value.trim();
    const room = $('room-input').value.trim() || $('room-select').value;

    if (!nick) {
        $('login-error').textContent = 'Podaj nick';
        return;
    }
    if (!room) {
        $('login-error').textContent = 'Wybierz pokój lub wpisz nazwę nowego';
        return;
    }

    console.log(`🚪 Próba dołączenia jako "${nick}" do pokoju "${room}"`);
    lastJoin = { nick, room };
    socket.emit('join', lastJoin);
}

socket.on('join-error', (err) => {
    console.warn('⚠️ Join error:', err);
    $('login-error').textContent = err;
});

// === TOAST ===
function showToast(msg) {
    const toast = $('toast');
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), TOAST_DURATION_MS);
}

socket.on('error-msg', (msg) => {
    console.warn('⚠️ Error:', msg);
    showToast(msg);
});

socket.on('joined', ({ nick, room }) => {
    console.log(`✅ Dołączono jako "${nick}" do pokoju "${room}"`);
    myNick = nick;
    $('room-label').textContent = `Pokój: ${room}`;
    document.title = `${nick} @ ${room}`;
    loginScreen.classList.add('hidden');
    chatScreen.classList.remove('hidden');
    $('msg-input').focus();
});

// === LISTA UŻYTKOWNIKÓW ===
socket.on('room-users', (users) => {
    console.log('👥 Użytkownicy w pokoju:', users);
    $('users-label').textContent = `${users.length} online: ${users.join(', ')}`;
});

// === ODBIERANIE WIADOMOŚCI ===
socket.on('message', (msg) => {
    if (msg.system) {
        console.log('ℹ️ System:', msg.text);
    } else if (msg.image) {
        console.log(`💬 Wiadomość od "${msg.nick}": [obrazek, mime: ${msg.mime}]`);
    } else {
        console.log(`💬 Wiadomość od "${msg.nick}":`, msg.text);
    }
    addMessage(msg);
});

function addMessage(msg) {
    const div = document.createElement('div');

    if (msg.system) {
        div.className = 'msg system';
        div.textContent = msg.text;
    } else {
        const mine = msg.nick === myNick;
        div.className = 'msg ' + (mine ? 'mine' : 'other');

        const meta = document.createElement('div');
        meta.className = 'meta';
        meta.textContent = `${msg.nick} • ${formatTime(msg.time)}`;
        div.appendChild(meta);

        if (msg.text) {
            const p = document.createElement('div');
            p.textContent = msg.text;
            div.appendChild(p);
        }
        if (msg.image) {
            const blob = new Blob([msg.image], { type: msg.mime || 'image/png' });
            const url = URL.createObjectURL(blob);
            const img = document.createElement('img');
            img.src = url;
            div.appendChild(img);
        }
    }

    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}

function formatTime(ts) {
    const d = new Date(ts);
    return d.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });
}

// === WYSYŁANIE TEKSTU ===
$('send-btn').addEventListener('click', sendText);
$('msg-input').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        sendText();
    }
});

$('msg-input').addEventListener('input', () => {
    const hasContent = $('msg-input').value.length > 0;
    sendTyping(hasContent);
});

function sendText() {
    const input = $('msg-input');
    const text = input.value.trim();
    if (!text) return;
    console.log('📤 Wysyłam wiadomość:', text);
    socket.emit('chat-message', { text });
    input.value = '';
    sendTyping(false);
}

// === TYPING INDICATOR ===
function sendTyping(isTyping) {
    if (isTyping === isCurrentlyTyping) return;
    isCurrentlyTyping = isTyping;
    socket.emit('typing', isTyping);

    if (isTyping) {
        clearTimeout(typingTimeout);
        typingTimeout = setTimeout(() => {
            isCurrentlyTyping = false;
            socket.emit('typing', false);
        }, TYPING_TIMEOUT_MS);
    }
}

socket.on('typing', ({ nick, isTyping }) => {
    console.log(`✏️ ${nick} ${isTyping ? 'pisze...' : 'przestał pisać'}`);

    if (isTyping) typingUsers.add(nick);
    else typingUsers.delete(nick);

    const list = Array.from(typingUsers);
    if (list.length === 0) typingIndicator.textContent = '';
    else if (list.length === 1) typingIndicator.textContent = `${list[0]} is typing...`;
    else typingIndicator.textContent = `${list.join(', ')} are typing...`;
});

// === WYSYŁANIE ZDJĘĆ ===
$('image-input').addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (!file) return;

    console.log(`📎 Wybrany plik: ${file.name}, typ: ${file.type}, rozmiar: ${(file.size / 1024).toFixed(1)}KB`);

    if (file.size > MAX_FILE_SIZE_BYTES) {
        showToast(`Plik za duży (max ${MAX_FILE_SIZE_MB}MB)`);
        return;
    }

    const reader = new FileReader();
    reader.onload = () => {
        console.log('📤 Wysyłam obrazek, mime:', file.type);
        socket.emit('chat-message', {
            image: reader.result,
            mime: file.type
        });
    };
    reader.readAsArrayBuffer(file);
    e.target.value = '';
});

// === LEAVE ===
$('leave-btn').addEventListener('click', () => {
    console.log('👋 Wychodzę z pokoju');
    lastJoin = null;
    location.reload();
});