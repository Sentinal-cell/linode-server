package com.server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class transfer implements Runnable {
    private Socket client;
    private String mail;
    private String passw;
    private String sid;
    private String rmail;
    private String rb;
    private Connection connection;
    private Statement statement;
    private static final Logger logger = LogManager.getLogger(transfer.class);

    public transfer(Socket client, String sid) {
        this.client = client;
        this.sid = sid;
    }

    @Override
    public void run() {
        String url = conn.url();
        String username = conn.reu();
        String password = conn.rep();
        String fdata = null;
        try {
            logger.info("starting transfer session...");
            DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            String[] rec = dataInputStream.readUTF().split("&");
            String bname = rec[0];
            rmail = rec[1];
            logger.info("receiver mail: {}", rmail);
            logger.info("receiver bank: {}", "bank");
            String query1 = "SELECT * FROM users WHERE mail='" + rmail + "'";
            ResultSet resultSet = statement.executeQuery(query1);
            String fname = null;
            String lname = null;
            switch (bname) {
                case "bank":
                    String rbname = "bank";
                    while (resultSet.next()) {
                        fname = resultSet.getString("fname");
                        lname = resultSet.getString("lname");
                        System.out.println("receiver is " + fname + " " + lname);
                        dataOutputStream.writeUTF(fname + "&" + lname);
                        ftran ftran = new ftran(client, sid, passw, rbname);
                        Thread thread = new Thread(ftran);
                        thread.start();
                        break;
                    }
            }
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }

    }
}
