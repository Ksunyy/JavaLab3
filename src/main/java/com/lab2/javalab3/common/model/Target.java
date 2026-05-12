package com.lab2.javalab3.common.model;

import java.awt.Color;

public class Target extends GameObject {
    private static final long serialVersionUID = 1L;

    private final int scoreValue;
    private final Color color;

    public Target(double diameter, double centerX, double centerY, double speed, int scoreValue, Color color) {
        super(diameter, diameter, centerX - diameter / 2.0, centerY - diameter / 2.0, speed);
        this.scoreValue = scoreValue;
        this.color = color;
    }

    public Target(Target other) {
        this(other.getMaxWidth(), other.getCenterX(), other.getCenterY(), other.getSpeed(),
                other.scoreValue, new Color(other.color.getRGB(), true));
    }

    public static Target createBigTarget() {
        return new Target(76.0, 385.0, 170.0, 2.0, 1, new Color(245, 162, 51));
    }

    public static Target createSmallTarget() {
        return new Target(34.0, 520.0, 70.0, 3.6, 2, new Color(217, 63, 63));
    }

    @Override
    public void moveObject(double diff) {
        setPositionY(getPositionY() + diff * getSpeed());
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public Color getColor() {
        return color;
    }

    public double getCenterX() {
        return getPositionX() + getMaxWidth() / 2.0;
    }

    public double getCenterY() {
        return getPositionY() + getMaxHeight() / 2.0;
    }
}
