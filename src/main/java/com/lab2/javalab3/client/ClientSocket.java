package com.lab2.javalab3.client;

import com.lab2.javalab3.common.Req;
import com.lab2.javalab3.common.Resp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocket {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final Object writeLock = new Object();

    private MessageListener messageListener;

    public ClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendReq(Req req) throws IOException {
        synchronized (writeLock) {
            out.writeObject(req);
            out.flush();
            out.reset();
        }
    }

    public Resp readResp() throws IOException, ClassNotFoundException {
        return (Resp) in.readObject();
    }

    public void startListener(GameClient gameClient) {
        messageListener = new MessageListener(this, gameClient);
        messageListener.start();
    }

    public void closeConnection() {
        if (messageListener != null) {
            messageListener.stopListener();
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // Ignore shutdown errors.
        }
    }
}
