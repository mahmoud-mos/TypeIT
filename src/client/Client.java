package client;

import model.*;

import java.io.*;
import java.net.*;

// Handles all networking on the client side
// The UI talks to this class not to sockets
public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private Socket socket;
    private ObjectOutputStream out; //to send full Java objects
    private ObjectInputStream in; //to receive full Java objects
    private User user;

    // Connects to the server and sends the user object to identify each one
    public boolean connect(User user) {
        this.user = user;
        try {
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // handshake between User and Server
            out.writeObject(user);
            out.flush();

            System.out.println("[Client] Connected as " + user.getUsername());
            return true;

        } catch (IOException e) {
            System.out.println("[Client] Could not connect to server: " + e.getMessage());
            return false;
        }
    }

    // Starts the background listener thread
    public void startListening(MessageListener.MessageCallback callback) {
        MessageListener listener = new MessageListener(in, callback);
        new Thread(listener).start();
    }

    // Sends a text message to the server
    public void sendTextMessage(String content) {
        TextMessage message = new TextMessage(user.getUsername(), content);
        sendMessage(message);
    }

    // Reads a file from disk and sends it as a FileMessage
    public void sendFile(File file) {
        try {
            byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
            FileMessage message = new FileMessage(user.getUsername(), file.getName(), fileData);
            sendMessage(message);
        } catch (IOException e) {
            System.out.println("[Client] Failed to read file: " + e.getMessage());
        }
    }

    // Internal method to serializes any Message object and sends it over the socket
    private void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("[Client] Failed to send message: " + e.getMessage());
        }
    }

    // Clean disconnection
    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("[Client] Error disconnecting.");
        }
    }

    public User getUser() {
        return user;
    }
}