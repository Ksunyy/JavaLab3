package com.lab2.javalab3.server;

import com.lab2.javalab3.common.GameState;
import com.lab2.javalab3.common.LeaderboardEntry;
import com.lab2.javalab3.common.Req;
import com.lab2.javalab3.common.Resp;
import com.lab2.javalab3.common.TypeMsg;
import com.lab2.javalab3.common.model.Arrow;
import com.lab2.javalab3.common.model.Participant;
import com.lab2.javalab3.common.model.Target;
import com.lab2.javalab3.server.db.HibernateUtil;
import com.lab2.javalab3.server.db.PlayerStat;
import com.lab2.javalab3.server.db.PlayerStatsDao;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameServer {
    public static final int DEFAULT_PORT = 5000;

    private static final Color[] PLAYER_COLORS = new Color[]{
            new Color(37, 99, 235),
            new Color(5, 150, 105),
            new Color(220, 38, 38),
            new Color(168, 85, 247)
    };

    private final Map<String, User> users = new LinkedHashMap<>();
    private final GameState gameState = new GameState();
    private final GameServerEngine engine = new GameServerEngine(this);
    private final PlayerStatsDao playerStatsDao = new PlayerStatsDao();
    private final int port;

    private ServerSocket serverSocket;

    public GameServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else if (System.getenv("PORT") != null && !System.getenv("PORT").isBlank()) {
            port = Integer.parseInt(System.getenv("PORT"));
        }

        try {
            new GameServer(port).start();
        } catch (Exception exception) {
            System.err.println("Failed to start the server: " + exception.getMessage());
        }
    }

    public void start() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(HibernateUtil::shutdown));
        playerStatsDao.loadLeaderboard();
        serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);

        while (true) {
            Socket userSocket = serverSocket.accept();
            new User(this, userSocket);
        }
    }

    public synchronized boolean registerUser(User user, String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isBlank()) {
            user.sendResp(Resp.withState(TypeMsg.ERROR, "Username must not be empty.", null));
            user.closeConnection();
            return false;
        }
        if (users.size() >= GameState.MAX_PLAYERS) {
            user.sendResp(Resp.withState(TypeMsg.ERROR, "The server already has 4 players.", null));
            user.closeConnection();
            return false;
        }
        if (users.containsKey(name)) {
            user.sendResp(Resp.withState(TypeMsg.ERROR, "This username is already taken.", null));
            user.closeConnection();
            return false;
        }
        if (gameState.isRunning() || gameState.isPaused()) {
            user.sendResp(Resp.withState(TypeMsg.ERROR,
                    "A round is already in progress. Please wait for the next one.", null));
            user.closeConnection();
            return false;
        }

        int laneIndex = allocateLaneIndex();
        PlayerStat playerStat;
        try {
            playerStat = playerStatsDao.findOrCreate(name);
        } catch (RuntimeException exception) {
            user.sendResp(Resp.withState(TypeMsg.ERROR, "Database is not available: " + exception.getMessage(), null));
            user.closeConnection();
            return false;
        }

        Participant participant = new Participant(
                name,
                laneIndex,
                laneCenterY(laneIndex),
                38.0,
                PLAYER_COLORS[laneIndex],
                playerStat.getWins()
        );
        users.put(name, user);
        gameState.getParticipants().add(participant);
        user.setPlayerName(name);

        updateWaitingStatus();
        user.sendResp(Resp.withState(
                TypeMsg.CONNECTED,
                "Connected as " + name + ".",
                gameState.snapshot()
        ));
        broadcast(TypeMsg.INFO, name + " joined the server.");
        System.out.println(name + " connected.");
        return true;
    }

    public synchronized void handleMessage(User user, Req req) {
        if (req == null || user.getPlayerName() == null) {
            return;
        }

        String playerName = user.getPlayerName();
        switch (req.getType()) {
            case READY -> handleReady(playerName);
            case PAUSE -> handlePause(playerName);
            case SHOOT -> handleShot(playerName);
            case LEADERBOARD -> handleLeaderboard(user);
            case DISCONNECT -> disconnect(user);
            default -> {
                // Ignore unsupported commands from the client.
            }
        }
    }

    public synchronized void disconnect(User user) {
        String playerName = user.getPlayerName();
        if (playerName == null || !users.containsKey(playerName)) {
            user.closeConnection();
            return;
        }

        users.remove(playerName);
        Participant participant = gameState.findParticipant(playerName);
        if (participant != null) {
            gameState.getParticipants().remove(participant);
        }
        user.closeConnection();

        if (users.isEmpty()) {
            engine.stopRound();
            gameState.getArrows().clear();
            gameState.setWaitingStatus("Waiting for players to connect.");
            System.out.println(playerName + " disconnected. No players left.");
            return;
        }

        if (gameState.isPaused() && allPlayersReady()) {
            resumeRound();
            return;
        }

        if (!gameState.isRunning() && !gameState.isPaused()) {
            updateWaitingStatus();
        }

        broadcast(TypeMsg.INFO, playerName + " left the game.");
        System.out.println(playerName + " disconnected.");
    }

    public synchronized void advanceGameTick() {
        if (!gameState.isRunning()) {
            return;
        }

        moveTarget(gameState.getBigTarget());
        moveTarget(gameState.getSmallTarget());

        Iterator<Arrow> iterator = gameState.getArrows().iterator();
        while (iterator.hasNext()) {
            Arrow arrow = iterator.next();
            arrow.moveObject(1.0);

            Participant owner = gameState.findParticipant(arrow.getOwnerName());
            if (owner == null) {
                iterator.remove();
                continue;
            }

            Target hitTarget = resolveHit(arrow);
            if (hitTarget != null) {
                owner.setScore(owner.getScore() + hitTarget.getScoreValue());
                iterator.remove();
                if (owner.getScore() >= GameState.REQUIRED_SCORE_TO_WIN) {
                    finishRound(owner.getName());
                    return;
                }
                continue;
            }

            if (arrow.getPositionX() - arrow.getMaxWidth() > GameState.SCREEN_WIDTH) {
                iterator.remove();
            }
        }

        broadcastState();
    }

    private void handleReady(String playerName) {
        Participant participant = gameState.findParticipant(playerName);
        if (participant == null) {
            return;
        }

        participant.setReady(true);

        if (gameState.isPaused()) {
            if (allPlayersReady()) {
                resumeRound();
            } else {
                gameState.setPausedStatus(buildPauseStatus(playerName));
                broadcastState();
            }
            return;
        }

        if (!gameState.isRunning()) {
            if (allPlayersReady()) {
                startNewRound();
            } else {
                updateWaitingStatus();
                broadcastState();
            }
        }
    }

    private void handlePause(String playerName) {
        pauseRound(playerName, playerName + " paused the round.");
    }

    private void pauseRound(String playerName, String message) {
        if (!gameState.isRunning()) {
            return;
        }

        Participant participant = gameState.findParticipant(playerName);
        if (participant == null) {
            return;
        }

        participant.setReady(false);
        engine.pauseRound();
        gameState.setPausedStatus(buildPauseStatus(playerName));
        broadcast(TypeMsg.INFO, message);
    }

    private void handleShot(String playerName) {
        if (!gameState.isRunning()) {
            return;
        }

        Participant participant = gameState.findParticipant(playerName);
        if (participant == null) {
            return;
        }

        participant.setCountShots(participant.getCountShots() + 1);
        gameState.getArrows().add(new Arrow(participant));
        broadcastState();
    }

    private void startNewRound() {
        gameState.prepareNewRound();
        engine.startRound();
        broadcast(TypeMsg.INFO, "The round has started.");
        System.out.println("Round started.");
    }

    private void resumeRound() {
        engine.resumeRound();
        gameState.setRunningStatus("Round resumed.");
        broadcast(TypeMsg.INFO, "The round has resumed.");
        System.out.println("Round resumed.");
    }

    private void finishRound(String winnerName) {
        engine.stopRound();
        saveWinner(winnerName);
        gameState.finishRound(winnerName);
        broadcast(TypeMsg.GAME_OVER, winnerName + " wins the round.");
        System.out.println("Round finished. Winner: " + winnerName);
    }

    private void saveWinner(String winnerName) {
        Participant winner = gameState.findParticipant(winnerName);
        if (winner == null) {
            return;
        }

        try {
            int wins = playerStatsDao.incrementWins(winnerName);
            winner.setWins(wins);
        } catch (RuntimeException exception) {
            System.err.println("Failed to save win for " + winnerName + ": " + exception.getMessage());
            winner.setWins(winner.getWins() + 1);
        }
    }

    private void handleLeaderboard(User user) {
        String playerName = user.getPlayerName();
        if (gameState.isRunning()) {
            pauseRound(playerName, playerName + " opened the leaderboard.");
        }

        try {
            List<LeaderboardEntry> leaderboard = playerStatsDao.loadLeaderboard();
            user.sendResp(Resp.withLeaderboard("Leaderboard loaded.", gameState.snapshot(), leaderboard));
        } catch (RuntimeException exception) {
            user.sendResp(Resp.withState(TypeMsg.ERROR, "Failed to load leaderboard: " + exception.getMessage(),
                    gameState.snapshot()));
        }
    }

    private void moveTarget(Target target) {
        target.moveObject(1.0);
        if (target.getPositionY() > GameState.SCREEN_HEIGHT) {
            target.setPositionY(-target.getMaxHeight());
        }
    }

    private Target resolveHit(Arrow arrow) {
        if (checkCollision(arrow, gameState.getSmallTarget())) {
            return gameState.getSmallTarget();
        }
        if (checkCollision(arrow, gameState.getBigTarget())) {
            return gameState.getBigTarget();
        }
        return null;
    }

    private boolean checkCollision(Arrow arrow, Target target) {
        double pointX = Math.pow(arrow.getPositionX() - target.getCenterX(), 2);
        double pointY = Math.pow(arrow.getPositionY() - target.getCenterY(), 2);
        double radius = target.getMaxWidth() / 2.0;
        return pointX + pointY <= radius * radius;
    }

    private void updateWaitingStatus() {
        if (gameState.getParticipants().isEmpty()) {
            gameState.setWaitingStatus("Waiting for players to connect.");
            return;
        }

        gameState.setWaitingStatus(
                "Connected players: " + gameState.getParticipants().size()
                        + ". Ready: " + readyCount() + "/" + gameState.getParticipants().size() + "."
        );
    }

    private String buildPauseStatus(String pausedBy) {
        return "Paused by " + pausedBy + ". Ready: " + readyCount() + "/"
                + gameState.getParticipants().size() + " to resume.";
    }

    private int readyCount() {
        int ready = 0;
        for (Participant participant : gameState.getParticipants()) {
            if (participant.isReady()) {
                ready++;
            }
        }
        return ready;
    }

    private boolean allPlayersReady() {
        return !gameState.getParticipants().isEmpty() && readyCount() == gameState.getParticipants().size();
    }

    private int allocateLaneIndex() {
        for (int lane = 0; lane < GameState.MAX_PLAYERS; lane++) {
            boolean busy = false;
            for (Participant participant : gameState.getParticipants()) {
                if (participant.getLaneIndex() == lane) {
                    busy = true;
                    break;
                }
            }
            if (!busy) {
                return lane;
            }
        }
        throw new IllegalStateException("No free lanes left.");
    }

    private double laneCenterY(int laneIndex) {
        double laneHeight = GameState.SCREEN_HEIGHT / (double) (GameState.MAX_PLAYERS + 1);
        return laneHeight * (laneIndex + 1);
    }

    private void broadcastState() {
        GameState snapshot = gameState.snapshot();
        List<User> clients = new ArrayList<>(users.values());
        for (User user : clients) {
            if (!user.sendResp(Resp.withState(TypeMsg.STATE, snapshot.getGameStatus(), snapshot))) {
                disconnect(user);
            }
        }
    }

    private void broadcast(TypeMsg type, String text) {
        GameState snapshot = gameState.snapshot();
        List<User> clients = new ArrayList<>(users.values());
        for (User user : clients) {
            if (!user.sendResp(Resp.withState(type, text, snapshot))) {
                disconnect(user);
            }
        }
    }
}
