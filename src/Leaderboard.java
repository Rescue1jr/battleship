import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.ResultSet;

public class Leaderboard extends Stage {

    private TableView<GameResult> table;
    private ObservableList<GameResult> data;

    public Leaderboard() {

        Scoreboard scoreboard = new Scoreboard();
        data = FXCollections.observableArrayList();

        table = new TableView<>();

        Label title = new Label("Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        // 🔹 Columns
        TableColumn<GameResult, Number> gameIdCol = new TableColumn<>("Game ID");
        gameIdCol.setCellValueFactory(c -> c.getValue().gameIdProperty());

        TableColumn<GameResult, Number> playerIdCol = new TableColumn<>("Player ID");
        playerIdCol.setCellValueFactory(c -> c.getValue().playerIdProperty());

        TableColumn<GameResult, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());

        TableColumn<GameResult, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(c -> c.getValue().timeProperty());

        // ✅ FIXED TIME SORTING
        timeCol.setComparator((t1, t2) -> {
            int time1 = Integer.parseInt(t1.replace("s", ""));
            int time2 = Integer.parseInt(t2.replace("s", ""));
            return Integer.compare(time1, time2);
        });

        TableColumn<GameResult, Number> shotsCol = new TableColumn<>("Shots");
        shotsCol.setCellValueFactory(c -> c.getValue().shotsProperty());

        TableColumn<GameResult, String> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(c -> c.getValue().resultProperty());

        // Color wins/losses (FIXED for Java 8)
        resultCol.setCellFactory(column -> new TableCell<GameResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (item.equalsIgnoreCase("WIN")) {
                        setTextFill(Color.GREEN);
                    } else {
                        setTextFill(Color.RED);
                    }
                }
            }
        });

        // Add columns
        table.getColumns().addAll(
                gameIdCol, playerIdCol, nameCol, timeCol, shotsCol, resultCol
        );

        // Load data
        loadData(scoreboard);
        table.setItems(data);

        // Default sort: newest first
        gameIdCol.setSortType(TableColumn.SortType.DESCENDING);
        table.getSortOrder().add(gameIdCol);

        // Optional UI polish
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox root = new VBox(10, title, table);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 900, 600);
        setScene(scene);
        setTitle("Leaderboard");
    }

    private void loadData(Scoreboard scoreboard) {

        try {

            ResultSet rs = scoreboard.getLeaderboard();

            while (rs != null && rs.next()) {

                data.add(new GameResult(
                        rs.getInt("game_ID"),
                        rs.getInt("player_id"),
                        rs.getString("name"),
                        rs.getString("time"),
                        rs.getInt("shots"),
                        rs.getString("victory")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}