import javafx.beans.property.*;

public class GameResult {

    private final SimpleIntegerProperty gameId;
    private final SimpleIntegerProperty playerId;
    private final SimpleStringProperty name;
    private final SimpleStringProperty time;
    private final SimpleIntegerProperty shots;
    private final SimpleStringProperty result;

    public GameResult(int gameId, int playerId, String name, String time, int shots, String result) {
        this.gameId = new SimpleIntegerProperty(gameId);
        this.playerId = new SimpleIntegerProperty(playerId);
        this.name = new SimpleStringProperty(name);
        this.time = new SimpleStringProperty(time);
        this.shots = new SimpleIntegerProperty(shots);
        this.result = new SimpleStringProperty(result);
    }

    public int getGameId() { return gameId.get(); }
    public int getPlayerId() { return playerId.get(); }
    public String getName() { return name.get(); }
    public String getTime() { return time.get(); }
    public int getShots() { return shots.get(); }
    public String getResult() { return result.get(); }

    public SimpleIntegerProperty gameIdProperty() { return gameId; }
    public SimpleIntegerProperty playerIdProperty() { return playerId; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty timeProperty() { return time; }
    public SimpleIntegerProperty shotsProperty() { return shots; }
    public SimpleStringProperty resultProperty() { return result; }
}