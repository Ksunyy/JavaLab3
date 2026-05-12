package com.lab2.javalab3.common;

import java.io.Serializable;

public enum TypeMsg implements Serializable {
    CONNECT,
    CONNECTED,
    READY,
    PAUSE,
    SHOOT,
    LEADERBOARD,
    STATE,
    INFO,
    GAME_OVER,
    ERROR,
    DISCONNECT
}
