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
                        Logger.log(Logger.level.NORMAL, "help:         Show a list of possible commands");
                        Logger.log(Logger.level.NORMAL, "?:            Show a list of possible commands");
                        Logger.log(Logger.level.NORMAL, "stop:         Stop the server");
                        Logger.log(Logger.level.NORMAL, "start:        Start the server");
                        Logger.log(Logger.level.NORMAL, "exit:         Exit the program");
                        Logger.log(Logger.level.NORMAL, "lockdown:     Stops any new HTTP request from processing, toggles");
                        Logger.log(Logger.level.NORMAL, "port:         Set the port, will require the server to restart");
                        Logger.log(Logger.level.NORMAL, "buffersize    Set buffer size in bytes, higher values equal to more speed but more RAM usage");
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
                            Main.webServer.start(Main.port);
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
                    case "port":
                        if (args.length > 1) {
                            if (args[1].matches("[0-9]*")) {
                                int parseInt = Integer.parseInt(args[1]);
                                if (parseInt >= 1 && parseInt <= 65535) {
                                    Main.port = parseInt;
                                    Logger.log(Logger.level.NORMAL, "Set the port to "+parseInt);
                                    break;
                                }
                            }
                            Logger.log(Logger.level.NORMAL, "That's not a valid port");
                            break;
                        }
                        Logger.log(Logger.level.NORMAL, "The current port is "+Main.port);
                        break;
                    case "buffersize":
                        if (args.length > 1) {
                            if (args[1].matches("[0-9]*")) {
                                int parseInt = Integer.parseInt(args[1]);
                                if (parseInt > 0) {
                                    Main.bufferSize = parseInt;
                                    Logger.log(Logger.level.NORMAL, "Set the buffer size to "+parseInt+" bytes");
                                    break;
                                }
                            }
                            Logger.log(Logger.level.NORMAL, "That's not a valid buffer size");
                            break;
                        }
                        Logger.log(Logger.level.NORMAL, "The current buffer size is "+Main.bufferSize+" bytes");
                        break;
                }
                read();
            }
        }).start();
    }
}
