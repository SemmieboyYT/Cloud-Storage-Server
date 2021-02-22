package semmieboy_yt.cloud_storage_server;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static boolean isDebug = false;
    public static File workDir = new File(".");

    public static void main(String[] arguments) {
        if (arguments.length > 1) {
            List<String> args = Arrays.asList(arguments);
            int length = arguments.length;

            if (args.contains("debug")) {
                isDebug = true;
                length--;
                args.remove("debug");
            }

            for (int i = 0; i < length; i += 2) {
                String arg = args.get(i);

                if (length == i++) {
                    Logger.log(Logger.level.ERROR, "Value for \""+arg+"\" is missing. Exiting to prevent possible damage.");
                    System.exit(1);
                }

                switch (arg.replaceAll("-", "")) {
                    default:
                        Logger.log(Logger.level.DEBUG, "Skipping unknown argument: "+arg);
                        break;
                    case "workDir":
                        workDir = new File(arg);

                        if (!workDir.isDirectory()) {
                            Logger.log(Logger.level.ERROR, "Value for \"workDir\" does not exist. Exiting to prevent possible damage.");
                            System.exit(1);
                        }
                        break;
                }
            }
        }
    }
}
