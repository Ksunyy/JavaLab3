package com.lab2.javalab3.common.model;

import java.io.Serializable;

public abstract class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private double maxHeight;
    private double maxWidth;
    private double positionX;
    private double positionY;
    private double speed;

    protected GameObject() {
        this(0.0, 0.0, 0.0, 0.0, 0.0);
    }

    protected GameObject(double height, double width, double x, double y, double speed) {
        this.maxHeight = height;
        this.maxWidth = width;
        this.positionX = x;
        this.positionY = y;
        this.speed = speed;
    }

    public abstract void moveObject(double diff);

    public double getMaxHeight() {
        return maxHeight;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
