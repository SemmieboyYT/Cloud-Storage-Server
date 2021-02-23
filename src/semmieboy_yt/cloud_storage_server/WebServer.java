package semmieboy_yt.cloud_storage_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class WebServer {
    HttpServer httpServer;
    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
    public WebServer(int port) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            //java.net.BindException
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
        String requestURI = httpExchange.getRequestURI().toString();
        InetSocketAddress inetSocketAddress = httpExchange.getRemoteAddress();
        Logger.log(Logger.level.NORMAL, "["+inetSocketAddress.getAddress().getHostAddress()+":"+inetSocketAddress.getPort()+"] "+requestMethod+" "+requestURI+": "+httpExchange.getProtocol());

        switch (requestMethod) {
            default:
                Logger.log(Logger.level.ERROR, "Don't know how to handle "+requestMethod);
                sendStringResponse(httpExchange, "Unknow request method", 501);
                break;
            case "GET":
                if (!sendStringResponse(httpExchange, HtmlFormat.format("<!doctype html><html><head><title>Cloud Storage</title></head><body><h1>"+requestURI+" %date% %time%</h1></body></html>"), 200)) Logger.log(Logger.level.ERROR, "Unable to send response.");
                break;
            case "POST":
                break;
            case "LOGIN":
                Logger.log(Logger.level.DEBUG, "login");
                break;
        }
    }

    private boolean sendStringResponse(HttpExchange httpExchange, String string, int code) {
        OutputStream outputStream = httpExchange.getResponseBody();
        try {
            httpExchange.sendResponseHeaders(code, string.length());
            outputStream.write(string.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
