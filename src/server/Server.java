package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final int PORT = 5000;
    private static final List<ClientHandler> connectedClients =
            Collections.synchronizedList(new ArrayList<>());
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        System.out.println("[Server] Starting on port " + PORT + " ");
        System.out.println("[Server] Type 'quit' to stop the server.");

        // Separate thread listens for the quit command in the console
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("quit")) {
                    shutdown();
                    break;
                }
            }
        }).start();

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[Server] Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New connection from: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, connectedClients);
                connectedClients.add(handler);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            // This triggers when shutdown() closes the serverSocket which is expected
            System.out.println("[Server] Server stopped.");
        }
    }

    // Closes all client connections then shuts down the server socket
    private static void shutdown() {
        System.out.println("[Server] Shutting down...");
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                client.sendMessage(new model.TextMessage("SERVER", "Server is shutting down."));
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("[Server] Error during shutdown.");
        }
    }
}