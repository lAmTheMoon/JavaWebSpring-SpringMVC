package ru.netology;

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
        try (final var serverSocket = new ServerSocket(PORT)) {
            while (true) {
                final var clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                POOL.execute(clientHandler);
            }
        } catch (IOException e) {
            POOL.shutdown();
            e.printStackTrace();
        }
    }
}