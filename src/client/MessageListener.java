package client;

import model.Message;

import java.io.*;
import java.net.*;

// Runs on a background thread so the UI thread doesn't freeze
// constantly listens for incoming messages from the server
public class MessageListener implements Runnable {

    private final ObjectInputStream in;
    private final MessageCallback callback;

    // a callback interface so the UI can react when a message arrives
    public MessageListener(ObjectInputStream in, MessageCallback callback) {
        this.in = in;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                callback.onMessageReceived(message);
            }
        } catch (EOFException | SocketException e) {
            // Server disconnected
            System.out.println("[Client] Disconnected from server.");
            callback.onDisconnected();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Client] Error receiving message: " + e.getMessage());
        }
    }

    // Simple interface — the UI implements this to know when something arrives
    public interface MessageCallback {
        void onMessageReceived(Message message);
        void onDisconnected();
    }
}