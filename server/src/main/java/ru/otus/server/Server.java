package ru.otus.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class.getName());
    private final int port;
    private static final ExecutorService thread = Executors.newFixedThreadPool(8);
    private final JdbcAuthenticationProvider database;


    public Server(int port, JdbcAuthenticationProvider database) throws SQLException {
        this.port = port;
        this.database = database;
    }

    public JdbcAuthenticationProvider getDatabase() {
        return database;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту " + port);
            while (!server.isClosed()) {
                Socket client = server.accept();
                thread.execute(new ClientHandler(client, this));
            }
        } catch (IOException e) {
            thread.shutdown();
            logger.error(e.getStackTrace());
            throw new RuntimeException(e);
        }
    }
}
