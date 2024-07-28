package com.server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class user_information implements Runnable {
    private Socket client;
    private String mail;
    private String passw;
    private boolean state;
    private static final Logger logger = LogManager.getLogger(user_information.class);

    public user_information(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        String url = "jdbc:mysql://localhost:3306/clients";
        String username = "root";
        String password = "root";
        String fdata = null;
        try {
            logger.info("Fetching user data...");
            DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            String[] mg = dataInputStream.readUTF().split("&");
            logger.info("Checking user information...");
            mail = mg[0];
            passw = mg[1];
            encryption encr = new encryption();
            state = encr.check(mail, passw);
            if (state) {
                logger.info("username and password correct...");
                String fetch_query = "SELECT * FROM users WHERE mail = '" + mail + "' AND passw = '" + passw + "'";
                logger.info("Fetching user data from DB...");
                ResultSet resultSet = statement.executeQuery(fetch_query);
                int id = 0;
                String fname = null;
                String lname = null;
                int age = 0;
                String mail = null;
                String passw = null;
                int balance = 0;
                int loan = 0;
                byte[] pp = null;
                while (resultSet.next()) {
                    logger.info("user info fetch was a success");
                    id = resultSet.getInt("id");
                    fname = resultSet.getString("fname");
                    lname = resultSet.getString("lname");
                    age = resultSet.getInt("age");
                    mail = resultSet.getString("mail");
                    passw = resultSet.getString("passw");
                    pp = resultSet.getBytes("image");
                    balance = resultSet.getInt("balance");
                    loan = resultSet.getInt("loan");
                }
                logger.info("creating session id...");
                String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                int length = 10;
                char[] randomString = new char[length];
                Random random = new Random();
                for (int i = 0; i < length; i++) {
                    int randomIndex = random.nextInt(characters.length());
                    randomString[i] = characters.charAt(randomIndex);
                }
                String sid = new String(randomString);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String active_update = "INSERT INTO active VALUES ('" + id + "', '" + sid + "', '" + fname + "', '"
                        + lname + "', '" + mail + "', " + balance + ", " + loan + ", '" + timestamp + "')";
                String del_id = "DELETE FROM active WHERE mail='" + mail + "'";
                try {
                    statement.executeUpdate(active_update);
                } catch (SQLException e) {
                    if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                        statement.executeUpdate(del_id);
                        try {
                            statement.executeUpdate(active_update);
                        } catch (SQLException z) {
                            logger.error("Error: {}", z.getMessage());
                        }
                    }
                }
                logger.info("active table appended");
                logger.info("Sending data to client...");
                dataOutputStream.writeUTF("success");
                Thread.sleep(2000);
                dataOutputStream.writeUTF(sid);
                Thread.sleep(2000);
                fdata = sid + "&" + fname + "&" + lname + "&" + age + "&" + mail + "&" + passw + "&" + balance + "&"
                        + loan;
                dataOutputStream.writeUTF(fdata);
                dataOutputStream.flush();
                Thread.sleep(1000);
                if (pp != null) {
                    logger.info("there is a picture");
                    dataOutputStream.writeInt(pp.length);
                    Thread.sleep(1000);
                    dataOutputStream.write(pp);
                } else {
                    logger.info("profile picture sent");
                }
                logger.info("Data sent...");
                client.close();
                connection.close();
                logger.info("Connection closed...");
            } else {
                dataOutputStream.writeUTF("Invalid");
                logger.info("Client used wrong usrname or password...");
                client.close();
            }
        } catch (SQLException e) {
            logger.error("Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
    }
}
