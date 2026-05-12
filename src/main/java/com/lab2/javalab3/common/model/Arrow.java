package com.lab2.javalab3.common.model;

import java.awt.Color;

public class Arrow extends GameObject {
    private static final long serialVersionUID = 1L;

    private final String ownerName;
    private final Color color;

    public Arrow(Participant participant) {
        super(6.0, 10.0, participant.getPositionX() + 48.0, participant.getPositionY(), 12.0);
        this.ownerName = participant.getName();
        this.color = participant.getColor();
    }

    public Arrow(Arrow other) {
        super(other.getMaxHeight(), other.getMaxWidth(), other.getPositionX(), other.getPositionY(), other.getSpeed());
        this.ownerName = other.ownerName;
        this.color = new Color(other.color.getRGB(), true);
    }

    @Override
    public void moveObject(double diff) {
        setPositionX(getPositionX() + diff * getSpeed());
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Color getColor() {
        return color;
    }
}
