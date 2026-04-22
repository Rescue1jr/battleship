import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import ships.*;

import java.util.*;

public class GameSetup extends Stage {

    private String username;

    private final int CELL_SIZE = 35;
    private final int GRID_SIZE = 10;

    private boolean placementLocked = false;

    private Pane playerBoardPane = new Pane();
    private Pane opponentBoardPane = new Pane();

    private int[][] opponentGrid = new int[10][10];

    private Rectangle selectedShip = null;

    // NEW: Ship system
    private List<Ship> ships = new ArrayList<>();
    private Map<Rectangle, Ship> shipMap = new HashMap<>();

    public GameSetup() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter your username");
        username = dialog.showAndWait().orElse("Player");

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
        VBox right = new VBox(10, opponentLabel, opponentBoardPane);

        left.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);

        HBox boards = new HBox(60, left, right);
        boards.setAlignment(Pos.CENTER);

        // Create ships
        ships.add(new Carrier());
        ships.add(new Battleship());
        ships.add(new Destroyer());
        ships.add(new Submarine());
        ships.add(new ScoutShip());

        HBox shipsBox = new HBox(15);
        shipsBox.setAlignment(Pos.CENTER);

        for (Ship ship : ships) {
            Rectangle rect = createShip(ship);
            shipMap.put(rect, ship);
            shipsBox.getChildren().add(rect);
        }

        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        startButton.setPrefWidth(200);

        startButton.setOnAction(e -> {
            placementLocked = true;

            Game game = new Game(username, ships, opponentGrid);
            game.show();

            this.close();
        });

        root.getChildren().addAll(boards, shipsBox, startButton);

        Scene scene = new Scene(root, 1000, 750);

        scene.setOnKeyPressed(e -> {
            if (placementLocked) return;

            if (e.getCode() == KeyCode.R && selectedShip != null) {
                Ship ship = shipMap.get(selectedShip);
                ship.rotate();

                double temp = selectedShip.getWidth();
                selectedShip.setWidth(selectedShip.getHeight());
                selectedShip.setHeight(temp);

                snapToGrid(selectedShip);
            }
        });

        setScene(scene);
        setTitle("Battleship Setup");
    }

    private Rectangle createShip(Ship ship) {

        Rectangle rect = new Rectangle(ship.getLength() * CELL_SIZE, CELL_SIZE);
        rect.setFill(Color.DARKGRAY);

        final double[] offset = new double[2];
        final double[] lastValid = new double[2];

        rect.setOnMousePressed(e -> {
            if (placementLocked) return;

            selectedShip = rect;

            offset[0] = e.getSceneX() - rect.getLayoutX();
            offset[1] = e.getSceneY() - rect.getLayoutY();

            lastValid[0] = rect.getLayoutX();
            lastValid[1] = rect.getLayoutY();
        });

        rect.setOnMouseDragged(e -> {
            if (placementLocked) return;

            rect.setLayoutX(e.getSceneX() - offset[0]);
            rect.setLayoutY(e.getSceneY() - offset[1]);
        });

        rect.setOnMouseReleased(e -> {
            if (placementLocked) return;

            if (!snapToGrid(rect)) {
                rect.setLayoutX(lastValid[0]);
                rect.setLayoutY(lastValid[1]);
            }

            selectedShip = null;
        });

        return rect;
    }

    private boolean snapToGrid(Rectangle rect) {

        Ship ship = shipMap.get(rect);

        double x = rect.getLayoutX();
        double y = rect.getLayoutY();

        int col = (int) Math.round((x - playerBoardPane.getLayoutX()) / CELL_SIZE);
        int row = (int) Math.round((y - playerBoardPane.getLayoutY()) / CELL_SIZE);

        int length = ship.getLength();
        boolean horizontal = ship.isHorizontal();

        if (horizontal && col + length - 1 > 10) col = 10 - length + 1;
        if (!horizontal && row + length - 1 > 10) row = 10 - length + 1;

        if (col < 1) col = 1;
        if (row < 1) row = 1;

        rect.setLayoutX(col * CELL_SIZE);
        rect.setLayoutY(row * CELL_SIZE);

        // Save into Ship object
        ship.setPosition(row - 1, col - 1);

        if (!playerBoardPane.getChildren().contains(rect)) {
            playerBoardPane.getChildren().add(rect);
        }

        return true;
    }

    private void drawBoard(Pane pane) {

        pane.setStyle("-fx-background-color: lightblue; -fx-border-color: black;");

        for (int i = 0; i <= GRID_SIZE; i++) {
            for (int j = 0; j <= GRID_SIZE; j++) {

                if (i == 0 && j > 0) {
                    Label label = new Label(String.valueOf(j));
                    label.setPrefSize(CELL_SIZE, CELL_SIZE);
                    label.setAlignment(Pos.CENTER);
                    label.setLayoutX(j * CELL_SIZE);
                    pane.getChildren().add(label);
                }

                if (j == 0 && i > 0) {
                    Label label = new Label(String.valueOf((char) ('A' + i - 1)));
                    label.setPrefSize(CELL_SIZE, CELL_SIZE);
                    label.setAlignment(Pos.CENTER);
                    label.setLayoutY(i * CELL_SIZE);
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

    private void placeOpponentShips() {

        Random rand = new Random();
        int[] sizes = {5,4,3,3,2};

        for (int size : sizes) {

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
                }

                placed = true;
            }
        }
    }

    public String getUsername() {
        return username;
    }
}