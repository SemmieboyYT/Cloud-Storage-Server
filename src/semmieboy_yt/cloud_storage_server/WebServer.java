package semmieboy_yt.cloud_storage_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
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

    public WebServer(int port) {
        start(port);
    }

    public void start(int port) {
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
                    sendBytes(httpExchange, "Unknown request method".getBytes(), 501);
                } catch (IOException exception) {
                    exception.printStackTrace();
                    Logger.log(Logger.level.ERROR, "Unable to send response");
                }
                break;
            case "GET":
                try {
                    String requestPath = requestURI.getPath();
                    if (!requestPath.endsWith("/")) {
                        httpExchange.getResponseHeaders().add("Location", requestPath+"/");
                        sendBytes(httpExchange, "The URL must end with a /, please make a request with it".getBytes(), 302);
                        return;
                    }
                    String[] args = requestPath.substring(1, requestPath.length() - 1).split("/");

                    switch (args[0]) {
                        default:
                            sendBytes(httpExchange, HtmlFormat.format("<!DOCTYPE html><html><head><title>Cloud Storage</title></head><body><h1>"+requestURI+" %date% %time%</h1></body></html>").getBytes(), 200);
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
                        case "files":
                            File requestedFile = new File(Main.workDir.getPath()+File.separator+requestPath.replace("/"+args[0], ""));

                            if (requestedFile.isFile()) {
                                FileInputStream fileInputStream = new FileInputStream(requestedFile);
                                sendData(httpExchange, fileInputStream, 200);
                                fileInputStream.close();
                            } else if (requestedFile.isDirectory()) {
                                String fileName = requestedFile.getName();
                                StringBuilder response = new StringBuilder("<!DOCTYPE html><html><head><title>Directory listing for "+fileName+"</title></head><body><h1>"+fileName+"</h1><hr>");

                                for (File file:requestedFile.listFiles()) {
                                    String name = file.getName();
                                    if (file.isDirectory()) name += "/";
                                    response.append("<a href=\"").append(name).append("\">").append(name).append("</a><br><br>");
                                }
                                response.append("</body></html>");

                                sendBytes(httpExchange, response.toString().getBytes(), 200);
                            } else {
                                sendBytes(httpExchange, "<!DOCTYPE html><html><head><title>404 not found</title></head><body><h1>Error 404: File not found</h1></body></html>".getBytes(), 404);
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
        //TODO: use sendData if more then buffer size
    }

    private void sendData(HttpExchange httpExchange, InputStream inputStream, int status) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        int length = inputStream.available();
        int bufferSize = Main.bufferSize;

        if (length > bufferSize) {
            int read = 0;
            byte[] size = Integer.toHexString(bufferSize).getBytes();
            byte[] terminator = new byte[] {13, 10};
            //TODO: calculate the size more precise
            long bytesSend = (long)Math.ceil(length/(double)bufferSize)*(bufferSize+4+size.length)+5;
            ByteBuffer byteBuffer = ByteBuffer.allocate(size.length + 4 + bufferSize);

            Logger.log(Logger.level.DEBUG, "Calculated amount of bytes that will get send: "+bytesSend);

            httpExchange.getResponseHeaders().add("Transfer-Encoding", "chunked");
            httpExchange.sendResponseHeaders(status, bytesSend);

            while (read < length) {
                byte[] bytes = new byte[bufferSize];

                int result = 0;
                while (result < bufferSize) {
                    result = inputStream.read(bytes, 0, bufferSize);
                    if (result == -1) break;
                }
                if (result == -1) break;
                if (result != bufferSize) {
                    Logger.log(Logger.level.ERROR, "Expected "+bufferSize+", got "+result);
                    return;
                    //TODO: make the client know why data transfer has cancelled
                }

                byteBuffer.put(size);
                byteBuffer.put(terminator);
                byteBuffer.put(bytes);
                byteBuffer.put(terminator);

                read += result;
                outputStream.write(byteBuffer.array());
                byteBuffer.clear();

                /*if (length-read-byteSize < byteSize) {
                    byteSize = length-read;
                    Logger.log(Logger.level.DEBUG, "set");
                }*/
            }
            Logger.log(Logger.level.DEBUG, "Sending terminator chunk");
            //Terminator chunk (0\r\n\r\n)
            outputStream.write(new byte[] {48, 13, 10, 13, 10});
            Logger.log(Logger.level.DEBUG, "Done");
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
