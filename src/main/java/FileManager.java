import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class FileManager {
    private static final String rootPath = System.getProperty("user.dir") + "\\rootDir";
    private File file;
    private File rootDir;
    private String username;
    private String userPath;
    private String currentPath;
    private static final Logger logger = LogManager.getLogger(FileManager.class.getName());

    public FileManager(String username) {
        this.username = username;
        userPath = rootPath + "\\" + username;
        rootDir = new File(userPath);
        currentPath = userPath;
        mkdir("");
    }

    private void mkdir(String dirName) {
        file = new File(currentPath + "\\" + dirName);
        if (file.exists()) {
            logger.info("Директория пользователя уже существует");
            return;
        }
        if (!file.mkdir()) {
            logger.error("Не удалось создать директорию пользователя " + username);
            return;
        }
        System.out.println("Директория создана");
        file = new File(userPath);
    }

    public void ls(boolean flag) {
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            logger.error("Директория пустая");
            return;
        }
        for (File f : files) {
            System.out.println("[ " + f.getName() + " ]");
            if (flag && f.isFile()) {
                System.out.println("\tРазмер: " + f.length() + " байт");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("\tДата последнего изменения: " + sdf.format(f.lastModified()));
            }
        }
    }

    public void cd(String path) {
        if (path.equals("..")) {
            if (file.getPath().equals(userPath)) {
                System.out.println("Вы находитесь в корневой директории");
                return;
            }
            String[] currentDirParts = file.getPath().split("\\\\");
            currentPath = file.getPath().substring(0, file.getPath().indexOf(currentDirParts[currentDirParts.length - 1]) - 1);
        } else {
            currentPath += "\\" + path;
            file = new File(currentPath);
            if (!file.exists()) {
                currentPath = userPath;
                System.out.println("Поддиректории не существует");
                return;
            }
        }
        file = new File(currentPath);
        System.out.println(file.getPath());
    }

    public void rm(String filename) {
        File file = new File(currentPath + "\\" + filename);

        if (!file.exists()) {
            System.out.println("Не существует");
            return;
        }

        if (file.isDirectory()) {
            File[] allData = file.listFiles();
            if (allData != null) {
                for (File f : allData) {
                    rm(filename + "\\" + f.getName());
                }
            }
        }

        if (file.delete()) {
            System.out.println(file.getName() + " удален");
        } else {
            System.out.println("Ошибка удаления");
        }
    }

    public void mv(String source, String destination) {
        File sourceFile = new File(currentPath + "\\" + source);
        System.out.println(sourceFile.getPath());
        System.out.println(currentPath);
        System.out.println(new File(currentPath + "\\" + destination + "\\" + sourceFile.getName()).getPath());
        System.out.println(sourceFile.renameTo(new File(userPath + "\\" + destination + "\\" + sourceFile.getName())));
    }

    public void cp(String source, String destination) {
        File fileFrom = new File(userPath + "\\" + source);
        File fileTo = new File(userPath + "\\" + destination + "\\" + source);
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileFrom));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(fileTo))) {
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void find(String filename, File file) {
        if (file.isDirectory()) {
            System.out.println("Поиск в директории: " + file.getPath());
            File[] allData = file.listFiles();
            if (allData != null) {
                for (File f : allData) {
                    if (f.getName().equals(filename)) {
                        System.out.println("Найдено совпадение: " + file.getPath() + "\\" + f.getName());
                        return;
                    }
                    find(filename, new File(f.getPath()));
                }
            }
        }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        scanning:
        while (scanner.hasNextLine()) {
            String message = scanner.nextLine();
            String[] arg = message.replaceAll("\\s+", " ").split(" ");

            switch (arg[0]) {
                case ("ls"):
                    if (arg.length > 2) {
                        continue;
                    }
                    if (arg.length > 1) {
                        ls(true);
                    }
                    if (arg.length == 1) {
                        ls(false);
                    }
                    break;
                case ("mkdir"):
                    if (arg.length != 2) {
                        continue;
                    }
                    mkdir(arg[1]);
                    break;
                case ("cd"):
                    if (arg.length > 2) {
                        continue;
                    }
                    cd(arg[1]);
                    break;
                case ("rm"):
                    if (arg.length != 2) {
                        continue;
                    }
                    rm(arg[1]);
                    break;
                case ("mv"):
                    if (arg.length != 3) {
                        continue;
                    }
                    mv(arg[1], arg[2]);
                    break;
                case ("cp"):
                    if (arg.length != 3) {
                        continue;
                    }
                    cp(arg[1], arg[2]);
                    break;
                case ("find"):
                    if (arg.length != 2) {
                        continue;
                    }
                    find(arg[1], file);
                    break;
                case ("exit"):
                    break scanning;
            }
        }
    }


}