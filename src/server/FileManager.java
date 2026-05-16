package server;

import model.FileMessage;
import model.TextMessage;

import java.io.*;
import java.nio.file.*;

// Handles all file and log operations on the server side
public class FileManager {

    private static final String LOG_FILE = "chatlog.txt";
    private static final String FILES_DIR = "received_files/";

    public FileManager() {
        // Create the received_files directory if it doesn't exist
        new File(FILES_DIR).mkdirs();
    }

    // Appends a text message to the chat log file (older files won't get deleted)
    public synchronized void saveTextMessage(TextMessage message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message.format());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("[FileManager] Failed to save message: " + e.getMessage());
        }
    }

    // Saves a received file's raw bytes to the received_files folder
    public synchronized void saveFile(FileMessage message) {
        String path = FILES_DIR + message.getFileName();
        try {
            Files.write(Paths.get(path), message.getFileData());
            System.out.println("[FileManager] Saved file: " + path);
        } catch (IOException e) {
            System.out.println("[FileManager] Failed to save file: " + e.getMessage());
        }
    }
}