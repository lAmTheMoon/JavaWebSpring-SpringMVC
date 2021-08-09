package ru.netology.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int PORT;
    private final ExecutorService POOL;

    public Server(int port, int poolSize) {
        this.PORT = port;
        this.POOL = Executors.newFixedThreadPool(poolSize);
    }

    public void start() {
        try (final var SERVER_SOCKET = new ServerSocket(PORT)) {
            while (true) {
                final var CLIENT_SOCKET = SERVER_SOCKET.accept();
                ClientHandler clientHandler = new ClientHandler(CLIENT_SOCKET);
                POOL.execute(clientHandler);
            }
        } catch (IOException e) {
            POOL.shutdown();
            e.printStackTrace();
        }
    }
}