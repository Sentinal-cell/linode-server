package com.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class conn {
    private static String ip;
    private static int port;
    private static String inf;
    private static String[] minf;
    private static String status;
    private static String session_id;
    private static String type;
    private static final Logger logger = LogManager.getLogger(conn.class);
    
    public static void main(String[] args) throws Exception {
        port = 3333;
        InetAddress addr = InetAddress.getByName("0.0.0.0");
        ServerSocket serverSocket = new ServerSocket(port, 50, addr);
        try{
            logger.info("Server is running on : {}:{}", addr.getHostAddress(), port);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("New connection...");
                logger.info("Client IP: {}", socket.getInetAddress());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                minf = dataInputStream.readUTF().split("&");
                status = minf[0];
                session_id = minf[1];
                type = minf[2];

                if (status.equals("old")) {
                    logger.info("Client is old...");
                    switch (type) {
                        case "tran":
                            logger.info("Type: Transfer");
                            transfer transfer = new transfer(socket, session_id);
                            Thread thread = new Thread(transfer);
                            thread.start();
                            break;
                        case "alerts":
                            logger.info("Type: Alerts");
                            alert_check alert_check = new alert_check(socket, session_id);
                            Thread thread2 = new Thread(alert_check);
                            thread2.start();
                            break;
                        case "empty":
                            user_information uinf = new user_information(socket);
                            Thread thread3 = new Thread(uinf);
                            thread3.start();
                            break;
                        case "records":
                            logger.info("Type: Records");
                            records records = new records(socket, session_id);
                            Thread thread4 = new Thread(records);
                            thread4.start();
                            break;
                    }
                } else {
                    logger.info("Type: Signup");
                    signup signup = new signup(socket);
                    Thread thread5 = new Thread(signup);
                    thread5.start();
                }
            } catch (IOException e) {
                logger.error("Connection error: ", e);
            }
        }
    }
}
