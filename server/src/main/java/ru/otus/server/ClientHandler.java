package ru.otus.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final List<String> availableCommands = List.of("auth [login] [password]", "register [login] [password] [username]");
    private final Socket socket;
    private final Server server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String username;

    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());

    @Override
    public void run() {
        try {
            authenticateUser(server);
            communicateWithUser();

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private void authenticateUser(Server server) throws IOException {
        while (username == null && !socket.isClosed()) {
            String message = in.readUTF();
            String[] args = message.replaceAll("\\s+", " ").split(" ");
            String command = args[0];
            switch (command) {
                case "auth": {
                    if (args.length != 3) {
                        out.writeUTF("Некорректное число аргументов");
                        logger.info("Передано некорректное число аргументов: " + Arrays.toString(args));
                        continue;
                    }
                    String login = args[1];
                    String password = args[2];
                    String username = server.getDatabase().authenticateUser(login, password);
                    logger.info("Попытка найти пользователя с логином " + login);
                    if (username == null) {
                        sendMessage("Указан неверный логин/пароль");
                        logger.info("Некорректный логин/пароль: " + login);
                    } else {
                        this.username = username;
                        sendMessage("Успешная авторизация");
                        logger.info("Пользователь " + username + " подключился");
                    }
                    break;
                }
                case "register": {
                    if (args.length != 4) {
                        out.writeUTF("Некорректное число аргументов");
                        logger.info("Передано некорректное число аргументов: " + Arrays.toString(args));
                        continue;
                    }
                    String login = args[1];
                    String username = args[2];
                    String password = args[3];
                    boolean isRegistered = server.getDatabase().registerUser(login, password, username);
                    if (!isRegistered) {
                        sendMessage("Логин/имя пользователя заняты");
                        logger.info("Некорректный логин/имя пользователя " + "[" + login + "/" + username + "]");
                    } else {
                        this.username = username;
                        sendMessage("Успешная регистрация");
                        logger.info("Пользователь " + username + " подключился");
                    }
                    break;
                }
                case "exit": {
                    disconnect();
                    break;
                }
                case "help": {
                    sendMessage("Список доступных команд: ");
                    for (String inputCommand: availableCommands) {
                        sendMessage(inputCommand);
                    }
                    break;
                }
                default: {
                    sendMessage("Сперва необходимо авторизоваться или зарегистрироваться");
                    logger.info("Попытка вызова команд без авторизации/регистрации");
                }
            }
        }
    }

    private void communicateWithUser() throws IOException {
        if (socket.isClosed()) {
            return;
        }
        FileManager fm = new FileManager(username, this);
        while (socket.isConnected()) {
            try {
                String message = in.readUTF();
                fm.start(message);
            } catch (SocketException e) {
                logger.error(e.getMessage());
                break;
            }
        }
    }

    public void disconnect() {
        if (socket != null) {
            try {
                socket.close();
                logger.info("Сокет закрыт");
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
                logger.info("Входящий поток закрыт");
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
                logger.info("Исходящий поток закрыт");
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}