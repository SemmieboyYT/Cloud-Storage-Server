package semmieboy_yt.cloud_storage_server;

public class Logger {
    public enum level {
        DEBUG,
        NORMAL,
        WARNING,
        ERROR,
        CRITICAL
    }

    public static void log(level level, String text) {
        switch (level) {
            case DEBUG:
                if (Main.isDebug) {
                    text = "[Debug] "+text;
                } else return;
                break;
            case NORMAL:
                break;
            case WARNING:
                text = "[WARNING] "+text;
                break;
            case ERROR:
                text = "[ERROR] "+text;
                break;
            case CRITICAL:
                text = "[CRITICAL] "+text;
                break;
        }
        System.out.println("["+java.time.LocalTime.now()+"] "+text);
    }
}
