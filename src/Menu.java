import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Menu extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();

        Label title = new Label("You Sunk My Battleship!!!");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 42));

        Button startButton = new Button("Start");
        startButton.setPrefWidth(200);

        Button leaderboardButton = new Button("Leaderboard");
        leaderboardButton.setPrefWidth(200);

        Button exitButton = new Button("X");
        exitButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        exitButton.setOnAction(e -> Platform.exit());

        startButton.setOnAction(e -> {
            GameSetup setup = new GameSetup();
            setup.show();
        });

        BorderPane topPane = new BorderPane();
        topPane.setCenter(title);

        HBox exitBox = new HBox(exitButton);
        exitBox.setAlignment(Pos.TOP_RIGHT);
        topPane.setRight(exitBox);

        VBox centerBox = new VBox(20, startButton, leaderboardButton);
        centerBox.setAlignment(Pos.CENTER);

        root.setTop(topPane);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("Battleship Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}