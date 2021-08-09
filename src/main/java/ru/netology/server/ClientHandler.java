package ru.netology.server;

import org.apache.http.NameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.apache.http.client.utils.URLEncodedUtils.parse;

public class ClientHandler implements Runnable {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private final List<String> ALLOWED_METHODS = List.of(GET, POST);
    private final int LIMIT = 4096;
    private final byte[] REQUEST_LINE_DELIMITER = new byte[]{'\r', '\n'};
    private final byte[] HEADERS_DELIMITER = new byte[]{'\r', '\n', '\r', '\n'};
    private final Socket CLIENT_SOCKET;

    public ClientHandler(Socket clientSocket) {
        CLIENT_SOCKET = clientSocket;
    }

    @Override
    public void run() {
        try (CLIENT_SOCKET;
             var in = new BufferedInputStream(CLIENT_SOCKET.getInputStream());
             var out = new BufferedOutputStream(CLIENT_SOCKET.getOutputStream())) {
            in.mark(LIMIT);
            final byte[] BUFFER = new byte[LIMIT];
            final int READ = in.read(BUFFER);

            // ищем request line
            final int REQUEST_LINE_END = getIndex(BUFFER, REQUEST_LINE_DELIMITER, 0, READ);
            if (REQUEST_LINE_END == -1) badRequest(out);
            // читаем request line
            final String[] REQUEST_LINE = new String(Arrays.copyOf(BUFFER, REQUEST_LINE_END)).split(" ");
            if (REQUEST_LINE.length != 3) badRequest(out);

            final String METHOD = REQUEST_LINE[0];
            if (!ALLOWED_METHODS.contains(METHOD)) badRequest(out);
            System.out.println(METHOD);

            final String PATH = REQUEST_LINE[1];
            if (!PATH.startsWith("/")) badRequest(out);
            System.out.println(PATH);

            // ищем заголовки
            final int HEADERS_START = REQUEST_LINE_END + REQUEST_LINE_DELIMITER.length;
            final int HEADERS_END = getIndex(BUFFER, HEADERS_DELIMITER, HEADERS_START, READ);
            if (HEADERS_END == -1) badRequest(out);
            // отматываем на начало буфера
            in.reset();
            // пропускаем REQUEST_LINE
            in.skip(HEADERS_START);

            final var headersBytes = in.readNBytes(HEADERS_END - HEADERS_START);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println(headers);

            // для GET тела нет
            if (!METHOD.equals(GET)) {
                in.skip(HEADERS_DELIMITER.length);
                // вычитываем Content-Length, чтобы прочитать body
                var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    String body = extractContentLength(in, contentLength);
                    System.out.println(body);
                }
            }

            response(out, okMessage());

            List<NameValuePair> pairs = getQueryParams(REQUEST_LINE);
            pairs.forEach(System.out::println);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private List<NameValuePair> getQueryParams(String[] request) throws URISyntaxException {
        return parse(new URI(request[1]), StandardCharsets.UTF_8);
    }

    private String extractContentLength(BufferedInputStream in, Optional<String> contentLength) throws IOException {
        int length = Integer.parseInt(contentLength.get());
        byte[] bodyBytes = in.readNBytes(length);
        return new String(bodyBytes);
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    // from google guava with modifications
    private static int getIndex(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        response(out, errorMessage());
    }

    private static void response(BufferedOutputStream out, String massage) throws IOException {
        out.write((massage).getBytes());
        out.flush();
    }

    private String okMessage() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String errorMessage() {
        return "HTTP/1.1 400 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
}