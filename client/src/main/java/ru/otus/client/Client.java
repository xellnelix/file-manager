package ru.otus.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client implements AutoCloseable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Socket getSocket() {
        return socket;
    }

    public void connect(int port) throws IOException {
        socket = new Socket("localhost", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    try {
                        String message = in.readUTF();
                        System.out.println(message);
                    } catch (EOFException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }).start();
    }

    @Override
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String msg) throws IOException {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            close();
            throw new SocketException();
        }
    }
}
