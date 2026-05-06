import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import ships.*;

import java.io.File;
import java.util.*;

public class Game extends Stage {

    private final int SIZE = 10;
    private final int CELL = 35;

    private Rectangle[][] playerCells = new Rectangle[SIZE][SIZE];
    private Rectangle[][] opponentCells = new Rectangle[SIZE][SIZE];

    private Ship[][] playerShipGrid = new Ship[SIZE][SIZE];
    private Ship[][] opponentShipGrid = new Ship[SIZE][SIZE];

    private List<Ship> playerShips;
    private List<Ship> opponentShips = new ArrayList<>();

    private Pane playerShipLayer = new Pane();
    private Pane opponentShipLayer = new Pane();

    private GridPane opponentGrid;

    private boolean playerTurn = true;
    private boolean gameOver = false;

    private int playerShipsRemaining;
    private int opponentShipsRemaining;

    private Label playerLabel;
    private Label opponentLabel;
    private Label timerLabel;
    private Label shotsLabel;
    private Label powerLabel;
    private Label cooldownLabel;

    private int shotsFired = 0;
    private int seconds = 0;

    private Timeline timer;
    private Random rand = new Random();

    private int playerTheme;
    private int opponentTheme;
    
    private Scoreboard scoreboard = new Scoreboard();

    // AI
    private boolean targetMode = false;
    private int lastHitRow = -1;
    private int lastHitCol = -1;
    private int direction = -1;

    // Ability
    private int powerPoints = 0;
    private int sonarCooldown = 0;
    private boolean sonarMode = false;

