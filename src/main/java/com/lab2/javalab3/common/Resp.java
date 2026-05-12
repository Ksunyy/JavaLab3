package com.lab2.javalab3.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Resp implements Serializable {
    private static final long serialVersionUID = 1L;

    private TypeMsg type;
    private String text;
    private GameState gameState;
    private List<LeaderboardEntry> leaderboard;

    public Resp(TypeMsg type, String text, GameState gameState) {
        this(type, text, gameState, new ArrayList<>());
    }

    public Resp(TypeMsg type, String text, GameState gameState, List<LeaderboardEntry> leaderboard) {
        this.type = type;
        this.text = text;
        this.gameState = gameState;
        this.leaderboard = leaderboard == null ? new ArrayList<>() : new ArrayList<>(leaderboard);
    }

    public static Resp withState(TypeMsg type, String text, GameState gameState) {
        return new Resp(type, text, gameState);
    }

    public static Resp withLeaderboard(String text, GameState gameState, List<LeaderboardEntry> leaderboard) {
        return new Resp(TypeMsg.LEADERBOARD, text, gameState, leaderboard);
    }

    public TypeMsg getType() {
        return type;
    }

    public void setType(TypeMsg type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderboardEntry> leaderboard) {
        this.leaderboard = leaderboard == null ? new ArrayList<>() : new ArrayList<>(leaderboard);
    }
}
