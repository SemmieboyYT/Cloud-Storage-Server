package semmieboy_yt.cloud_storage_server;

import javax.swing.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static boolean isDebug = false;
    public static File workDir = new File(".");
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] arguments) {
        int port = 80;
        boolean force = false;

        if (arguments.length > 1) {
            List<String> args = new ArrayList<>();
            int length = arguments.length;

            for (String arg:arguments) args.add(arg.replaceAll("-", ""));

            if (args.contains("debug")) {
                isDebug = true;
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
                            }
                        }
                        Logger.log(Logger.level.WARNING, "Value for \"port\" is not a valid port, skipping.");
                        break;
                }
            }
        }
        if (System.console() == null && !force) {
            JOptionPane.showMessageDialog(null, "This program currently only works on the command line");
            System.exit(1);
        }

        new WebServer(port);
    }
}
