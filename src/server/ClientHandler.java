package server;

import model.*;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final List<ClientHandler> allClients; //list of everyone connected
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;

    public ClientHandler(Socket socket, List<ClientHandler> allClients) {
        this.socket = socket;
        this.allClients = allClients;
    }

    @Override
    public void run() {
        try {
            // Set up streams for sending/receiving serialized objects
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            //client sends in their User object (with username + role)
            user = (User) in.readObject();
            System.out.println("[Server] " + user.getUsername() + " connected as " + user.getRole());

            //listening for messages until client disconnects
            while (true) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }

        } catch (EOFException | SocketException e) {
            // Client disconnected cleanly
            System.out.println("[Server] " + (user != null ? user.getUsername() : "Unknown") + " disconnected.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Server] Error with client: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    // Decides what to do with each received message
    private void handleMessage(Message message) {
        FileManager fileManager = new FileManager();

        if (message instanceof TextMessage textMessage) {
            fileManager.saveTextMessage(textMessage);

            // Check if the message is an AI command
            if (textMessage.getContent().startsWith("/ai ")) {
                // Extract the question after "/ai "
                String question = textMessage.getContent().substring(4).trim();
                broadcast(textMessage);

                // Run AI call on a separate thread so it doesn't block the chat
                new Thread(() -> {
                    AIHandler ai = new AIHandler();
                    String answer = ai.ask(question);

                    // Send the response back as a message from "AI Assistant"
                    TextMessage aiResponse = new TextMessage("🤖 AI Assistant", answer);
                    broadcast(aiResponse);
                    sendMessage(aiResponse);
                }).start();

            } else {
                broadcast(textMessage);
            }

        } else if (message instanceof FileMessage fileMessage) {
            fileManager.saveFile(fileMessage);
            broadcast(fileMessage);
        }
    }

    // Sends a message to every connected client except the sender
    public void broadcast(Message message) {
        synchronized (allClients) {
            for (ClientHandler client : allClients) {
                if (client != this) {
                    client.sendMessage(message);
                }
            }
        }
    }

    // Sends a message to this specific client
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("[Server] Failed to send to " + user.getUsername());
        }
    }

    // Clean up when client disconnects
    private void disconnect() {
        synchronized (allClients) {
            boolean remove = allClients.remove(this);
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("[Server] Error closing socket.");
        }
    }

    public User getUser() {
        return user;
    }
}