package semmieboy_yt.cloud_storage_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
        Logger.log(Logger.level.NORMAL, "Type \"help\" or \"?\" for help");
        new CommandProcessor(System.in).start();
    }

    public void stop() {
        httpServer.stop(0);
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
                                sendFile(httpExchange, requestedFile, 200);
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

    private void sendFile(HttpExchange httpExchange, File file, int status) throws IOException {
        long length = file.length();
        OutputStream outputStream = httpExchange.getResponseBody();

        if (length > 1024) {
            int offset = 0;
            InputStream fileInputStream = new FileInputStream(file);

            httpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
            httpExchange.sendResponseHeaders(status, length);
            int i = 0;

            while (offset < length) {
                i++;
                Logger.log(Logger.level.DEBUG, "At chunk "+i+" "+length);
                byte[] bytes = new byte[1024];

                ByteBuffer byteBuffer1 = ByteBuffer.wrap(Integer.toHexString(bytes.length).getBytes());
                byteBuffer1.put(new byte[] {13, 10});
                byteBuffer1.put(bytes);
                byteBuffer1.put(new byte[] {13, 10});

                Logger.log(Logger.level.DEBUG, new String(byteBuffer1.array()));

                int result = fileInputStream.read(bytes, offset, bytes.length+offset);
                if (result == -1) break;

                ByteBuffer byteBuffer = ByteBuffer.wrap(Integer.toHexString(bytes.length).getBytes());
                byteBuffer.put(new byte[] {13, 10});
                byteBuffer.put(bytes);
                byteBuffer.put(new byte[] {13, 10});

                Logger.log(Logger.level.DEBUG, new String(byteBuffer.array()));

                outputStream.write(byteBuffer.array());
                offset += result;
            }
            Logger.log(Logger.level.DEBUG, "Sending terminator chunk");
            //Terminator chunk (0\r\n\r\n)
            outputStream.write(new byte[] {48, 13, 10, 13, 10});
        } else {
            httpExchange.sendResponseHeaders(status, length);
            outputStream.write(Files.readAllBytes(file.toPath()));
        }
    }
}
