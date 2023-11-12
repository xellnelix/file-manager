package ru.otus.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;

public class FileManager {
    private static final Logger logger = LogManager.getLogger(FileManager.class.getName());
    private static final String rootPath = System.getProperty("user.dir") + "\\rootDir";
    private File currentDir;
    private final String username;
    private final String userPath;
    private String currentPath;
    private final ClientHandler cl;

    public FileManager(String username, ClientHandler clientHandler) {
        this.username = username;
        userPath = rootPath + "\\" + username;
        currentPath = userPath;
        createDirectory("");
        cl = clientHandler;
    }

    private void createDirectory(String dirName) {
        currentDir = new File(currentPath + "\\" + dirName);
        if (currentDir.exists()) {
            return;
        }
        if (!currentDir.mkdirs()) {
            logger.info("Не удалось создать директорию " + dirName);
            return;
        }
        cl.sendMessage("Директория создана");
        logger.info("Создана директория " + dirName + " для пользователя " + username);
//        currentDir = new File(userPath);
    }

    private void showDirectoryContent(boolean isDetailized) {
        File[] files = currentDir.listFiles();
        if (files == null || files.length == 0) {
            cl.sendMessage("Директория " + currentDir.getName() + " пустая");
            logger.info("Директория " + currentDir.getName() + "пустая");
            return;
        }
        for (File f : files) {
            cl.sendMessage("[ " + f.getName() + " ]");
            if (isDetailized && f.isFile()) {
                cl.sendMessage("\tРазмер: " + f.length() + " байт");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                cl.sendMessage("\tДата последнего изменения: " + sdf.format(f.lastModified()));
            }
        }
        logger.info("Получены данные о содержимом директории " + currentDir.getName());
    }

    private void changeDirectory(String path) {
        if (path.equals("..")) {
            if (currentDir.getPath().equals(userPath)) {
                cl.sendMessage("Вы находитесь в корневой директории");
                return;
            }
            String[] currentDirParts = currentDir.getPath().split("\\\\");
            currentPath = currentDir.getPath().substring(0, currentDir.getPath().indexOf(currentDirParts[currentDirParts.length - 1]) - 1);
        } else {
            currentPath += "\\" + path;
            if (!currentDir.exists()) {
                currentPath = userPath;
                cl.sendMessage("Поддиректории не существует");
                return;
            }
        }
        currentDir = new File(currentPath);
    }

    private void remove(String filename) {
        File deleteFile = new File(currentPath + "\\" + filename);

        if (!deleteFile.exists()) {
            cl.sendMessage("Файла/директории не существует");
            return;
        }

        File[] allData = deleteFile.listFiles();
        if (allData != null) {
            for (File f : allData) {
                remove(filename + "\\" + f.getName());
            }
        }


        if (deleteFile.delete()) {
            cl.sendMessage("Удалено успешно " + deleteFile.getName());
            logger.info("Удалено успешно " + deleteFile.getName());
        } else {
            cl.sendMessage("Не удалось удалить " + deleteFile.getName());
            logger.info("Не удалось удалить " + deleteFile.getName());
        }
    }


    private void move(String source, String destination) {
        File sourceFile = new File(currentPath + "\\" + source);
        if (sourceFile.renameTo(new File(userPath + "\\" + destination + "\\" + sourceFile.getName()))) {
            cl.sendMessage("Успешно перемещено в " + destination);
            logger.info("Перемещено " + source + " в " + destination);
        }
    }

    private void copy(String source, String destination) {
        File fileFrom = new File(userPath + "\\" + source);
        File fileTo = new File(userPath + "\\" + destination + "\\" + fileFrom.getName());
        try (DataInputStream in = new DataInputStream(new FileInputStream(fileFrom));
             DataOutputStream out = new DataOutputStream(new FileOutputStream(fileTo))) {
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
            cl.sendMessage("Успешно скопировано " + fileFrom.getName());
            logger.info("Скопировано " + fileFrom.getName());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void find(String filename, File searchFile) {
        if (searchFile.isDirectory()) {
            cl.sendMessage("Поиск по директории: " + searchFile.getName());
        }
        File[] allData = searchFile.listFiles();
        if (allData != null) {
            for (File f : allData) {
                if (f.getName().equals(filename)) {
                    cl.sendMessage("Найдено совпадение: " + searchFile.getPath() + "\\" + f.getName());
                    logger.info("Найден файл " + filename);
                    return;
                }
                find(filename, f);
            }
        }
    }

    public void showCurrentDirectory() {
        cl.sendMessage(currentPath);
    }

    public void start(String message) {

        String[] arg = message.replaceAll("\\s+", " ").split(" ");

        switch (arg[0]) {
            case ("ls"):
                if (arg.length > 2) {
                    break;
                }
                if (arg.length > 1) {
                    showDirectoryContent(true);
                }
                if (arg.length == 1) {
                    showDirectoryContent(false);
                }
                break;
            case ("mkdir"):
                if (arg.length != 2) {
                    break;
                }
                createDirectory(arg[1]);
                break;
            case ("cd"):
                if (arg.length > 2) {
                    break;
                }
                changeDirectory(arg[1]);
                break;
            case ("rm"):
                if (arg.length != 2) {
                    break;
                }
                remove(arg[1]);
                break;
            case ("mv"):
                if (arg.length != 3) {
                    break;
                }
                move(arg[1], arg[2]);
                break;
            case ("cp"):
                if (arg.length != 3) {
                    break;
                }
                copy(arg[1], arg[2]);
                break;
            case ("find"):
                if (arg.length != 2) {
                    break;
                }
                find(arg[1], new File(userPath));
                break;
            case ("where"):
                if (arg.length > 1) {
                    break;
                }
                showCurrentDirectory();
                break;
            case ("exit"):
                cl.disconnect();
                break;
        }
    }
}