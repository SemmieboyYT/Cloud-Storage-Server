package semmieboy_yt.cloud_storage_server;

public class ShutdownHook extends Thread {
    public void run() {
        Logger.log(Logger.level.NORMAL, "Stopping server...");
        Main.webServer.stop();
    }
}
