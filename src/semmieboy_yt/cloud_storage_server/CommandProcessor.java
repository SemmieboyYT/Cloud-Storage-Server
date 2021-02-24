package semmieboy_yt.cloud_storage_server;

import java.io.InputStream;
import java.util.Scanner;

public class CommandProcessor {
    private Scanner scanner;
    public boolean active = false;

    public CommandProcessor(InputStream inputStream) {
        scanner = new Scanner(inputStream);
    }

    public void start() {
        active = true;
        read();
    }

    private void read() {
        new Thread(() -> {
            if (active) {
                System.out.print("> ");
                String[] args = scanner.nextLine().split(" ");
                switch (args[0]) {
                    default:
                        Logger.log(Logger.level.NORMAL, "Unknown command, type \"help\" or \"?\" to see a list of commands");
                        break;
                    case "help":
                    case "?":
                        Logger.log(Logger.level.NORMAL, "help:   Show a list of possible commands");
                        Logger.log(Logger.level.NORMAL, "?:      Show a list of possible commands");
                        Logger.log(Logger.level.NORMAL, "stop:   Stop the server");
                        break;
                    case "stop":
                        System.exit(0);
                        break;
                }
                read();
            }
        }).start();
    }
}
