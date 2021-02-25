package semmieboy_yt.cloud_storage_server;

public class ShutdownHook extends Thread {
    public void run() {
        Main.commandProcessor.active = false;
        if (Main.webServer.running) {
            Logger.log(Logger.level.NORMAL, "Stopping server...");
            Main.webServer.stop();
        }
        Logger.log(Logger.level.NORMAL, "Exiting program...");
    }
}
