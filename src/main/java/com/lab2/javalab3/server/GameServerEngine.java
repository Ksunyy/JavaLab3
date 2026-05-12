package com.lab2.javalab3.server;

public class GameServerEngine extends Thread {
    private static final int FRAME_DELAY_MS = 33;

    private final GameServer gameServer;

    private volatile boolean running;
    private volatile boolean shutdownRequested;

    public GameServerEngine(GameServer gameServer) {
        this.gameServer = gameServer;
        setName("game-server-engine");
        setDaemon(true);
        start();
    }

    public void startRound() {
        running = true;
    }

    public void pauseRound() {
        running = false;
    }

    public void resumeRound() {
        running = true;
    }

    public void stopRound() {
        running = false;
    }

    public void shutdown() {
        shutdownRequested = true;
        interrupt();
    }

    @Override
    public void run() {
        while (!shutdownRequested) {
            if (running) {
                gameServer.advanceGameTick();
            }

            try {
                Thread.sleep(FRAME_DELAY_MS);
            } catch (InterruptedException ignored) {
                if (shutdownRequested) {
                    return;
                }
            }
        }
    }
}
