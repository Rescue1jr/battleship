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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import ships.*;

import java.io.File;
import java.util.*;

public class GameSetup extends Stage {

    private String username;

    private final int CELL = 35;
    private final int SIZE = 10;

    private Pane playerBoard = new Pane();
    private Pane opponentBoard = new Pane();

    private Ship[][] playerGrid = new Ship[SIZE][SIZE];
    private int[][] opponentGrid = new int[SIZE][SIZE];

    private List<Ship> playerShips = new ArrayList<>();
    private Map<ImageView, Ship> shipMap = new HashMap<>();

    private int playerTheme;
    private int opponentTheme;

    private ImageView selectedShip;

    public GameSetup() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter username");
        username = dialog.showAndWait().orElse("Player");

        Random rand = new Random();
        playerTheme = rand.nextInt(3) + 1;
        do {
            opponentTheme = rand.nextInt(3) + 1;
        } while (opponentTheme == playerTheme);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Label playerLabel = new Label(username);
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Label opponentLabel = new Label("Opponent");
        opponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        playerBoard.setPrefSize(11 * CELL, 11 * CELL);
        opponentBoard.setPrefSize(11 * CELL, 11 * CELL);

        drawBoard(playerBoard);
        drawBoard(opponentBoard);

        placeOpponentShips();

        createShip("carrier", new Carrier(), 0);
        createShip("battleship", new Battleship(), 1);
        createShip("crusier", new Destroyer(), 2);
        createShip("submarine", new Submarine(), 3);
        createShip("scout ship", new ScoutShip(), 4);

        VBox left = new VBox(10, playerLabel, playerBoard);
        VBox right = new VBox(10, opponentLabel, opponentBoard);

        left.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);

        HBox boards = new HBox(60, left, right);
        boards.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Start Game");
        startBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        startBtn.setOnAction(e -> {
            Game game = new Game(username, playerShips, opponentGrid, playerTheme, opponentTheme);
            game.show();
            this.close();
        });

        root.getChildren().addAll(boards, startBtn);

        Scene scene = new Scene(root, 1000, 750);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.R && selectedShip != null) {
                Ship ship = shipMap.get(selectedShip);
                rotateShip(selectedShip, ship);
            }
        });

        setScene(scene);
        setTitle("Setup");
    }

    private Image loadImage(int theme, String name) {
        return new Image(new File("lib/" + theme + "/" + name + ".png").toURI().toString());
    }

    private void createShip(String name, Ship ship, int index) {

        ImageView view = new ImageView(loadImage(playerTheme, name));

        view.setFitWidth(ship.getLength() * CELL);
        view.setFitHeight(CELL);

        // Start below board
        view.setLayoutX((index + 1) * CELL * 2);
        view.setLayoutY((SIZE + 2) * CELL);

        playerShips.add(ship);
        shipMap.put(view, ship);

        final double[] offset = new double[2];

        view.setOnMousePressed(e -> {
            selectedShip = view;
            offset[0] = e.getX();
            offset[1] = e.getY();
        });

        view.setOnMouseDragged(e -> {

            double boardX = e.getSceneX() - playerBoard.localToScene(0, 0).getX();
            double boardY = e.getSceneY() - playerBoard.localToScene(0, 0).getY();

            view.setLayoutX(boardX - offset[0]);
            view.setLayoutY(boardY - offset[1]);
        });

        view.setOnMouseReleased(e -> snapShip(view, ship));

        playerBoard.getChildren().add(view);
    }

    private void rotateShip(ImageView view, Ship ship) {
        if (ship == null) return;

        view.setRotate((view.getRotate() + 90) % 180);
        ship.rotate();
    }

    private void snapShip(ImageView view, Ship ship) {

    // 👉 Use CENTER of ship instead of top-left
    double centerX = view.getLayoutX() + view.getBoundsInParent().getWidth() / 2;
    double centerY = view.getLayoutY() + view.getBoundsInParent().getHeight() / 2;

    int col = (int)(centerX / CELL) - 1;
    int row = (int)(centerY / CELL) - 1;

    int length = ship.getLength();
    boolean horizontal = ship.isHorizontal();

    // bounds
    if (col < 0 || row < 0) return;

    if (horizontal) {
        if (col + length > SIZE) return;
    } else {
        if (row + length > SIZE) return;
    }

    // overlap
    for (int i = 0; i < length; i++) {
        int r = row + (horizontal ? 0 : i);
        int c = col + (horizontal ? i : 0);

        if (playerGrid[r][c] != null && playerGrid[r][c] != ship) return;
    }

    // clear old
    for (int r = 0; r < SIZE; r++) {
        for (int c = 0; c < SIZE; c++) {
            if (playerGrid[r][c] == ship) playerGrid[r][c] = null;
        }
    }

    // place
    ship.setPosition(row, col);

    for (int i = 0; i < length; i++) {
        int r = row + (horizontal ? 0 : i);
        int c = col + (horizontal ? i : 0);
        playerGrid[r][c] = ship;
    }

    // 🔥 Snap using correct anchor
    view.setLayoutX((col + 1) * CELL);
    view.setLayoutY((row + 1) * CELL);
}
    private void drawBoard(Pane pane) {

        pane.setStyle("-fx-background-color: lightblue; -fx-border-color: black;");

        for (int r = 0; r <= SIZE; r++) {
            for (int c = 0; c <= SIZE; c++) {

                if (r == 0 && c > 0) {
                    Label l = new Label(String.valueOf(c));
                    l.setPrefSize(CELL, CELL);
                    l.setLayoutX(c * CELL);
                    l.setLayoutY(0);
                    l.setAlignment(Pos.CENTER);
                    pane.getChildren().add(l);
                }

                if (c == 0 && r > 0) {
                    Label l = new Label(String.valueOf((char) ('A' + r - 1)));
                    l.setPrefSize(CELL, CELL);
                    l.setLayoutX(0);
                    l.setLayoutY(r * CELL);
                    l.setAlignment(Pos.CENTER);
                    pane.getChildren().add(l);
                }

                if (r > 0 && c > 0) {
                    Rectangle cell = new Rectangle(CELL, CELL);
                    cell.setStroke(Color.BLACK);
                    cell.setFill(Color.TRANSPARENT);
                    cell.setLayoutX(c * CELL);
                    cell.setLayoutY(r * CELL);
                    pane.getChildren().add(cell);
                }
            }
        }
    }

    private void placeOpponentShips() {

        Random rand = new Random();
        int[] sizes = {5, 4, 3, 3, 2};

        for (int size : sizes) {

            boolean placed = false;

            while (!placed) {

                boolean horizontal = rand.nextBoolean();
                int row = rand.nextInt(SIZE);
                int col = rand.nextInt(SIZE);

                if (horizontal && col + size > SIZE) continue;
                if (!horizontal && row + size > SIZE) continue;

                boolean valid = true;

                for (int i = 0; i < size; i++) {
                    int r = row + (horizontal ? 0 : i);
                    int c = col + (horizontal ? i : 0);

                    if (opponentGrid[r][c] == 1) {
                        valid = false;
                        break;
                    }
                }

                if (!valid) continue;

                for (int i = 0; i < size; i++) {
                    int r = row + (horizontal ? 0 : i);
                    int c = col + (horizontal ? i : 0);
                    opponentGrid[r][c] = 1;
                }

                placed = true;
            }
        }
    }
}