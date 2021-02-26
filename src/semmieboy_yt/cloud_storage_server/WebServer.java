package semmieboy_yt.cloud_storage_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class WebServer {
    public boolean running = false;
    public boolean lockdown = false;
    private HttpServer httpServer;
    private final int port;

    public WebServer(int port) {
        this.port = port;
        start();
    }

    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException exception) {
            Logger.log(Logger.level.DEBUG, exception.getMessage());
            if (exception.getMessage().equals("Address already in use: bind")) {
                Logger.log(Logger.level.CRITICAL, "Port "+port+" is already in use");
                return;
            }
            exception.printStackTrace();
            Logger.log(Logger.level.CRITICAL, "The webserver was unable to be created");
            return;
        }
        httpServer.createContext("/", new HttpHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(10));

        httpServer.start();
        running = true;
    }

    public void stop() {
        httpServer.stop(Integer.MAX_VALUE);
        running = false;
    }
}

class HttpHandler implements com.sun.net.httpserver.HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) {
        if (Main.webServer.lockdown) return;
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
                String requestPath = requestURI.getPath();
                String[] args = requestPath.substring(1).split("/");

                try {
                    switch (args[0]) {
                        default:
                            sendBytes(httpExchange, HtmlFormat.format("<!doctype html><html><head><title>Cloud Storage</title></head><body><h1>"+requestURI+" %date% %time%</h1></body></html>").getBytes(), 200);
                            break;
                        case "favicon":
                            File favicon = new File(Main.workDir.getPath()+File.separator+"/favicon.ico");
                            if (favicon.exists()) {
                                sendBytes(httpExchange, Files.readAllBytes(Paths.get(Main.workDir.toPath()+File.separator+"favicon.ico")), 200);
                            } else {
                                sendBytes(httpExchange, "Not found".getBytes(), 404);
                            }
                            break;
                        case "site":
                            if (args.length > 1) {
                                httpExchange.getResponseHeaders().add("Location", args[1]);
                                sendBytes(httpExchange, ("Redirecting to "+args[1]).getBytes(), 302);
                            } else {
                                sendBytes(httpExchange, "Please enter a site, example: /site/google.com".getBytes(), 200);
                            }
                            break;
                        case "file":
                            File requestedFile = new File(Main.workDir.getPath()+File.separator+requestPath.replace("/"+args[0], ""));

                            if (requestedFile.isFile()) {
                                sendData(httpExchange, new FileInputStream(requestedFile), 200);
                            } else {
                                sendBytes(httpExchange, "<!doctype html><html><head><title>404 not found</title></head><body><h1>Error 404: File not found</h1></body></html>".getBytes(), 404);
                            }
                            break;
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

    private void sendBytes(HttpExchange httpExchange, byte[] bytes, int status) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        httpExchange.sendResponseHeaders(status, bytes.length);
        outputStream.write(bytes);
        outputStream.close();
    }

    private void sendData(HttpExchange httpExchange, InputStream inputStream, int status) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        int length = inputStream.available();
        int byteSize = 1024;

        if (length > byteSize) {
            int read = 0;
            byte[] size = Integer.toHexString(byteSize).getBytes();
            byte[] terminator = new byte[] {13, 10};
            long bytesSend = (long)Math.ceil(length/(double)byteSize)*(byteSize+4+size.length)+5;

            Logger.log(Logger.level.DEBUG, "Calculated amount of bytes that will get send: "+bytesSend);

            httpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
            httpExchange.sendResponseHeaders(status, bytesSend);

            int check = 0;

            while (read < length) {
                byte[] bytes = new byte[byteSize];

                int result = 0;
                while (result < byteSize) {
                    result = inputStream.read(bytes, 0, byteSize);
                    if (result == -1) break;
                }
                if (result == -1) break;
                if (result != byteSize) {
                    Logger.log(Logger.level.ERROR, "Expected "+byteSize+", got "+result);
                    return;
                    //TODO: make the client know why data transfer has cancelled
                }

                ByteBuffer byteBuffer = ByteBuffer.allocate(size.length + 4 + byteSize);
                byteBuffer.put(size);
                byteBuffer.put(terminator);
                byteBuffer.put(bytes);
                byteBuffer.put(terminator);


                read += result;
                check += byteBuffer.array().length;
                outputStream.write(byteBuffer.array());

                /*if (length-read-byteSize < byteSize) {
                    byteSize = length-read;
                    Logger.log(Logger.level.DEBUG, "set");
                }*/
            }
            Logger.log(Logger.level.DEBUG, "Sending terminator chunk");
            //Terminator chunk (0\r\n\r\n)
            outputStream.write(new byte[] {48, 13, 10, 13, 10});
            Logger.log(Logger.level.DEBUG, "Done");

            Logger.log(Logger.level.DEBUG, "Check: "+(bytesSend-(check+5)));
        } else {
            httpExchange.sendResponseHeaders(status, length);
            byte[] bytes = new byte[length];
            int offset = 0;

            while (offset < length) {
                int result = inputStream.read(bytes, offset, length);
                offset += result;
            }
            outputStream.write(bytes);
        }
    }
}