    public Game(String username, List<Ship> ships, int[][] opponentGridInput, int playerTheme, int opponentTheme) {

        this.playerShips = ships;
        this.playerTheme = playerTheme;
        this.opponentTheme = opponentTheme;

        buildPlayerGridWithShips(ships);
        generateOpponentShips();

        playerShipsRemaining = playerShips.size();
        opponentShipsRemaining = opponentShips.size();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        playerLabel = new Label(username);
        opponentLabel = new Label("Opponent");

        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        opponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        timerLabel = new Label("Time: 0s");
        shotsLabel = new Label("Shots: 0");
        powerLabel = new Label("Power Points: 0");
        cooldownLabel = new Label("Cooldown: 0");

        VBox info = new VBox(10, timerLabel, shotsLabel);
        info.setAlignment(Pos.CENTER);

        GridPane playerGridPane = createBoard(playerCells, true);
        opponentGrid = createBoard(opponentCells, false);

        playerShipLayer.setMouseTransparent(true);
        opponentShipLayer.setMouseTransparent(true);

        StackPane playerBoard = new StackPane(playerGridPane, playerShipLayer);
        StackPane opponentBoard = new StackPane(opponentGrid, opponentShipLayer);

        showPlayerShips();

        Button sonarButton = new Button("Use Sonar (10 PP)");

        sonarButton.setOnAction(e -> {
            if (!playerTurn || gameOver) return;
            if (powerPoints < 10) return;
            if (sonarCooldown > 0) return;

            sonarMode = true;
            sonarButton.setText("Select a tile...");
        });

        VBox left = new VBox(10,
                playerLabel,
                playerBoard,
                powerLabel,
                sonarButton,
                cooldownLabel
        );

        VBox right = new VBox(10, opponentLabel, opponentBoard);

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

    // ================= CORE BOARD =================

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

    // ================= SHIP SETUP =================

    private void buildPlayerGridWithShips(List<Ship> ships) {

        for (Ship ship : ships) {

            int row = ship.getRow();
            int col = ship.getCol();

            for (int i = 0; i < ship.getLength(); i++) {

                int r = row + (ship.isHorizontal() ? 0 : i);
                int c = col + (ship.isHorizontal() ? i : 0);

                playerShipGrid[r][c] = ship;
            }
        }
    }

    private void generateOpponentShips() {

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

    // ================= RENDERING =================

    private Image loadImage(int theme, String name) {
        return new Image(new File("lib/" + theme + "/" + name + ".png").toURI().toString());
    }

    private String getShipName(Ship ship) {
        if (ship instanceof Carrier) return "carrier";
        if (ship instanceof Battleship) return "battleship";
        if (ship instanceof Destroyer) return "crusier";
        if (ship instanceof Submarine) return "submarine";
        return "scout ship";
    }

    private void drawShipImage(Pane layer, Ship ship, int theme) {

        ImageView img = new ImageView(loadImage(theme, getShipName(ship)));

        int row = ship.getRow();
        int col = ship.getCol();
        int length = ship.getLength();

        if (ship.isHorizontal()) {
            img.setFitWidth(length * CELL);
            img.setFitHeight(CELL);
            img.setLayoutX((col + 1) * CELL);
            img.setLayoutY((row + 1) * CELL);
        } else {
            img.setFitWidth(length * CELL);
            img.setFitHeight(CELL);
            img.setRotate(90);
            img.setLayoutX((col + 1) * CELL + (length - 1) * CELL);
            img.setLayoutY((row + 1) * CELL);
        }

        layer.getChildren().add(img);
    }

    private void showPlayerShips() {
        for (Ship ship : playerShips) {
            drawShipImage(playerShipLayer, ship, playerTheme);
        }
    }

    private void revealOpponentShip(Ship ship) {
        drawShipImage(opponentShipLayer, ship, opponentTheme);
    }

    // ================= GAME LOGIC =================

    private void sinkShip(Ship[][] grid, Rectangle[][] cells, Ship ship) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == ship) {
                    cells[r][c].setFill(Color.BLACK);
                }
            }
        }
    }
    
    private void handlePlayerShot(int r, int c) {

        if (!playerTurn || gameOver) return;

        Color current = (Color) opponentCells[r][c].getFill();

        // 🔍 SONAR MODE
        if (sonarMode) {

            // can only use on already shot tiles
            if (current == Color.RED || current == Color.WHITE) {

                powerPoints -= 10;
                powerLabel.setText("Power Points: " + powerPoints);
                sonarCooldown = 3;

                int count = countAdjacentShips(r, c);

                Label label = new Label(String.valueOf(count));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                StackPane stack = new StackPane(opponentCells[r][c], label);

                // ✅ FIXED (no casting issues)
                opponentGrid.add(stack, c + 1, r + 1);

                sonarMode = false;
            }

            return;
        }

        // only allow shooting untouched tiles
        if (current != Color.LIGHTBLUE) return;

        shotsFired++;
        shotsLabel.setText("Shots: " + shotsFired);

        // 🎯 HIT
        if (opponentShipGrid[r][c] != null) {

            Ship ship = opponentShipGrid[r][c];
            ship.hit();

            opponentCells[r][c].setFill(Color.RED);

            if (ship.isSunk()) {

                sinkShip(opponentShipGrid, opponentCells, ship);
                revealOpponentShip(ship);

                opponentShipsRemaining--;

                if (opponentShipsRemaining == 0) {
                    endGame(true);
                }
            }

        } else {
            // ❌ MISS
            opponentCells[r][c].setFill(Color.WHITE);

            playerTurn = false;
            updateTurnUI();
            opponentTurn();
        }
    }

    private int countAdjacentShips(int r, int c) {

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

    // ================= AI =================

    private boolean isValidShot(int r, int c) {
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return false;
        Color color = (Color) playerCells[r][c].getFill();
        return color == Color.LIGHTBLUE || color == Color.DARKGRAY;
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

                powerPoints++;
                powerLabel.setText("Power Points: " + powerPoints);

                if (ship.isSunk()) {
                    sinkShip(playerShipGrid, playerCells, ship);
                    playerShipsRemaining--;

                    if (playerShipsRemaining == 0) {
                        endGame(false);
                        return;
                    }
                }

                opponentTurn();

            } else {

                playerCells[r][c].setFill(Color.WHITE);
                playerTurn = true;
                updateTurnUI();
            }

        }));

        delay.play();
    }

    // ================= UI =================

    private void updateTurnUI() {

        if (playerTurn) {
            playerLabel.setTextFill(Color.YELLOW);
            opponentLabel.setTextFill(Color.BLACK);

            powerPoints += 2;
            powerLabel.setText("Power Points: " + powerPoints);

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

    private void endGame(boolean playerWon) {

        gameOver = true;

        if (timer != null) timer.stop();

        // ✅ SAVE TO DATABASE
        try {
            scoreboard.saveGameResult(
                    playerLabel.getText(), // username
                    seconds,               // time
                    shotsFired,            // shots
                    playerWon              // win/loss
            );
        } catch (Exception e) {
            e.printStackTrace(); // helps debugging
        }

        Label result = new Label(playerWon ? "YOU WIN!" : "YOU LOSE!");
        result.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        result.setTextFill(playerWon ? Color.GREEN : Color.RED);

        ((VBox) getScene().getRoot()).getChildren().add(result);
    }
}