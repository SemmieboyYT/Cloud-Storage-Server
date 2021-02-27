package semmieboy_yt.cloud_storage_server;

import semmieboy_yt.cloud_storage_server.server.WebServer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static boolean debug = false;
    public static File workDir = new File(".");
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static WebServer webServer;
    public static CommandProcessor commandProcessor = new CommandProcessor(System.in);
    public static int bufferSize = 10240;
    public static int port = 80;
    public static File loginData;

    public static void main(String[] arguments) {
        boolean force = false;

        if (arguments.length > 0) {
            List<String> args = new ArrayList<>();
            int length = arguments.length;

            for (String arg:arguments) args.add(arg.replaceAll("-", ""));

            if (args.contains("debug")) {
                debug = true;
                length--;
                args.remove("debug");
            }

            if (args.contains("force")) {
                force = true;
                length--;
                args.remove("force");
            }

            for (int i = 0; i < length; i += 2) {
                String arg = args.get(i);
                String value = args.get(i+1);

                if (length == i+1) {
                    Logger.log(Logger.level.ERROR, "Value for \""+arg+"\" is missing. Exiting to prevent possible damage.");
                    System.exit(1);
                }

                switch (arg) {
                    default:
                        Logger.log(Logger.level.DEBUG, "Skipping unknown argument: "+arg);
                        break;
                    case "workDir":
                        workDir = new File(value);

                        if (!workDir.isDirectory()) {
                            Logger.log(Logger.level.ERROR, "Directory \""+value+"\" does not exist. Unable to continue.");
                            System.exit(1);
                        }
                        break;
                    case "port":
                        if (value.matches("[0-9]*")) {
                            int parseInt = Integer.parseInt(value);
                            if (parseInt >= 1 && parseInt <= 65535) {
                                port = parseInt;
                                break;
                            }
                        }
                        Logger.log(Logger.level.WARNING, "Value for \"port\" is not a valid port, skipping.");
                        break;
                    case "bufferSize":
                        if (value.matches("[0-9]*")) {
                            int parseInt = Integer.parseInt(value);
                            if (parseInt > 0) {
                                bufferSize = parseInt;
                                break;
                            }
                        }
                        Logger.log(Logger.level.WARNING, "Value for \"bufferSize\" is invalid, skipping");
                        break;
                }
            }
        }

        if (System.console() == null && !force) {
            Logger.log(Logger.level.WARNING, "Detected that program is not running in console, please try running with -force");
            JOptionPane.showMessageDialog(null, "This program currently only works on the command line");
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        loginData = new File(workDir.getPath()+File.separator+"accounts.json");

        try {
            loginData.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
            Logger.log(Logger.level.CRITICAL, "Login data file could not be created, unable to continue");
            System.exit(1);
        }

        webServer = new WebServer(port);

        if (webServer.running) {
            Logger.log(Logger.level.NORMAL, "Server has started on port "+port);
        }

        commandProcessor.start();
        Logger.log(Logger.level.NORMAL, "Type \"help\" or \"?\" for help");
    }
}
