package semmieboy_yt.cloud_storage_server;

import java.io.InputStream;
import java.util.Scanner;

public class CommandProcessor {
    private final Scanner scanner;
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
                String[] args = scanner.nextLine().split(" ");
                switch (args[0]) {
                    default:
                        Logger.log(Logger.level.NORMAL, "Unknown command, type \"help\" or \"?\" to see a list of commands");
                        break;
                    case "help":
                    case "?":
                        Logger.log(Logger.level.NORMAL, "help:       Show a list of possible commands");
                        Logger.log(Logger.level.NORMAL, "?:          Show a list of possible commands");
                        Logger.log(Logger.level.NORMAL, "stop:       Stop the server");
                        Logger.log(Logger.level.NORMAL, "start:      Start the server");
                        Logger.log(Logger.level.NORMAL, "exit:       Exit the program");
                        Logger.log(Logger.level.NORMAL, "lockdown:   Stops all HTTP requests from processing, toggles");
                        break;
                    case "stop":
                        if (Main.webServer.running) {
                            Main.webServer.stop();
                            if (!Main.webServer.running) {
                                Logger.log(Logger.level.NORMAL, "Server stopped");
                            }
                        } else {
                            Logger.log(Logger.level.ERROR, "Server already stopped");
                        }
                        break;
                    case "start":
                        if (Main.webServer.running) {
                            Logger.log(Logger.level.ERROR, "Server already started");
                        } else {
                            Main.webServer.start();
                            if (Main.webServer.running) Logger.log(Logger.level.NORMAL, "Server has started");
                        }
                        break;
                    case "lockdown":
                        Main.webServer.lockdown = !Main.webServer.lockdown;
                        Logger.log(Logger.level.NORMAL, "Set lockdown mode to "+Main.webServer.lockdown);
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                }
                read();
            }
        }).start();
    }
}
