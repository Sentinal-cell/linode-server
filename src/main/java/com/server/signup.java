package com.server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class signup implements Runnable {
    private Socket client;
    private static final Logger logger = LogManager.getLogger(transfer.class);
    private Connection connection;
    private byte[] pp;
    String[] info;
    String fname;
    String lname;
    String mail;
    int age;
    String passw;

    public signup(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        String url = conn.url();
        String username = conn.reu();
        String password = conn.rep();
        try {
            logger.info("Starting signup! ");
            DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
            connection = DriverManager.getConnection(url, username, password);
            info = dataInputStream.readUTF().split("&");
            fname = info[0];
            lname = info[1];
            mail = info[2];
            passw = info[3];
            System.out.println(fname + lname + mail);
            age = Integer.valueOf(info[4]);
            Thread.sleep(1000);
            pp = new byte[dataInputStream.readInt()];
            dataInputStream.readFully(pp);
            String query = "INSERT INTO users (fname, lname, mail, passw, balance, loan, image, age) VALUES (?, ?, ?, ?, 0,0, ?, ?)";
            PreparedStatement quer = connection.prepareStatement(query);
            quer.setString(1, fname);
            quer.setString(2, lname);
            quer.setString(3, mail);
            quer.setString(4, passw);
            quer.setBytes(5, pp);
            quer.setInt(6, age);
            quer.executeUpdate();
            dataOutputStream.writeUTF("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}