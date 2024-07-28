package com.server;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class encryption {
    private String password;
    private static final Logger logger = LogManager.getLogger(records.class);

    public boolean check(String mail, String hash) {
        String url = "jdbc:mysql://localhost:3306/clients";
        String username = "root";
        String dbpassword = "root";
        try (Connection connection = DriverManager.getConnection(url, username, dbpassword);
                Statement statement = connection.createStatement()) {
            String query = "SELECT passw FROM users WHERE mail='" + mail + "'";
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                password = rs.getString("passw");
            }
            return password.equals(hash);
        } catch (SQLException e) {
            logger.error("Error: {}", e.getMessage());
            return false;
        }
    }
}
