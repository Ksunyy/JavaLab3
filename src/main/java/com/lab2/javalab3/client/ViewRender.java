package com.lab2.javalab3.client;

import com.lab2.javalab3.common.GameState;
import com.lab2.javalab3.common.model.Arrow;
import com.lab2.javalab3.common.model.Participant;
import com.lab2.javalab3.common.model.Player;
import com.lab2.javalab3.common.model.Target;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

import java.util.Objects;

public final class ViewRender {
    private ViewRender() {
    }

    public static void renderGraphics(GraphicsContext graphicsContext, GameState gameState, String playerName) {
        graphicsContext.clearRect(0, 0, GameState.SCREEN_WIDTH, GameState.SCREEN_HEIGHT);

        graphicsContext.setFill(new LinearGradient(
                0, 0, 0, 1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(233, 244, 255)),
                new Stop(1.0, Color.rgb(210, 227, 245))
        ));
        graphicsContext.fillRect(0, 0, GameState.SCREEN_WIDTH, GameState.SCREEN_HEIGHT);

        graphicsContext.setFill(Color.rgb(181, 208, 232));
        graphicsContext.fillRoundRect(255, 16, 320, GameState.SCREEN_HEIGHT - 32, 24, 24);

        graphicsContext.setStroke(Color.rgb(255, 255, 255, 0.72));
        graphicsContext.setLineWidth(2.0);
        for (int lane = 1; lane <= GameState.MAX_PLAYERS; lane++) {
            double y = GameState.SCREEN_HEIGHT / (double) (GameState.MAX_PLAYERS + 1) * lane;
            graphicsContext.strokeLine(18, y, GameState.SCREEN_WIDTH - 18, y);
        }

        renderTarget(graphicsContext, gameState.getBigTarget());
        renderTarget(graphicsContext, gameState.getSmallTarget());

        for (Arrow arrow : gameState.getArrows()) {
            renderArrow(graphicsContext, arrow);
        }

        for (Participant participant : gameState.getParticipants()) {
            Player player = new Player(participant, Objects.equals(participant.getName(), playerName));
            renderPlayer(graphicsContext, player);
        }

        graphicsContext.setFill(Color.rgb(56, 70, 92));
        graphicsContext.setFont(Font.font("Segoe UI", 12));
        graphicsContext.fillText("Big target = 1 point", 18, GameState.SCREEN_HEIGHT - 18);
        graphicsContext.fillText("Small target = 2 points", 160, GameState.SCREEN_HEIGHT - 18);
    }

    private static void renderTarget(GraphicsContext graphicsContext, Target target) {
        graphicsContext.setFill(toFxColor(target.getColor()));
        graphicsContext.fillOval(target.getPositionX(), target.getPositionY(), target.getMaxWidth(), target.getMaxHeight());
        graphicsContext.setStroke(Color.rgb(42, 45, 52));
        graphicsContext.setLineWidth(2.0);
        graphicsContext.strokeOval(target.getPositionX(), target.getPositionY(), target.getMaxWidth(), target.getMaxHeight());
    }

    private static void renderArrow(GraphicsContext graphicsContext, Arrow arrow) {
        double tailX = arrow.getPositionX() - 38.0;
        double headBaseX = arrow.getPositionX() - 12.0;
        double topY = arrow.getPositionY() - 6.0;
        double bottomY = arrow.getPositionY() + 6.0;

        graphicsContext.setStroke(toFxColor(arrow.getColor()));
        graphicsContext.setFill(toFxColor(arrow.getColor()));
        graphicsContext.setLineWidth(3.0);
        graphicsContext.strokeLine(tailX, arrow.getPositionY(), headBaseX, arrow.getPositionY());
        graphicsContext.fillPolygon(
                new double[]{arrow.getPositionX(), headBaseX, headBaseX},
                new double[]{arrow.getPositionY(), topY, bottomY},
                3
        );
    }

    private static void renderPlayer(GraphicsContext graphicsContext, Player player) {
        double tipX = player.getPositionX() + player.getMaxWidth();
        double backX = player.getPositionX();
        double topY = player.getPositionY() - player.getMaxHeight();
        double centerY = player.getPositionY();
        double bottomY = player.getPositionY() + player.getMaxHeight();

        graphicsContext.setFill(toFxColor(player.getColor()));
        graphicsContext.fillPolygon(
                new double[]{tipX, backX, backX},
                new double[]{centerY, topY, bottomY},
                3
        );

        if (player.isLocalPlayer()) {
            graphicsContext.setStroke(Color.rgb(35, 43, 57));
            graphicsContext.setLineWidth(2.5);
            graphicsContext.strokePolygon(
                    new double[]{tipX, backX, backX},
                    new double[]{centerY, topY, bottomY},
                    3
            );
        }

        graphicsContext.setFill(Color.rgb(20, 24, 28));
        graphicsContext.setFont(Font.font("Segoe UI Semibold", 12));
        graphicsContext.fillText(player.getName(), backX - 2, topY - 8);
    }

    private static Color toFxColor(java.awt.Color color) {
        return Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255.0);
    }
}
