import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Random;

public class GameSetup extends Stage {

    private String username;
    private final int CELL_SIZE = 35;
    private final int GRID_SIZE = 10;

    private boolean placementLocked = false;

    private Pane playerBoardPane = new Pane();
    private Pane opponentBoardPane = new Pane();

    private int[][] opponentGrid = new int[10][10];
    private int[][] playerGrid = new int[10][10];

    private Rectangle selectedShip = null;

    public GameSetup() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter your username");
        Optional<String> result = dialog.showAndWait();
        username = result.orElse("Player");

        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);

        Label playerLabel = new Label(username);
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label opponentLabel = new Label("Opponent");
        opponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        playerBoardPane.setPrefSize(11 * CELL_SIZE, 11 * CELL_SIZE);
        opponentBoardPane.setPrefSize(11 * CELL_SIZE, 11 * CELL_SIZE);

        drawBoard(playerBoardPane);
        drawBoard(opponentBoardPane);

        placeOpponentShips();

        VBox left = new VBox(10, playerLabel, playerBoardPane);
        left.setAlignment(Pos.CENTER);

        VBox right = new VBox(10, opponentLabel, opponentBoardPane);
        right.setAlignment(Pos.CENTER);

        HBox boards = new HBox(60, left, right);
        boards.setAlignment(Pos.CENTER);

        HBox ships = new HBox(15);
        ships.setAlignment(Pos.CENTER);

        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int size : shipSizes) {
            Rectangle ship = createShip(size);
            ships.getChildren().add(ship);
        }

        Button startGameButton = new Button("Start Game");
        startGameButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        startGameButton.setPrefWidth(200);

        startGameButton.setOnAction(e -> {
            placementLocked = true;

            Game game = new Game(username, playerGrid, opponentGrid);
            game.show();

            this.close();
        });

        root.getChildren().addAll(boards, ships, startGameButton);

        Scene scene = new Scene(root, 1000, 750);

        // 🔑 KEY HANDLER FOR ROTATION
        scene.setOnKeyPressed(e -> {
            if (placementLocked) return;

            if (e.getCode() == KeyCode.R && selectedShip != null) {
                rotateShip(selectedShip);
            }
        });

        setScene(scene);
        setTitle("Battleship Setup");
    }

    private void rotateShip(Rectangle ship) {

        double temp = ship.getWidth();
        ship.setWidth(ship.getHeight());
        ship.setHeight(temp);

        snapToGrid(ship);
    }

    private void drawBoard(Pane pane) {

        pane.setStyle("-fx-background-color: lightblue; -fx-border-color: black;");

        for (int i = 0; i <= GRID_SIZE; i++) {
            for (int j = 0; j <= GRID_SIZE; j++) {

                if (i == 0 && j > 0) {
                    Label label = new Label(String.valueOf(j));
                    label.setLayoutX(j * CELL_SIZE);
                    label.setLayoutY(0);
                    label.setPrefSize(CELL_SIZE, CELL_SIZE);
                    label.setAlignment(Pos.CENTER);
                    pane.getChildren().add(label);
                }

                if (j == 0 && i > 0) {
                    Label label = new Label(String.valueOf((char) ('A' + i - 1)));
                    label.setLayoutX(0);
                    label.setLayoutY(i * CELL_SIZE);
                    label.setPrefSize(CELL_SIZE, CELL_SIZE);
                    label.setAlignment(Pos.CENTER);
                    pane.getChildren().add(label);
                }

                if (i > 0 && j > 0) {
                    Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                    cell.setStroke(Color.BLACK);
                    cell.setFill(Color.TRANSPARENT);
                    cell.setLayoutX(j * CELL_SIZE);
                    cell.setLayoutY(i * CELL_SIZE);
                    pane.getChildren().add(cell);
                }
            }
        }
    }

    private Rectangle createShip(int length) {

        Rectangle ship = new Rectangle(length * CELL_SIZE, CELL_SIZE);
        ship.setFill(Color.DARKGRAY);

        final double[] offset = new double[2];
        final double[] lastValidPos = new double[2];

        ship.setOnMousePressed(e -> {
            if (placementLocked) return;

            selectedShip = ship;

            offset[0] = e.getSceneX() - ship.getLayoutX();
            offset[1] = e.getSceneY() - ship.getLayoutY();

            lastValidPos[0] = ship.getLayoutX();
            lastValidPos[1] = ship.getLayoutY();
        });

        ship.setOnMouseDragged(e -> {
            if (placementLocked) return;

            ship.setLayoutX(e.getSceneX() - offset[0]);
            ship.setLayoutY(e.getSceneY() - offset[1]);
        });

        ship.setOnMouseReleased(e -> {
            if (placementLocked) return;

            if (!snapToGrid(ship)) {
                ship.setLayoutX(lastValidPos[0]);
                ship.setLayoutY(lastValidPos[1]);
            }

            selectedShip = null;
        });

        return ship;
    }

    private boolean snapToGrid(Rectangle ship) {

        double x = ship.getLayoutX();
        double y = ship.getLayoutY();

        int col = (int) Math.round((x - playerBoardPane.getLayoutX()) / CELL_SIZE);
        int row = (int) Math.round((y - playerBoardPane.getLayoutY()) / CELL_SIZE);

        int length = (ship.getWidth() > ship.getHeight())
                ? (int)(ship.getWidth() / CELL_SIZE)
                : (int)(ship.getHeight() / CELL_SIZE);

        boolean horizontal = ship.getWidth() > ship.getHeight();

        if (horizontal && col + length - 1 > 10) col = 10 - length + 1;
        if (!horizontal && row + length - 1 > 10) row = 10 - length + 1;

        if (col < 1) col = 1;
        if (row < 1) row = 1;

        for (int i = 0; i < length; i++) {
            int r = row - 1 + (horizontal ? 0 : i);
            int c = col - 1 + (horizontal ? i : 0);

            if (playerGrid[r][c] == 1) return false;
        }

        clearGrid();

        for (int i = 0; i < length; i++) {
            int r = row - 1 + (horizontal ? 0 : i);
            int c = col - 1 + (horizontal ? i : 0);
            playerGrid[r][c] = 1;
        }

        ship.setLayoutX(col * CELL_SIZE);
        ship.setLayoutY(row * CELL_SIZE);

        if (!playerBoardPane.getChildren().contains(ship)) {
            playerBoardPane.getChildren().add(ship);
        }

        return true;
    }

    private void clearGrid() {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                playerGrid[r][c] = 0;
            }
        }
    }

    private void placeOpponentShips() {

        Random rand = new Random();
        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int size : shipSizes) {

            boolean placed = false;

            while (!placed) {

                boolean horizontal = rand.nextBoolean();
                int row = rand.nextInt(10);
                int col = rand.nextInt(10);

                if (horizontal) {

                    if (col + size > 10) continue;

                    boolean valid = true;
                    for (int i = 0; i < size; i++) {
                        if (opponentGrid[row][col + i] == 1) {
                            valid = false;
                            break;
                        }
                    }

                    if (!valid) continue;

                    for (int i = 0; i < size; i++) {
                        opponentGrid[row][col + i] = 1;
                    }

                    placed = true;

                } else {

                    if (row + size > 10) continue;

                    boolean valid = true;
                    for (int i = 0; i < size; i++) {
                        if (opponentGrid[row + i][col] == 1) {
                            valid = false;
                            break;
                        }
                    }

                    if (!valid) continue;

                    for (int i = 0; i < size; i++) {
                        opponentGrid[row + i][col] = 1;
                    }

                    placed = true;
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }
}