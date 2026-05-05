const http = require('http');
const websocket = require('ws');

const server = http.createServer((req, res) => {
    res.end("I am connected");
});

const wss = new websocket.Server({ server });

wss.on('headers', (headers, req) => {
    console.log('WebSocket.on headers:\n');
    console.log(headers);
});

wss.on('connection', (ws, req) => {
    ws.send('Welcome, your connection is ready');

    ws.on('message', (msg, isBinary) => {
        console.log('Received message from client:');
        const message = isBinary ? msg : msg.toString();
        console.log(message);
    });
});
setInterval(() => {
    wss.clients.forEach(client => {
        if (client.readyState === websocket.OPEN) {
            client.send(`Server time: ${new Date().toLocaleTimeString()}`);
        }
    });
}, 3000);

console.log('Listening on http://localhost:8000 ...');
server.listen(8000);