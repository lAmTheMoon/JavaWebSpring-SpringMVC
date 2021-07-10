package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ClientHandler implements Runnable {

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final Socket CLIENT_SOCKET;

    public ClientHandler(Socket CLIENT_SOCKET) {
        this.CLIENT_SOCKET = CLIENT_SOCKET;
    }

    @Override
    public void run() {
        try (CLIENT_SOCKET;
             var in = new BufferedReader(new InputStreamReader(CLIENT_SOCKET.getInputStream()));
             var out = new BufferedOutputStream(CLIENT_SOCKET.getOutputStream())) {
            while (true) {
                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");
                if (parts.length != 3) {
                    continue;
                }
                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    out.write(errorMessage().getBytes());
                    out.flush();
                    continue;
                }
                response(out, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void response(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);
        if (path.equals("/classic.html")) {
            responseToClassicHTML(out, filePath, mimeType);
            return;
        }
        responseUsual(out, filePath, mimeType);
    }

    private void responseUsual(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        out.write((okMessage(mimeType, length)).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    private void responseToClassicHTML(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
        out.write(okMessage(mimeType, content.length).getBytes());
        out.write(content);
        out.flush();
    }

    private String okMessage(String mimeType, long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String errorMessage() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
}
