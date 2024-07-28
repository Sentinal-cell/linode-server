package com.server;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class records implements Runnable {
    private String sender;
    private String receiver;
    private String tid;
    private int amount;
    private String date;
    private String rbank;
    private Socket client;
    private String session_id;
    private Connection connection;
    private Statement statement;
    private String mail;
    private String path = "users_tran/";
    private String filepath;
    private int counter = 0;
    private static final Logger logger = LogManager.getLogger(records.class);

    public records(Socket client, String session_id) {
        this.client = client;
        this.session_id = session_id;
    }

    @Override
    public void run() {
        String url = conn.url();
        String username = conn.reu();
        String password = conn.rep();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 10;
        char[] randomString = new char[length];
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            randomString[i] = characters.charAt(randomIndex);
        }
        String filepath = path + new String(randomString) + ".txt";
        System.out.println(filepath);
        try {
            logger.info("checking records for sid: {}", session_id);
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            String query = "select mail from active where sid='" + session_id + "'";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                mail = resultSet.getString("mail");
            }
            String query1 = "select * from transactions where sender='" + mail + "' or receiver='" + mail + "'";
            ResultSet resultSet2 = statement.executeQuery(query1);
            while (resultSet2.next()) {
                counter++;
                sender = resultSet2.getString("sender");
                receiver = resultSet2.getString("receiver");
                tid = resultSet2.getString("tid");
                amount = resultSet2.getInt("amount");
                date = resultSet2.getString("date");
                rbank = resultSet2.getString("rbank");
                String record = tid + ", " + sender + ", " + receiver + ", " + amount + ", " + date + ", " + rbank;
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
                    writer.write(record);
                    writer.newLine();
                }
            }
            logger.info("All records retrieved...");
            // FileInputStream fileInputStream = new FileInputStream("records_transfer."\);
            DataOutputStream dout = new DataOutputStream(client.getOutputStream());
            BufferedReader in = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = in.readLine()) != null) {
                dout.writeUTF(line);
                dout.flush();
                Thread.sleep(500);
            }
            in.close();
            dout.writeUTF("end");
            dout.flush();
            File fileToDelete = new File(filepath);
            fileToDelete.delete();
            client.close();
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
    }

}
