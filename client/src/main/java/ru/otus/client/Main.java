package ru.otus.client;

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Client client = new Client()){
            client.connect(8080);
            Scanner scanner = new Scanner(System.in);
            System.out.println("Соединение установлено");
            while (client.getSocket().isConnected()) {
                String msg = scanner.nextLine();
                try {
                    client.sendMessage(msg);
                } catch (SocketException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
