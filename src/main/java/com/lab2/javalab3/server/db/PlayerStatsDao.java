package com.lab2.javalab3.server.db;

import com.lab2.javalab3.common.LeaderboardEntry;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsDao {
    public PlayerStat findOrCreate(String username) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PlayerStat playerStat = session.find(PlayerStat.class, username);
            if (playerStat == null) {
                playerStat = new PlayerStat(username, 0);
                session.persist(playerStat);
            }
            transaction.commit();
            return new PlayerStat(playerStat.getUsername(), playerStat.getWins());
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public int incrementWins(String username) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            PlayerStat playerStat = session.find(PlayerStat.class, username);
            if (playerStat == null) {
                playerStat = new PlayerStat(username, 0);
                session.persist(playerStat);
            }
            playerStat.setWins(playerStat.getWins() + 1);
            transaction.commit();
            return playerStat.getWins();
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    public List<LeaderboardEntry> loadLeaderboard() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<PlayerStat> stats = session
                    .createQuery("FROM PlayerStat ORDER BY wins DESC, username ASC", PlayerStat.class)
                    .getResultList();
            transaction.commit();

            List<LeaderboardEntry> leaderboard = new ArrayList<>();
            for (PlayerStat stat : stats) {
                leaderboard.add(new LeaderboardEntry(stat.getUsername(), stat.getWins()));
            }
            return leaderboard;
        } catch (RuntimeException exception) {
            rollback(transaction);
            throw exception;
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
