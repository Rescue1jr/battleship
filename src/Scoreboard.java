import java.sql.*;

public class Scoreboard {

    private Connection connection;

    public Scoreboard() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/battleship",
                    "root",
                    "ThisisMine3"
            );

            initializeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveGameResult(String username, int time, int shots, boolean win) {

        if (connection == null) {
            System.out.println("Database connection failed. Skipping save.");
            return;
        }

        try {
            int playerId = getOrCreatePlayer(username);

            String victory = win ? "WIN" : "LOSS";

            String query = "INSERT INTO game_results (player_id, time, shots, victory) VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, playerId);
            stmt.setString(2, time + "s");
            stmt.setInt(3, shots);
            stmt.setString(4, victory);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getOrCreatePlayer(String username) throws SQLException {

        // Check if player exists
        String select = "SELECT player_id FROM player WHERE name = ?";
        PreparedStatement selectStmt = connection.prepareStatement(select);
        selectStmt.setString(1, username);

        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("player_id");
        }

        // Insert new player
        String insert = "INSERT INTO player (name) VALUES (?)";
        PreparedStatement insertStmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, username);
        insertStmt.executeUpdate();

        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }

        throw new SQLException("Failed to create player.");
    }
    
    private void initializeDatabase() {

        try {

            Statement stmt = connection.createStatement();

            // Player table
            String playerTable = "CREATE TABLE IF NOT EXISTS player (" +
                    "player_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(45) UNIQUE" +
                    ")";

            // Game results table
            String resultsTable = "CREATE TABLE IF NOT EXISTS game_results (" +
                    "game_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_id INT, " +
                    "time VARCHAR(45), " +
                    "shots INT, " +
                    "victory VARCHAR(5), " +
                    "FOREIGN KEY (player_id) REFERENCES player(player_id)" +
                    ")";

            stmt.execute(playerTable);
            stmt.execute(resultsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getLeaderboard() {

        try {

            String query =
                    "SELECT g.game_ID, p.player_id, p.name, g.time, g.shots, g.victory " +
                    "FROM game_results g " +
                    "JOIN player p ON g.player_id = p.player_id " +
                    "ORDER BY g.game_ID DESC";

            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}