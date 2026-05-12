package com.lab2.javalab3.client;

import com.lab2.javalab3.common.Resp;

public class MessageListener extends Thread {
    private final ClientSocket clientSocket;
    private final GameClient gameClient;

    private volatile boolean stop;

    public MessageListener(ClientSocket clientSocket, GameClient gameClient) {
        this.clientSocket = clientSocket;
        this.gameClient = gameClient;
        setName("client-message-listener");
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                Resp resp = clientSocket.readResp();
                gameClient.handleServerMessage(resp);
            }
        } catch (Exception exception) {
            if (!stop) {
                gameClient.handleConnectionLost("The connection to the server was lost.");
            }
        }
    }

    public void stopListener() {
        stop = true;
        interrupt();
    }
}
