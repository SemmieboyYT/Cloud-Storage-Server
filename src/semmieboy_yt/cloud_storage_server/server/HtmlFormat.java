package semmieboy_yt.cloud_storage_server.server;

import semmieboy_yt.cloud_storage_server.Main;

import java.time.LocalDateTime;

public class HtmlFormat {
    public static String format(String html) {
        LocalDateTime localDateTime = LocalDateTime.now();
        return html
                .replace("%date%", Main.dateFormat.format(localDateTime))
                .replace("%time%", Main.timeFormat.format(localDateTime));
    }
}
