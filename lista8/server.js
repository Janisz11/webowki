const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const path = require('path');

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
    maxHttpBufferSize: 6e6 // 6 MB — twardy limit Socket.IO, handler sprawdza 5MB
});

app.use(express.static(path.join(__dirname, 'public')));

// Stan w pamięci: dla każdego pokoju zbiór nicków
const rooms = new Map(); // roomName -> Set<nick>

function getUsers(room) {
    return Array.from(rooms.get(room) || []);
}

function broadcastRoomsList() {
    io.emit('rooms-list', Array.from(rooms.keys()));
}

// Fix 1 — sanityzacja stringów
function sanitizeString(str) {
    return String(str)
        .slice(0, 30)
        .trim()
        .replace(/[<>&"'`]/g, '')
        .replace(/\s+/g, ' ');
}

io.on('connection', (socket) => {
    console.log('Client connected:', socket.id);

    socket.on('list-rooms', () => {
        socket.emit('rooms-list', Array.from(rooms.keys()));
    });

    socket.on('join', (data) => {
        // Fix 4 — walidacja typów
        if (!data || typeof data !== 'object' || Array.isArray(data)) return;
        let { nick, room } = data;
        if (typeof nick !== 'string' || typeof room !== 'string') return;

        // Fix 1 — sanityzacja
        nick = sanitizeString(nick);
        room = sanitizeString(room);
        if (!nick || !room) return;

        // Fix 3 — opuść poprzedni pokój jeśli już gdzieś jest
        if (socket.data.nick && socket.data.room) {
            const oldRoom = socket.data.room;
            const oldNick = socket.data.nick;

            const oldSet = rooms.get(oldRoom);
            if (oldSet) {
                oldSet.delete(oldNick);
                if (oldSet.size === 0) rooms.delete(oldRoom);
            }
            socket.leave(oldRoom);
            socket.to(oldRoom).emit('message', {
                system: true,
                text: `${oldNick} opuścił pokój`,
                time: Date.now()
            });
            io.to(oldRoom).emit('room-users', getUsers(oldRoom));
        }

        // Sprawdź czy nick zajęty w pokoju
        const usersInRoom = rooms.get(room) || new Set();
        if (usersInRoom.has(nick)) {
            socket.emit('join-error', 'Nick zajęty w tym pokoju');
            return;
        }

        socket.data.nick = nick;
        socket.data.room = room;
        socket.join(room);

        if (!rooms.has(room)) rooms.set(room, new Set());
        rooms.get(room).add(nick);

        // Powiadom pokój
        socket.to(room).emit('message', {
            system: true,
            text: `${nick} dołączył do pokoju`,
            time: Date.now()
        });

        // Wyślij listę użytkowników do wszystkich w pokoju
        io.to(room).emit('room-users', getUsers(room));
        socket.emit('joined', { nick, room });
        broadcastRoomsList();
    });

    socket.on('chat-message', (payload) => {
    // Fix 4 — walidacja typów
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) return;

    const { nick, room } = socket.data;
    if (!nick || !room) return;

    const msg = {
        nick,
        time: Date.now()
    };

    if (payload.text) {
        // Fix 4 — text musi być stringiem
        if (typeof payload.text !== 'string') return;
        msg.text = payload.text.slice(0, 2000);
    } else if (payload.image) {
        // Fix 2 — whitelist MIME typów
        const ALLOWED_MIMES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];
        const mime = String(payload.mime || '').toLowerCase();
        if (!ALLOWED_MIMES.includes(mime)) {
            socket.emit('error-msg', 'Niedozwolony typ pliku');
            return;
        }

        // Sprawdź rozmiar
        if (!payload.image.byteLength || payload.image.byteLength === 0) {
            socket.emit('error-msg', 'Plik jest pusty');
            return;
        }
        if (payload.image.byteLength > 5 * 1024 * 1024) {
            socket.emit('error-msg', 'Plik za duży (max 5MB)');
            return;
        }

        // Sprawdź magic bytes
        const bytes = new Uint8Array(payload.image.slice(0, 4));
        const isPNG  = bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4E && bytes[3] === 0x47;
        const isJPEG = bytes[0] === 0xFF && bytes[1] === 0xD8 && bytes[2] === 0xFF;
        const isGIF  = bytes[0] === 0x47 && bytes[1] === 0x49 && bytes[2] === 0x46;
        const isWEBP = bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x57;

        if (!isPNG && !isJPEG && !isGIF && !isWEBP) {
            socket.emit('error-msg', 'Plik nie jest prawidłowym obrazkiem');
            return;
        }

        msg.image = payload.image;
        msg.mime = mime;
    } else {
        return;
    }

    io.to(room).emit('message', msg);
});
    socket.on('typing', (isTyping) => {
        // Fix 4 — typing musi być booleanem
        if (typeof isTyping !== 'boolean') return;

        const { nick, room } = socket.data;
        if (!nick || !room) return;
        socket.to(room).emit('typing', { nick, isTyping });
    });

    socket.on('disconnect', () => {
        const { nick, room } = socket.data;
        if (!nick || !room) return;

        const set = rooms.get(room);
        if (set) {
            set.delete(nick);
            if (set.size === 0) rooms.delete(room);
        }

        socket.to(room).emit('message', {
            system: true,
            text: `${nick} opuścił pokój`,
            time: Date.now()
        });
        socket.to(room).emit('typing', { nick, isTyping: false });
        io.to(room).emit('room-users', getUsers(room));
        broadcastRoomsList();

        console.log('Client disconnected:', socket.id);
    });
});

const PORT = 3000;
server.listen(PORT, () => {
    console.log(`Chat server running on http://localhost:${PORT}`);
});