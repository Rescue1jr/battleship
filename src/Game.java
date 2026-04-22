import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import ships.*;

import java.util.*;

public class Game extends Stage {
	
	private GridPane opponentGridPane;
	
    private final int SIZE = 10;
    private final int CELL = 35;

    private int[][] playerBoard;
    private int[][] opponentBoard;

    private Rectangle[][] playerCells = new Rectangle[SIZE][SIZE];
    private Rectangle[][] opponentCells = new Rectangle[SIZE][SIZE];

    private Ship[][] playerShipGrid = new Ship[SIZE][SIZE];
    private Ship[][] opponentShipGrid = new Ship[SIZE][SIZE];

    private List<Ship> playerShips;
    private List<Ship> opponentShips = new ArrayList<>();

    private boolean playerTurn = true;
    private int shotsFired = 0;

    private Label playerLabel;
    private Label opponentLabel;
    private Label timerLabel;
    private Label shotsLabel;

    private int seconds = 0;
    private Timeline timer;

    private Random rand = new Random();

    // 🤖 AI
    private boolean targetMode = false;
    private int lastHitRow = -1;
    private int lastHitCol = -1;
    private int direction = -1;
    private List<int[]> hitStack = new ArrayList<>();

    // 🏁 GAME STATE
    private boolean gameOver = false;
    private int playerShipsRemaining;
    private int opponentShipsRemaining;

    // ⚡ ABILITY
    private Ability ability;
    private int sonarCooldown = 0;
    private boolean sonarMode = false;
    private Label cooldownLabel;

    public Game(String username, List<Ship> ships, int[][] opponentGrid) {

        this.playerShips = ships;

        buildPlayerGridWithShips(ships);
        generateOpponentShips();

        playerShipsRemaining = playerShips.size();
        opponentShipsRemaining = opponentShips.size();

        ability = new Ability();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        playerLabel = new Label(username);
        opponentLabel = new Label("Opponent");

        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        opponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        timerLabel = new Label("Time: 0s");
        shotsLabel = new Label("Shots: 0");

        VBox info = new VBox(10, timerLabel, shotsLabel);
        info.setAlignment(Pos.CENTER);

        GridPane playerGridPane = createBoard(playerCells, true);
        opponentGridPane = createBoard(opponentCells, false);

        showPlayerShips();

        // 🔘 Sonar Button
        Button sonarButton = new Button("Use Sonar (10 PP)");
        cooldownLabel = new Label("Cooldown: 0");

        sonarButton.setOnAction(e -> {

            if (!playerTurn || gameOver) return;
            if (!ability.canUseSonar()) return;
            if (sonarCooldown > 0) return;

            sonarMode = true;
            sonarButton.setText("Select a tile...");
        });

        VBox left = new VBox(10,
                playerLabel,
                playerGridPane,
                ability.getDisplay(),
                sonarButton,
                cooldownLabel
        );

        VBox right = new VBox(10, opponentLabel, opponentGridPane);

        left.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);

        HBox boards = new HBox(50, info, left, right);
        boards.setAlignment(Pos.CENTER);

        root.getChildren().add(boards);

        Scene scene = new Scene(root, 1100, 650);
        setScene(scene);
        setTitle("Battleship");

