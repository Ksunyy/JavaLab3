package com.lab2.javalab3.common;

import java.io.Serializable;

public class Req implements Serializable {
    private static final long serialVersionUID = 1L;

    private TypeMsg type;
    private String playerName;

    public Req(TypeMsg type) {
        this(type, "");
    }

    public Req(TypeMsg type, String playerName) {
        this.type = type;
        this.playerName = playerName;
    }

    public static Req connect(String playerName) {
        return new Req(TypeMsg.CONNECT, playerName);
    }

    public static Req command(TypeMsg type, String playerName) {
        return new Req(type, playerName);
    }

    public TypeMsg getType() {
        return type;
    }

    public void setType(TypeMsg type) {
        this.type = type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        return "Req{" +
                "type=" + type +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}
