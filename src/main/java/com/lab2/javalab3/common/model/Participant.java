package com.lab2.javalab3.common.model;

import java.awt.Color;
import java.io.Serializable;

public class Participant implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean ready;
    private int countShots;
    private int score;
    private int wins;
    private final String name;
    private final int laneIndex;
    private final double positionY;
    private final double positionX;
    private final Color color;

    public Participant(String name, int laneIndex, double positionY, double positionX, Color color) {
        this(name, laneIndex, positionY, positionX, color, 0);
    }

    public Participant(String name, int laneIndex, double positionY, double positionX, Color color, int wins) {
        this.name = name;
        this.laneIndex = laneIndex;
        this.positionY = positionY;
        this.positionX = positionX;
        this.color = color;
        this.wins = wins;
    }

    public Participant(Participant other) {
        this.ready = other.ready;
        this.countShots = other.countShots;
        this.score = other.score;
        this.wins = other.wins;
        this.name = other.name;
        this.laneIndex = other.laneIndex;
        this.positionY = other.positionY;
        this.positionX = other.positionX;
        this.color = new Color(other.color.getRGB(), true);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getCountShots() {
        return countShots;
    }

    public void setCountShots(int countShots) {
        this.countShots = countShots;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public String getName() {
        return name;
    }

    public int getLaneIndex() {
        return laneIndex;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getPositionX() {
        return positionX;
    }

    public Color getColor() {
        return color;
    }
}
