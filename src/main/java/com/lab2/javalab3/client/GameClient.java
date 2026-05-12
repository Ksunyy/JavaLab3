package com.lab2.javalab3.client;

import com.lab2.javalab3.common.GameState;
import com.lab2.javalab3.common.Req;
import com.lab2.javalab3.common.Resp;
import com.lab2.javalab3.common.TypeMsg;

import java.io.IOException;
import javafx.application.Platform;

public class GameClient {
    private final GameClientController controller;

    private ClientSocket clientSocket;
    private String playerName;
    private GameState currentState = new GameState();

    public GameClient(GameClientController controller) {
        this.controller = controller;
    }

    public synchronized void connect(String host, int port, String playerName) throws IOException {
        disconnectSilently();

        this.playerName = playerName.trim();
        ClientSocket socket = new ClientSocket(host, port);
        try {
            socket.sendReq(Req.connect(this.playerName));
            Resp response = socket.readResp();
            if (response.getType() == TypeMsg.ERROR) {
                socket.closeConnection();
                throw new IOException(response.getText());
            }
            if (response.getType() != TypeMsg.CONNECTED) {
                socket.closeConnection();
                throw new IOException("Unexpected server response.");
            }

            clientSocket = socket;
            handleServerMessage(response);
            clientSocket.startListener(this);
        } catch (ClassNotFoundException exception) {
            socket.closeConnection();
            throw new IOException("Failed to decode the server response.", exception);
        }
    }

    public void sendReady() {
        sendCommand(TypeMsg.READY);
    }

    public void sendPause() {
        sendCommand(TypeMsg.PAUSE);
    }

    public void sendShoot() {
        sendCommand(TypeMsg.SHOOT);
    }

    public void requestLeaderboard() {
        sendCommand(TypeMsg.LEADERBOARD);
    }

    public synchronized void disconnect() {
        if (clientSocket != null) {
            try {
                clientSocket.sendReq(Req.command(TypeMsg.DISCONNECT, playerName));
            } catch (IOException ignored) {
                // Ignore send failures during shutdown.
            }
        }
        disconnectSilently();
    }

    public void handleServerMessage(Resp resp) {
        if (resp == null) {
            return;
        }

        if (resp.getGameState() != null) {
            currentState = resp.getGameState();
            runOnUiThread(() -> controller.updateGameState(currentState, playerName));
        }

        switch (resp.getType()) {
            case CONNECTED, STATE, INFO -> {
                if (resp.getText() != null && !resp.getText().isBlank()) {
                    runOnUiThread(() -> controller.setStatusText(resp.getText()));
                }
            }
            case GAME_OVER -> runOnUiThread(() -> controller.showGameOver(resp.getText()));
            case LEADERBOARD -> runOnUiThread(() -> controller.showLeaderboard(resp.getLeaderboard()));
            case ERROR -> runOnUiThread(() -> controller.showError(resp.getText()));
            default -> {
                // Nothing else to do.
            }
        }
    }

    public void handleConnectionLost(String text) {
        disconnectSilently();
        runOnUiThread(() -> controller.handleConnectionLost(text));
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isConnected() {
        return clientSocket != null;
    }

    private synchronized void sendCommand(TypeMsg type) {
        if (clientSocket == null) {
            return;
        }

        try {
            clientSocket.sendReq(Req.command(type, playerName));
        } catch (IOException exception) {
            handleConnectionLost("The connection to the server was lost.");
        }
    }

    private synchronized void disconnectSilently() {
        if (clientSocket != null) {
            clientSocket.closeConnection();
            clientSocket = null;
        }
    }

    private void runOnUiThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
