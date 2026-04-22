import java.sql.*;

public class Scoreboard {

    private Connection connection;

    public Scoreboard() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //force load driver

            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/battleship",
                    "root",
                    "ThisisMine3"
            );

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
}