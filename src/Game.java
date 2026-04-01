import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Game extends Stage {

    private final int SIZE = 10;
    private final int CELL = 35;

    private int[][] playerBoard;
    private int[][] opponentBoard;

    private Rectangle[][] playerCells = new Rectangle[SIZE][SIZE];
    private Rectangle[][] opponentCells = new Rectangle[SIZE][SIZE];

    private boolean playerTurn = true;
    private int shotsFired = 0;

    private Label playerLabel;
    private Label opponentLabel;
    private Label timerLabel;
    private Label shotsLabel;

    private int seconds = 0;
    private Random rand = new Random();

    // Ship tracking
    private Map<Integer, List<int[]>> opponentShips = new HashMap<>();
    private Map<Integer, Integer> opponentHealth = new HashMap<>();
    private int shipIdCounter = 2;

    public Game(String username, int[][] playerGrid, int[][] opponentGrid) {

        this.playerBoard = playerGrid;
        this.opponentBoard = opponentGrid;

        identifyShips();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        playerLabel = new Label(username);
        opponentLabel = new Label("Opponent");

        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        opponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        timerLabel = new Label("Time: 0s");
        shotsLabel = new Label("Shots: 0");

        VBox leftInfo = new VBox(10, timerLabel, shotsLabel);
        leftInfo.setAlignment(Pos.CENTER);

        GridPane playerGridPane = createBoard(playerCells, true);
        GridPane opponentGridPane = createBoard(opponentCells, false);

        VBox leftBoard = new VBox(10, playerLabel, playerGridPane);
        VBox rightBoard = new VBox(10, opponentLabel, opponentGridPane);

        leftBoard.setAlignment(Pos.CENTER);
        rightBoard.setAlignment(Pos.CENTER);

        HBox boards = new HBox(50, leftInfo, leftBoard, rightBoard);
        boards.setAlignment(Pos.CENTER);

        root.getChildren().add(boards);

        Scene scene = new Scene(root, 1000, 600);
        setScene(scene);
        setTitle("Battleship Game");

        startTimer();
        updateTurnUI();
    }

    private void identifyShips() {

        boolean[][] visited = new boolean[SIZE][SIZE];

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {

                if (opponentBoard[r][c] == 1 && !visited[r][c]) {

                    List<int[]> shipCells = new ArrayList<>();
                    dfs(r, c, visited, shipCells);

                    opponentShips.put(shipIdCounter, shipCells);
                    opponentHealth.put(shipIdCounter, shipCells.size());

                    for (int[] pos : shipCells) {
                        opponentBoard[pos[0]][pos[1]] = shipIdCounter;
                    }

                    shipIdCounter++;
                }
            }
        }
    }

    private void dfs(int r, int c, boolean[][] visited, List<int[]> shipCells) {

        if (r < 0 || c < 0 || r >= SIZE || c >= SIZE) return;
        if (visited[r][c] || opponentBoard[r][c] != 1) return;

        visited[r][c] = true;
        shipCells.add(new int[]{r, c});

        dfs(r + 1, c, visited, shipCells);
        dfs(r - 1, c, visited, shipCells);
        dfs(r, c + 1, visited, shipCells);
        dfs(r, c - 1, visited, shipCells);
    }

    private GridPane createBoard(Rectangle[][] cells, boolean isPlayer) {

        GridPane grid = new GridPane();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {

                Rectangle rect = new Rectangle(CELL, CELL);
                rect.setFill(Color.LIGHTBLUE);
                rect.setStroke(Color.BLACK);

                int row = r;
                int col = c;

                if (!isPlayer) {
                    rect.setOnMouseClicked(e -> handlePlayerShot(row, col));
                }

                cells[r][c] = rect;
                grid.add(rect, c, r);
            }
        }

        return grid;
    }

    private void handlePlayerShot(int r, int c) {

        if (!playerTurn) return;
        if (opponentCells[r][c].getFill() != Color.LIGHTBLUE) return;

        shotsFired++;
        shotsLabel.setText("Shots: " + shotsFired);

        int cell = opponentBoard[r][c];

        if (cell >= 2) {

            opponentCells[r][c].setFill(Color.RED);

            opponentHealth.put(cell, opponentHealth.get(cell) - 1);

            if (opponentHealth.get(cell) == 0) {
                sinkShip(cell);
            }

        } else {
            opponentCells[r][c].setFill(Color.WHITE);
            playerTurn = false;
            updateTurnUI();
            opponentTurn();
        }
    }

    private void sinkShip(int shipId) {

        for (int[] pos : opponentShips.get(shipId)) {
            opponentCells[pos[0]][pos[1]].setFill(Color.BLACK);
        }
    }

    private void opponentTurn() {

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            int r = rand.nextInt(SIZE);
            int c = rand.nextInt(SIZE);

            if (playerCells[r][c].getFill() != Color.LIGHTBLUE) {
                opponentTurn();
                return;
            }

            if (playerBoard[r][c] == 1) {
                playerCells[r][c].setFill(Color.RED);
            } else {
                playerCells[r][c].setFill(Color.WHITE);
                playerTurn = true;
                updateTurnUI();
            }

        }));

        delay.setCycleCount(1);
        delay.play();
    }

    private void updateTurnUI() {

        if (playerTurn) {
            playerLabel.setTextFill(Color.YELLOW);
            opponentLabel.setTextFill(Color.BLACK);
        } else {
            opponentLabel.setTextFill(Color.YELLOW);
            playerLabel.setTextFill(Color.BLACK);
        }
    }

    private void startTimer() {

        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds++;
            timerLabel.setText("Time: " + seconds + "s");
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
}