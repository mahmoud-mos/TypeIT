package ui;

import client.Client;
import client.MessageListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FileMessage;
import model.Message;
import model.TextMessage;

import java.io.*;
import java.nio.file.Files;

public class ChatScreen implements MessageListener.MessageCallback {

    private final Stage stage;
    private final Client client;
    private VBox messageBox;
    private ScrollPane scrollPane;

    public ChatScreen(Stage stage, Client client) {
        this.stage = stage;
        this.client = client;
    }

    public void show() {
        // Header
        Text title = new Text("💬 TypeIT");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #ffffff;");

        Text userInfo = new Text("Logged in as: " + client.getUser().getUsername()
                + " [" + client.getUser().getRole() + "]");
        userInfo.setStyle("-fx-font-size: 12px; -fx-fill: #aaaaaa;");

        HBox header = new HBox(10, title, userInfo);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: #16213e; -fx-border-color: #333355; -fx-border-width: 0 0 1 0;");

        // Message area
        messageBox = new VBox(8);
        messageBox.setPadding(new Insets(12));
        messageBox.setStyle("-fx-background-color: #1e1e2e;");

        scrollPane = new ScrollPane(messageBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1e1e2e; -fx-background-color: #1e1e2e;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Input area
        TextField inputField = new TextField();
        inputField.setPromptText("Type a message...");
        inputField.setStyle(inputStyle());
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.setStyle(buttonStyle("#7c6af7"));

        Button fileBtn = new Button("📎");
        fileBtn.setStyle(buttonStyle("#444466"));
        fileBtn.setTooltip(new Tooltip("Send a file or image"));

        // Send text on button click or Enter key
        sendBtn.setOnAction(e -> sendText(inputField));
        inputField.setOnAction(e -> sendText(inputField));

        // File chooser for sending files
        fileBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a file to send");
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                client.sendFile(file);
                String name = file.getName().toLowerCase();
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                    try {
                        byte[] fileData = Files.readAllBytes(file.toPath());
                        FileMessage preview = new FileMessage("You", file.getName(), fileData);
                        addImage(preview, true);
                    } catch (IOException ex) {
                        addMessage("You sent: " + file.getName(), true, false, "");
                    }
                } else {
                    try {
                        byte[] fileData = Files.readAllBytes(file.toPath());
                        FileMessage preview = new FileMessage("You", file.getName(), fileData);
                        addFileMessage(preview, true);
                    } catch (IOException ex) {
                        addMessage("You sent: " + file.getName(), true, false, "");
                    }
                }
            }
        });

        HBox inputBar = new HBox(8, fileBtn, inputField, sendBtn);
        inputBar.setPadding(new Insets(10, 16, 10, 16));
        inputBar.setAlignment(Pos.CENTER);
        inputBar.setStyle("-fx-background-color: #16213e; -fx-border-color: #333355; -fx-border-width: 1 0 0 0;");

        // Main layout
        VBox layout = new VBox(header, scrollPane, inputBar);
        layout.setStyle("-fx-background-color: #1e1e2e;");

        Scene scene = new Scene(layout, 700, 500);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        // listening for incoming messages from the server
        client.startListening(this);

        // Clean up on window close
        stage.setOnCloseRequest(e -> client.disconnect());
    }

    private void sendText(TextField inputField) {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        client.sendTextMessage(text);
        addMessage(text, true, false, "");
        inputField.clear();
    }

    // Called by MessageListener when a message arrives from the server
    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            if (message instanceof TextMessage textMessage) {
                if (textMessage.getSenderUsername().equals("🤖 AI Assistant")) {
                    addAIMessage(textMessage.getContent());
                } else {
                    addMessage(textMessage.getContent(), false, false, textMessage.getSenderUsername());
                }
            } else if (message instanceof FileMessage fileMessage) {
                String name = fileMessage.getFileName().toLowerCase();
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                    addImage(fileMessage, false);
                } else {
                    addFileMessage(fileMessage, false);
                }
            }
        });
    }

    @Override
    public void onDisconnected() {
        Platform.runLater(() ->
                addMessage("⚠️ Disconnected from server.", false, true, "")
        );
    }

    // Adds a text bubble to the chat
    private void addMessage(String text, boolean isMine, boolean isSystem, String senderName) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(480);
        bubble.setPadding(new Insets(8, 12, 8, 12));

        if (isSystem) {
            bubble.setStyle("-fx-background-color: #333344; -fx-text-fill: #aaaaaa;" +
                    "-fx-background-radius: 10px; -fx-font-size: 12px;");
        } else if (isMine) {
            bubble.setStyle("-fx-background-color: #7c6af7; -fx-text-fill: #ffffff;" +
                    "-fx-background-radius: 10px; -fx-font-size: 13px;");
        } else {
            bubble.setStyle("-fx-background-color: #2e2e3e; -fx-text-fill: #ffffff;" +
                    "-fx-background-radius: 10px; -fx-font-size: 13px;");
        }

        VBox messageContainer;

        // Only show name label if not a system message
        if (!isSystem) {
            Label nameLabel = new Label((isMine ? "  You" : senderName) + "  " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
            nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");
            messageContainer = new VBox(3, nameLabel, bubble);
        } else {
            messageContainer = new VBox(3, bubble);
        }

        HBox row = new HBox(messageContainer);
        row.setPadding(new Insets(2, 8, 2, 8));
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageBox.getChildren().add(row);
        scrollPane.setVvalue(1.0);
    }

    // Adds an image preview inline in the chat with an Open button
    private void addImage(FileMessage message, boolean isMine) {
        try {
            Image image = new Image(new java.io.ByteArrayInputStream(message.getFileData()));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(250);
            imageView.setPreserveRatio(true);

            Label nameLabel = new Label(isMine ? "  You" : message.getSenderUsername());
            nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");

            Button openBtn = new Button("Open");
            openBtn.setStyle(buttonStyle("#444466"));
            openBtn.setOnAction(e -> {
                try {
                    File tempFile = File.createTempFile(
                            message.getFileName().replace(".", "_"),
                            "." + getExtension(message.getFileName())
                    );
                    Files.write(tempFile.toPath(), message.getFileData());
                    tempFile.deleteOnExit();
                    java.awt.Desktop.getDesktop().open(tempFile);
                } catch (IOException ex) {
                    System.out.println("[UI] Could not open file: " + ex.getMessage());
                }
            });

            VBox imageBox = new VBox(4, nameLabel, imageView, openBtn);
            imageBox.setStyle("-fx-background-color: #2e2e3e; -fx-background-radius: 10px; -fx-padding: 8px;");

            HBox row = new HBox(imageBox);
            row.setPadding(new Insets(2, 8, 2, 8));
            row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            messageBox.getChildren().add(row);
            scrollPane.setVvalue(1.0);

        } catch (Exception e) {
            addMessage(message.format(), false, false, message.getSenderUsername());
        }
    }

    // Shows a non-image file with an Open button
    private void addFileMessage(FileMessage message, boolean isMine) {
        Label nameLabel = new Label(isMine ? "You" : message.getSenderUsername());
        nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label fileLabel = new Label("📎 " + message.getFileName()
                + " (" + message.getFileData().length + " bytes)");
        fileLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");

        Button openBtn = new Button("Open");
        openBtn.setStyle(buttonStyle("#444466"));
        openBtn.setOnAction(e -> {
            try {
                File tempFile = File.createTempFile(
                        message.getFileName().replace(".", "_"),
                        "." + getExtension(message.getFileName())
                );
                Files.write(tempFile.toPath(), message.getFileData());
                tempFile.deleteOnExit();
                java.awt.Desktop.getDesktop().open(tempFile);
            } catch (IOException ex) {
                System.out.println("[UI] Could not open file: " + ex.getMessage());
            }
        });

        HBox fileBox = new HBox(10, fileLabel, openBtn);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.setPadding(new Insets(8, 12, 8, 12));
        fileBox.setStyle("-fx-background-color: #2e2e3e; -fx-background-radius: 10px;");

        VBox container = new VBox(3, nameLabel, fileBox);

        HBox row = new HBox(container);
        row.setPadding(new Insets(2, 8, 2, 8));
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageBox.getChildren().add(row);
        scrollPane.setVvalue(1.0);
    }

    // Extracts file extension "photo.png" -> "png"
    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "tmp";
    }

    private String inputStyle() {
        return "-fx-background-color: #2e2e3e;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-border-color: #555577;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 8px;" +
                "-fx-font-size: 13px;";
    }

    private String buttonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 8px 16px;" +
                "-fx-cursor: hand;";
    }

    // Special box for AI responses
    private void addAIMessage(String text) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(480);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-text-fill: #00ffcc;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: #7c6af7;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-font-size: 13px;"
        );

        Label aiLabel = new Label("🤖 AI Assistant");
        aiLabel.setStyle("-fx-text-fill: #7c6af7; -fx-font-size: 11px; -fx-font-weight: bold;");

        VBox messageContainer = new VBox(3, aiLabel, bubble);

        HBox row = new HBox(messageContainer);
        row.setPadding(new Insets(2, 8, 2, 8));
        row.setAlignment(Pos.CENTER_LEFT);

        messageBox.getChildren().add(row);
        scrollPane.setVvalue(1.0);
    }
}