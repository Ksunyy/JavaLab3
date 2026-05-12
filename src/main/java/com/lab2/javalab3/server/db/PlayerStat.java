package com.lab2.javalab3.server.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_stats")
public class PlayerStat {
    @Id
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "wins", nullable = false)
    private int wins;

    public PlayerStat() {
    }

    public PlayerStat(String username, int wins) {
        this.username = username;
        this.wins = wins;
    }

    public String getUsername() {
        return username;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }
}
