package com.lab2.javalab3.client;

import com.lab2.javalab3.common.GameState;
import com.lab2.javalab3.common.LeaderboardEntry;
import com.lab2.javalab3.common.model.Participant;
import com.lab2.javalab3.server.GameServer;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

public class GameClientController {
    @FXML
    private Canvas mainCanvas;
    @FXML
    private Button readyButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button shootButton;
    @FXML
    private Button leaderboardButton;
    @FXML
    private Label shotsCounter;
    @FXML
    private Label scoreCounter;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea playersArea;

    private final GameClient gameClient = new GameClient(this);
    private GameState gameState = new GameState();
    private GraphicsContext graphicsContext;
    private boolean connectionPromptOpened;

    @FXML
    private void initialize() {
        graphicsContext = mainCanvas.getGraphicsContext2D();
        playersArea.setEditable(false);
        playersArea.setFocusTraversable(false);
        readyButton.setFocusTraversable(false);
        pauseButton.setFocusTraversable(false);
        shootButton.setFocusTraversable(false);
        leaderboardButton.setFocusTraversable(false);
        mainCanvas.setFocusTraversable(true);
        drawState();
        updateCounters(null);
        updatePlayersTable();
        updateControls();

        mainCanvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                configureScene(newScene);
            }
        });
    }

    @FXML
    private void handleReady() {
        gameClient.sendReady();
    }

    @FXML
    private void handlePause() {
        gameClient.sendPause();
    }

    @FXML
    private void handleShoot() {
        gameClient.sendShoot();
    }

    @FXML
    private void handleLeaderboard() {
        gameClient.requestLeaderboard();
    }

    public void updateGameState(GameState gameState, String playerName) {
        this.gameState = gameState;
        updateCounters(playerName);
        updatePlayersTable();
        updateControls();
        drawState();
    }

    public void setStatusText(String text) {
        if (text != null && !text.isBlank()) {
            statusLabel.setText(text);
        }
    }

    public void showGameOver(String text) {
        setStatusText(text);
        showAlert(Alert.AlertType.INFORMATION, "Round Finished", text);
    }

    public void showLeaderboard(List<LeaderboardEntry> leaderboard) {
        setStatusText("Leaderboard loaded. The game is paused until players are ready again.");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Leaderboard");
        dialog.setHeaderText(null);
        dialog.initOwner(getWindow());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        TableView<LeaderboardEntry> table = new TableView<>();
        table.setPrefSize(360, 280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LeaderboardEntry, String> nameColumn = new TableColumn<>("User name");
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));

        TableColumn<LeaderboardEntry, Number> winsColumn = new TableColumn<>("Wins");
        winsColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getWins()));

        table.getColumns().add(nameColumn);
        table.getColumns().add(winsColumn);
        table.getItems().setAll(leaderboard);

        dialog.getDialogPane().setContent(table);
        dialog.showAndWait();
    }

    public void showError(String text) {
        showAlert(Alert.AlertType.ERROR, "Server Error", text);
    }

    public void handleConnectionLost(String text) {
        statusLabel.setText(text);
        updateControls();
        showAlert(Alert.AlertType.WARNING, "Disconnected", text);
    }

    private void configureScene(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (!shootButton.isDisable()) {
                    gameClient.sendShoot();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER && !readyButton.isDisable()) {
                gameClient.sendReady();
                event.consume();
            }
        });

        scene.windowProperty().addListener((observable, oldWindow, newWindow) -> {
            if (newWindow != null) {
                newWindow.setOnHidden(event -> gameClient.disconnect());
                if (!connectionPromptOpened) {
                    connectionPromptOpened = true;
                    Platform.runLater(this::showConnectionDialog);
                }
            }
        });
    }

    private void showConnectionDialog() {
        String hostValue = "127.0.0.1";
        String portValue = Integer.toString(GameServer.DEFAULT_PORT);
        String userNameValue = "";

        while (true) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Connect To Server");
            dialog.setHeaderText("Enter the server settings and a unique user name.");
            dialog.initOwner(getWindow());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            TextField hostField = new TextField(hostValue);
            TextField portField = new TextField(portValue);
            TextField userNameField = new TextField(userNameValue);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Host:"), 0, 0);
            grid.add(hostField, 1, 0);
            grid.add(new Label("Port:"), 0, 1);
            grid.add(portField, 1, 1);
            grid.add(new Label("User name:"), 0, 2);
            grid.add(userNameField, 1, 2);
            dialog.getDialogPane().setContent(grid);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                closeWindow();
                return;
            }

            hostValue = hostField.getText().trim();
            portValue = portField.getText().trim();
            userNameValue = userNameField.getText().trim();

            try {
                int parsedPort = Integer.parseInt(portValue);
                gameClient.connect(hostValue, parsedPort, userNameValue);
                Stage stage = (Stage) getWindow();
                stage.setTitle("Network Archery Client - " + gameClient.getPlayerName());
                return;
            } catch (NumberFormatException exception) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Port must be a number.");
            } catch (Exception exception) {
                showAlert(Alert.AlertType.ERROR, "Connection Error", exception.getMessage());
            }
        }
    }

    private void drawState() {
        ViewRender.renderGraphics(graphicsContext, gameState, gameClient.getPlayerName());
    }

    private void updateCounters(String playerName) {
        Participant currentPlayer = gameState.findParticipant(playerName);
        if (currentPlayer == null) {
            shotsCounter.setText("Shots: 0");
            scoreCounter.setText("Score: 0");
            return;
        }

        shotsCounter.setText("Shots: " + currentPlayer.getCountShots());
        scoreCounter.setText("Score: " + currentPlayer.getScore() + " | Ready: " + (currentPlayer.isReady() ? "yes" : "no"));
    }

    private void updatePlayersTable() {
        StringBuilder builder = new StringBuilder();
        for (Participant participant : gameState.getParticipants()) {
            builder.append(participant.getName())
                    .append(" | score=")
                    .append(participant.getScore())
                    .append(" | shots=")
                    .append(participant.getCountShots())
                    .append(" | wins=")
                    .append(participant.getWins())
                    .append(" | ready=")
                    .append(participant.isReady() ? "yes" : "no")
                    .append(System.lineSeparator());
        }
        playersArea.setText(builder.toString());
    }

    private void updateControls() {
        boolean connected = gameClient.isConnected();
        Participant currentPlayer = gameState.findParticipant(gameClient.getPlayerName());
        boolean currentPlayerIsReady = currentPlayer != null && currentPlayer.isReady();

        readyButton.setText(currentPlayerIsReady && !gameState.isRunning() ? "Ready sent" : "Ready");
        readyButton.setDisable(!connected || gameState.isRunning() || currentPlayerIsReady);
        pauseButton.setDisable(!gameState.isRunning());
        shootButton.setDisable(!gameState.isRunning());
        leaderboardButton.setDisable(!connected);
    }

    private void showAlert(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        if (getWindow() != null) {
            alert.initOwner(getWindow());
        }
        alert.showAndWait();
    }

    private Window getWindow() {
        return mainCanvas.getScene() == null ? null : mainCanvas.getScene().getWindow();
    }

    private void closeWindow() {
        Window window = getWindow();
        if (window instanceof Stage stage) {
            stage.close();
        }
    }
}
