package com.lab2.javalab3.common.model;

import java.awt.Color;

public class Player extends GameObject {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Color color;
    private final boolean localPlayer;

    public Player(Participant participant, boolean localPlayer) {
        super(24.0, 42.0, participant.getPositionX(), participant.getPositionY(), 0.0);
        this.name = participant.getName();
        this.color = participant.getColor();
        this.localPlayer = localPlayer;
    }

    @Override
    public void moveObject(double diff) {
        // The player stays on a fixed lane.
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public boolean isLocalPlayer() {
        return localPlayer;
    }
}
