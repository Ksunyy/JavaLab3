package com.lab2.javalab3.server;

import com.lab2.javalab3.common.Req;
import com.lab2.javalab3.common.Resp;
import com.lab2.javalab3.common.TypeMsg;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class User extends Thread {
    private final GameServer server;
    private final Socket socket;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private volatile boolean closed;
    private String playerName;

    public User(GameServer server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
        setName("user-" + socket.getPort());
        start();
    }

    @Override
    public void run() {
        try {
            while (!closed) {
                Object rawMessage = in.readObject();
                if (!(rawMessage instanceof Req req)) {
                    continue;
                }

                if (playerName == null) {
                    if (req.getType() != TypeMsg.CONNECT) {
                        sendResp(Resp.withState(TypeMsg.ERROR, "The first message must be CONNECT.", null));
                        break;
                    }

                    if (!server.registerUser(this, req.getPlayerName())) {
                        return;
                    }
                    continue;
                }

                server.handleMessage(this, req);
            }
        } catch (EOFException | SocketException ignored) {
            // The client disconnected gracefully.
        } catch (Exception exception) {
            System.err.println("Connection error for " + playerName + ": " + exception.getMessage());
        } finally {
            server.disconnect(this);
        }
    }

    public synchronized boolean sendResp(Resp resp) {
        if (closed) {
            return false;
        }

        try {
            out.writeObject(resp);
            out.flush();
            out.reset();
            return true;
        } catch (IOException exception) {
            closeConnection();
            return false;
        }
    }

    public synchronized void closeConnection() {
        if (closed) {
            return;
        }

        closed = true;
        try {
            socket.close();
        } catch (IOException ignored) {
            // Nothing else to do.
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
