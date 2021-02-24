package semmieboy_yt.cloud_storage_server;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

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
                    text = Ansi.colorize("[Debug] "+text, Attribute.ITALIC(), Attribute.DIM());
                } else return;
                break;
            case NORMAL:
                break;
            case WARNING:
                text = Ansi.colorize("[WARNING] "+text, Attribute.YELLOW_TEXT());
                break;
            case ERROR:
                text = Ansi.colorize("[ERROR] "+text, Attribute.BRIGHT_RED_TEXT());
                break;
            case CRITICAL:
                text = Ansi.colorize("[CRITICAL] "+text, Attribute.RED_TEXT());
                break;
        }
        System.out.println("["+Main.timeFormat.format(java.time.LocalTime.now())+"] "+text);
    }
}
