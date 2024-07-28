package com.server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.TimeUnit;

public class ftran implements Runnable {
    private static int sbal;
    private static int rbal;
    public Socket client;
    private String rbank;
    private String session_id;
    private String receiver;
    private int amount;
    private String sender;
    private String tr[];
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String receiver_mail;
    private String query1;
    private String status;
    private String query2;
    private boolean tup = true;
    private static final Logger logger = LogManager.getLogger(records.class);

    public ftran(Socket client, String session_id, String receiver_mail, String rbank) {
        this.client = client;
        this.session_id = session_id;
        this.receiver_mail = receiver_mail;
        this.rbank = rbank;
    }

    @Override
    public void run() {
        String url = conn.url();
        String username = conn.reu();
        String password = conn.rep();
        logger.info("session timer started...");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            System.out.println("5 minutes is up! session ended");
            scheduler.shutdown();
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Error: {}", e.getMessage());
            }
        }, 10, TimeUnit.MINUTES);
        try {
            dataInputStream = new DataInputStream(client.getInputStream());
            dataOutputStream = new DataOutputStream(client.getOutputStream());
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            // Statement statement2 = connection.createStatement();
            logger.info("session id: {}", session_id);
            String check_mail = "SELECT * FROM active WHERE sid='" + session_id + "'";
            ResultSet resultSet = statement.executeQuery(check_mail);
            String sid = null;
            String fname = null;
            String lname = null;
            String mail = null;
            int sbalance = 0;
            int loan = 0;
            logger.info("fetching sender mail from DB...");
            while (resultSet.next()) {
                mail = resultSet.getString("mail");
            }
            resultSet.close();
            logger.info("fetching sender info from DB...");
            String fetch = "SELECT * FROM users WHERE mail='" + mail + "'";
            ResultSet resultSet2 = statement.executeQuery(fetch);
            while (resultSet2.next()) {
                sid = resultSet2.getString("id");
                fname = resultSet2.getString("fname");
                lname = resultSet2.getString("lname");
                sbalance = resultSet2.getInt("balance");
                loan = resultSet2.getInt("loan");
            }
            resultSet2.close();
            tr = dataInputStream.readUTF().split("&");
            logger.info("receiver: ", tr[0]);
            receiver = tr[0];
            amount = Integer.parseInt(tr[1]);
            sender = tr[2];
            String query = "SELECT * FROM users WHERE mail='" + receiver + "'";
            String rfname = null;
            String rlname = null;
            String rmail = null;
            int rbalance = 0;
            logger.info("fetching receiver info from DB...");
            ResultSet resultSet3 = statement.executeQuery(query);
            while (resultSet3.next()) {
                rfname = resultSet3.getString("fname");
                rlname = resultSet3.getString("lname");
                rmail = resultSet3.getString("mail");
                rbalance = resultSet3.getInt("balance");
            }
            resultSet3.close();
            if (amount <= sbalance) {
                sbal = sbalance - amount;
                rbal = rbalance + amount;
                String upquer = "UPDATE users SET balance=" + sbal + " WHERE mail='" + mail + "'";
                String upquer2 = "UPDATE users SET balance=" + rbal + " WHERE mail='" + receiver + "'";
                try {
                    logger.info("Updating users table");
                    statement.executeUpdate(upquer);
                    statement.executeUpdate(upquer2);
                } catch (Exception e) {
                    logger.error("Error: {}", e.getMessage());
                }
            }

            try {
                status = "successful";
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                int length = 10;
                char[] randomString = new char[length];
                Random random = new Random();
                for (int i = 0; i < length; i++) {
                    int randomIndex = random.nextInt(characters.length());
                    randomString[i] = characters.charAt(randomIndex);
                }
                String tid = new String(randomString);
                String amt = Integer.toString(amount);
                dataOutputStream.writeUTF(mail + "&" + rmail + "&" + status + "&" + amt + "&" + timestamp + "&" + tid);
                String update_transaction = "INSERT INTO Transactions (tid, Date, Sender, Receiver, Rbank, Amount, rec_conf) VALUES ('"
                        + tid + "', '" + timestamp + "', '" + mail + "', '" + rmail + "', '" + rbank + "', " + amount
                        + ", 'false')";
                logger.info("Updating transactions table");
                statement.executeUpdate(update_transaction);
                connection.close();
            } catch (Exception e) {
                logger.error("Error: {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
    }
}
