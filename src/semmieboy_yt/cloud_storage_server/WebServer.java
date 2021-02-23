package semmieboy_yt.cloud_storage_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class WebServer {
    //TODO: add commands for the server

    HttpServer httpServer;
    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
    public WebServer(int port) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            //TODO: catch java.net.BindException
        } catch (IOException exception) {
            exception.printStackTrace();
            Logger.log(Logger.level.CRITICAL, "The webserver was unable to be created, aborting.");
        }
        httpServer.createContext("/", new HttpHandler());
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();
        Logger.log(Logger.level.NORMAL, "Server has started on port "+port);
    }
}

class HttpHandler implements com.sun.net.httpserver.HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) {
        String requestMethod = httpExchange.getRequestMethod();
        URI requestURI = httpExchange.getRequestURI();
        InetSocketAddress inetSocketAddress = httpExchange.getRemoteAddress();

        String ip = inetSocketAddress.getAddress().getHostAddress();
        if (ip.equals("0:0:0:0:0:0:0:1")) ip = "localhost";

        Logger.log(Logger.level.NORMAL, "["+ip+":"+inetSocketAddress.getPort()+"] "+requestMethod+" "+requestURI+" "+httpExchange.getProtocol());

        switch (requestMethod) {
            default:
                Logger.log(Logger.level.ERROR, "Don't know how to handle "+requestMethod);
                try {
                    sendBytes(httpExchange, "Unknow request method".getBytes(), 501);
                } catch (IOException exception) {
                    exception.printStackTrace();
                    Logger.log(Logger.level.ERROR, "Unable to send response");
                }
                break;
            case "GET":
                String mainSite = "<!doctype html><html><head><title>Cloud Storage</title></head><body><h1>"+requestURI+" %date% %time%</h1></body></html>";
                try {
                    String requestPath = requestURI.getPath();
                    if (requestPath.startsWith("/google")) {
                        httpExchange.getResponseHeaders().add("Location", "https://www.google.com");
                        sendBytes(httpExchange, "Redirecting to www.google.com".getBytes(), 302);
                    } else if (requestPath.startsWith("/file")) {
                        File requestedFile = new File(Main.workDir.getPath()+requestPath.substring(5).replace("/", File.separator));

                        if (requestedFile.exists()) {
                            sendBytes(httpExchange, Files.readAllBytes(requestedFile.toPath()), 200);
                        } else {
                            sendBytes(httpExchange, "<!doctype html><html><head><title>404 not found</title></head><body><h1>Error 404: File not found</h1></body></html>".getBytes(), 404);
                        }
                    } else if (requestPath.equalsIgnoreCase("/favicon.ico")) {
                        sendBytes(httpExchange, Files.readAllBytes(Paths.get(Main.workDir.toPath()+File.separator+"favicon.ico")), 200);
                    } else {
                        sendBytes(httpExchange, HtmlFormat.format(mainSite).getBytes(), 200);
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                    Logger.log(Logger.level.ERROR, "Unable to send response");
                }
                break;
            case "POST":
                break;
            case "LOGIN":
                Logger.log(Logger.level.DEBUG, "login");
                break;
        }
    }

    private void sendBytes(HttpExchange httpExchange, byte[] bytes, int code) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        httpExchange.sendResponseHeaders(code, bytes.length);
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
}