        startTimer();
        updateTurnUI();
    }

    private void buildPlayerGridWithShips(List<Ship> ships) {

        playerBoard = new int[SIZE][SIZE];

        for (Ship ship : ships) {

            int row = ship.getRow();
            int col = ship.getCol();

            for (int i = 0; i < ship.getLength(); i++) {

                int r = row + (ship.isHorizontal() ? 0 : i);
                int c = col + (ship.isHorizontal() ? i : 0);

                playerBoard[r][c] = 1;
                playerShipGrid[r][c] = ship;
            }
        }
    }

    private void generateOpponentShips() {

        opponentBoard = new int[SIZE][SIZE];

        opponentShips.add(new Carrier());
        opponentShips.add(new Battleship());
        opponentShips.add(new Destroyer());
        opponentShips.add(new Submarine());
        opponentShips.add(new ScoutShip());

        for (Ship ship : opponentShips) {

            boolean placed = false;

            while (!placed) {

                int row = rand.nextInt(SIZE);
                int col = rand.nextInt(SIZE);
                boolean horizontal = rand.nextBoolean();

                ship.setPosition(row, col);
                if (!horizontal) ship.rotate();

                if (canPlaceShip(opponentShipGrid, ship)) {

                    for (int i = 0; i < ship.getLength(); i++) {

                        int r = row + (ship.isHorizontal() ? 0 : i);
                        int c = col + (ship.isHorizontal() ? i : 0);

                        opponentBoard[r][c] = 1;
                        opponentShipGrid[r][c] = ship;
                    }

                    placed = true;
                }
            }
        }
    }

    private boolean canPlaceShip(Ship[][] grid, Ship ship) {

        int row = ship.getRow();
        int col = ship.getCol();

        for (int i = 0; i < ship.getLength(); i++) {

            int r = row + (ship.isHorizontal() ? 0 : i);
            int c = col + (ship.isHorizontal() ? i : 0);

            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return false;
            if (grid[r][c] != null) return false;
        }

        return true;
    }

    private GridPane createBoard(Rectangle[][] cells, boolean isPlayer) {

        GridPane grid = new GridPane();

        for (int c = 0; c < SIZE; c++) {
            Label label = new Label(String.valueOf(c + 1));
            label.setMinSize(CELL, CELL);
            label.setAlignment(Pos.CENTER);
            grid.add(label, c + 1, 0);
        }

        for (int r = 0; r < SIZE; r++) {

            Label rowLabel = new Label(String.valueOf((char) ('A' + r)));
            rowLabel.setMinSize(CELL, CELL);
            rowLabel.setAlignment(Pos.CENTER);
            grid.add(rowLabel, 0, r + 1);

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
                grid.add(rect, c + 1, r + 1);
            }
        }

        return grid;
    }

    private void showPlayerShips() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (playerShipGrid[r][c] != null) {
                    playerCells[r][c].setFill(Color.DARKGRAY);
                }
            }
        }
    }

    private void handlePlayerShot(int r, int c) {

        if (!playerTurn || gameOver) return;

        Color current = (Color) opponentCells[r][c].getFill();

        // 🔍 SONAR MODE
        if (sonarMode) {

            if (current == Color.RED || current == Color.WHITE) {

                ability.useSonar();
                sonarCooldown = 3;

                int count = countAdjacentShipsWithDiagonals(r, c);

                Label sonarLabel = new Label(String.valueOf(count));
                sonarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

             // Remove old rectangle from grid
                opponentGridPane.getChildren().remove(opponentCells[r][c]);

                // Create stack with label
                StackPane stack = new StackPane(opponentCells[r][c], sonarLabel);

                // Add back into correct position
                opponentGridPane.add(stack, c + 1, r + 1);
                sonarMode = false;
            }

            return;
        }

        if (current != Color.LIGHTBLUE) return;

        shotsFired++;
        shotsLabel.setText("Shots: " + shotsFired);

        if (opponentShipGrid[r][c] != null) {

            Ship ship = opponentShipGrid[r][c];
            ship.hit();

            opponentCells[r][c].setFill(Color.RED);

            if (ship.isSunk()) {
                sinkShip(opponentShipGrid, opponentCells, ship);
                opponentShipsRemaining--;

                if (opponentShipsRemaining == 0) {
                    endGame(true);
                }
            }

        } else {

            opponentCells[r][c].setFill(Color.WHITE);
            playerTurn = false;
            updateTurnUI();
            opponentTurn();
        }
    }

    private int countAdjacentShipsWithDiagonals(int r, int c) {

        int count = 0;

        int[][] dirs = {
                {-1,0},{1,0},{0,-1},{0,1},
                {-1,-1},{-1,1},{1,-1},{1,1}
        };

        for (int[] d : dirs) {

            int nr = r + d[0];
            int nc = c + d[1];

            if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {

                if (opponentShipGrid[nr][nc] != null) {

                    Color tileColor = (Color) opponentCells[nr][nc].getFill();

                    if (tileColor != Color.RED && tileColor != Color.BLACK) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
    private void opponentTurn() {

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            if (gameOver) return;

            int r, c;

            do {
                r = rand.nextInt(SIZE);
                c = rand.nextInt(SIZE);
            } while (!isValidShot(r, c));

            if (playerShipGrid[r][c] != null) {

                Ship ship = playerShipGrid[r][c];
                ship.hit();

                playerCells[r][c].setFill(Color.RED);

                ability.addPoints(1);

                if (ship.isSunk()) {
                    sinkShip(playerShipGrid, playerCells, ship);
                    playerShipsRemaining--;

                    if (playerShipsRemaining == 0) {
                        endGame(false);
                        return;
                    }
                }

                // 🔁 opponent shoots again after hit
                opponentTurn();

            } else {

                playerCells[r][c].setFill(Color.WHITE);
                playerTurn = true;
                updateTurnUI();
            }

        }));

        delay.play();
    }

    private void endGame(boolean playerWon) {

        gameOver = true;

        if (timer != null) timer.stop();

        Label result = new Label(playerWon ? "YOU WIN!" : "YOU LOSE!");
        result.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        result.setTextFill(playerWon ? Color.GREEN : Color.RED);

        ((VBox) getScene().getRoot()).getChildren().add(result);
    }

    private void updateTurnUI() {

        if (playerTurn) {
            playerLabel.setTextFill(Color.YELLOW);
            opponentLabel.setTextFill(Color.BLACK);

            ability.addPoints(2);

            if (sonarCooldown > 0) sonarCooldown--;
            cooldownLabel.setText("Cooldown: " + sonarCooldown);

        } else {
            opponentLabel.setTextFill(Color.YELLOW);
            playerLabel.setTextFill(Color.BLACK);
        }
    }

    private void startTimer() {

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!gameOver) {
                seconds++;
                timerLabel.setText("Time: " + seconds + "s");
            }
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    
    private void sinkShip(Ship[][] grid, Rectangle[][] cells, Ship ship) {

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {

                if (grid[r][c] == ship) {
                    cells[r][c].setFill(Color.BLACK);
                }
            }
        }
    }
    
    private boolean isValidShot(int r, int c) {
        return playerCells[r][c].getFill() == Color.LIGHTBLUE
                || playerCells[r][c].getFill() == Color.DARKGRAY
                || playerCells[r][c].getFill() == Color.BLACK;
    }
    
}