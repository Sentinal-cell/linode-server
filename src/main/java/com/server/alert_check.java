package com.server;
import java.net.Socket;
import java.sql.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class alert_check implements Runnable {
    String session_id;
    Socket client;
    String tid;
    String date;
    String sender;
    String mail;
    int amount;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private static final Logger logger = LogManager.getLogger(alert_check.class);

    public alert_check(Socket client, String session_id) {
        this.client = client;
        this.session_id = session_id;
    }

    @Override
    public void run() {
        String status = "successful";
        String url = "jdbc:mysql://localhost:3306/clients";
        String username = "root";
        String password = "root";
        String query1 = "SELECT mail FROM active WHERE sid='" + session_id + "'";
        try {
            dataInputStream = new DataInputStream(client.getInputStream());
            dataOutputStream = new DataOutputStream(client.getOutputStream());
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query1);
            while (resultSet.next()) {
                mail = resultSet.getString("mail");
            }
            String query2 = "SELECT * FROM transactions WHERE receiver='" + mail + "' AND  rec_conf='false'";
            ResultSet resultSet2 = statement.executeQuery(query2);
            if (resultSet2.next()) {
                dataOutputStream.writeUTF("true");
                tid = resultSet2.getString("tid");
                date = resultSet2.getString("date");
                sender = resultSet2.getString("sender");
                amount = resultSet2.getInt("amount");
                dataOutputStream.writeUTF(tid + "&" + date + "&" + sender + "&" + String.valueOf(amount));
                String query3 = "update transactions set rec_conf='true' where receiver='" + mail + "'";
                statement.executeUpdate(query3);
            } else {
                dataOutputStream.writeUTF("false");
            }
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
    }
};