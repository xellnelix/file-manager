package ru.otus.server;

import java.sql.SQLException;

public class Main {
    private static final int port = 8080;

    public static void main(String[] args) {
        try {
            Server server = new Server(port, new JdbcAuthenticationProvider());
            server.start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
