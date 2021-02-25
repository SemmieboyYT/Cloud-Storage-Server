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
                if (Main.debug) {
                    System.out.println("\r["+Main.timeFormat.format(java.time.LocalTime.now())+"] "+Ansi.colorize("[Debug] "+text, Attribute.ITALIC(), Attribute.DIM()));
                } else return;
                break;
            case NORMAL:
                System.out.println("\r["+Main.timeFormat.format(java.time.LocalTime.now())+"] "+text);
                break;
            case WARNING:
                System.out.println("\r["+Main.timeFormat.format(java.time.LocalTime.now())+"] "+Ansi.colorize("[WARNING] "+text, Attribute.YELLOW_TEXT()));
                break;
            case ERROR:
                System.err.println("\r["+Main.timeFormat.format(java.time.LocalTime.now())+"] "+Ansi.colorize("[ERROR] "+text, Attribute.BRIGHT_RED_TEXT()));
                break;
            case CRITICAL:
                System.err.println("\r["+Main.timeFormat.format(java.time.LocalTime.now())+"] "+Ansi.colorize("[CRITICAL] "+text, Attribute.RED_TEXT()));
                break;
        }
        if (Main.commandProcessor.active) System.out.print("> ");
    }
}
