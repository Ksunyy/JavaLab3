package com.lab2.javalab3.common;

import com.lab2.javalab3.common.model.Arrow;
import com.lab2.javalab3.common.model.Participant;
import com.lab2.javalab3.common.model.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_PLAYERS = 4;
    public static final int REQUIRED_SCORE_TO_WIN = 6;
    public static final int SCREEN_WIDTH = 650;
    public static final int SCREEN_HEIGHT = 490;

    private final List<Arrow> arrows;
    private final List<Participant> participants;
    private Target smallTarget;
    private Target bigTarget;
    private String gameStatus;
    private String winnerName;
    private boolean running;
    private boolean paused;

    public GameState() {
        this.arrows = new ArrayList<>();
        this.participants = new ArrayList<>();
        resetTargets();
        this.gameStatus = "Waiting for players to connect.";
        this.winnerName = "";
    }

    public GameState(GameState other) {
        this.arrows = new ArrayList<>();
        for (Arrow arrow : other.arrows) {
            this.arrows.add(new Arrow(arrow));
        }

        this.participants = new ArrayList<>();
        for (Participant participant : other.participants) {
            this.participants.add(new Participant(participant));
        }

        this.smallTarget = new Target(other.smallTarget);
        this.bigTarget = new Target(other.bigTarget);
        this.gameStatus = other.gameStatus;
        this.winnerName = other.winnerName;
        this.running = other.running;
        this.paused = other.paused;
    }

    public GameState snapshot() {
        return new GameState(this);
    }

    public void resetTargets() {
        this.smallTarget = Target.createSmallTarget();
        this.bigTarget = Target.createBigTarget();
    }

    public void prepareNewRound() {
        arrows.clear();
        resetTargets();
        winnerName = "";
        running = true;
        paused = false;
        gameStatus = "Round is running.";
        for (Participant participant : participants) {
            participant.setScore(0);
            participant.setCountShots(0);
            participant.setReady(true);
        }
    }

    public void setWaitingStatus(String status) {
        running = false;
        paused = false;
        winnerName = "";
        gameStatus = status;
    }

    public void setPausedStatus(String status) {
        running = false;
        paused = true;
        gameStatus = status;
    }

    public void setRunningStatus(String status) {
        running = true;
        paused = false;
        gameStatus = status;
    }

    public void finishRound(String winner) {
        arrows.clear();
        winnerName = winner;
        running = false;
        paused = false;
        gameStatus = "Winner: " + winner + ". Press Ready to start a new round.";
        for (Participant participant : participants) {
            participant.setReady(false);
        }
    }

    public Participant findParticipant(String name) {
        for (Participant participant : participants) {
            if (participant.getName().equals(name)) {
                return participant;
            }
        }
        return null;
    }

    public List<Arrow> getArrows() {
        return arrows;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public Target getSmallTarget() {
        return smallTarget;
    }

    public Target getBigTarget() {
        return bigTarget;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }
}
