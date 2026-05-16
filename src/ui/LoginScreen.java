package ui;

import client.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.AdminUser;
import model.RegularUser;
import model.User;

// The first -> enter username and pick a role
public class LoginScreen {

    private final Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Title
        Text title = new Text("💬 TypeIT");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: #ffffff;");

        Text subtitle = new Text("Connect and chat in real time");
        subtitle.setStyle("-fx-font-size: 13px; -fx-fill: #aaaaaa;");

        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username...");
        usernameField.setStyle(inputStyle());
        usernameField.setMaxWidth(300);

        // Role selector
        Label roleLabel = new Label("Role");
        roleLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        ToggleGroup roleGroup = new ToggleGroup();

        RadioButton userBtn = new RadioButton("Regular User");
        userBtn.setToggleGroup(roleGroup);
        userBtn.setSelected(true);
        userBtn.setStyle("-fx-text-fill: #ffffff;");

        RadioButton adminBtn = new RadioButton("Admin");
        adminBtn.setToggleGroup(roleGroup);
        adminBtn.setStyle("-fx-text-fill: #ffffff;");

        HBox roleBox = new HBox(20, userBtn, adminBtn);
        roleBox.setAlignment(Pos.CENTER);

        // Error label (hidden until needed)
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px;");

        // Connect button
        Button connectBtn = new Button("Connect");
        connectBtn.setStyle(buttonStyle());
        connectBtn.setMaxWidth(300);

        connectBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                errorLabel.setText("Please enter a username.");
                return;
            }

            // Build the correct user type based on role selection
            User user = adminBtn.isSelected()
                    ? new AdminUser(username)
                    : new RegularUser(username);

            // Try to connect to the server
            Client client = new Client();
            boolean connected = client.connect(user);

            if (!connected) {
                errorLabel.setText("Could not connect. Is the server running?");
                return;
            }

            // If connected, move to the chat screen
            ChatScreen chatScreen = new ChatScreen(stage, client);
            chatScreen.show();
        });

        // Layout
        VBox layout = new VBox(15,
                title,
                subtitle,
                new Separator(),
                usernameLabel,
                usernameField,
                roleLabel,
                roleBox,
                errorLabel,
                connectBtn
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #1e1e2e;");

        Scene scene = new Scene(layout, 420, 400);
        stage.setScene(scene);
        stage.show();
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

    private String buttonStyle() {
        return "-fx-background-color: #7c6af7;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;";
    }
}