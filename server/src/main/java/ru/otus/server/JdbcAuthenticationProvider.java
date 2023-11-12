package ru.otus.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class JdbcAuthenticationProvider  {
    private static final Logger logger = LogManager.getLogger(JdbcAuthenticationProvider.class.getName());
    //    private static final String GET_ADMIN = "select username from chat_user cu join user_role ur on cu.role_id = ur.id and ur.role = 'ADMIN'";
    private static final String GET_USER_AUTH = "select username from file_manager_user where login = ? and password = ?";
    private static final String GET_USER_BY_USERNAME = "select * from file_manager_user where username = ?";
    private static final String GET_USER = "select login, username from file_manager_user where login = ? or username = ?";
    private static final String REG_USER = "insert into file_manager_user (username, password, login) values (?, ?, ?)";
    //    private static final String DELETE_USER = "delete from chat_user where username = ?";
    private static final String DB_URL = "jdbc:postgresql://localhost/file-manager";
    private static final String DB_LOGIN = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private final Connection databaseConnection = DriverManager.getConnection(DB_URL, DB_LOGIN, DB_PASSWORD);

    public JdbcAuthenticationProvider() throws SQLException {
    }

    public String authenticateUser(String login, String password) {
        try (ResultSet rs = sqlReadParams(GET_USER_AUTH, login, password)) {
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public boolean registerUser(String login, String password, String username) {
        try (ResultSet rs = sqlReadParams(GET_USER, login, username)) {
            if (rs.next()) {
                return false;
            }
            if (sqlModifyParams(REG_USER, login, password, username) != 0) {
                return true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private ResultSet sqlReadParams(String query, String... params) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int sqlModifyParams(String query, String... params) {
        try {
            PreparedStatement ps = databaseConnection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
