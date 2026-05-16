package ui;

import javafx.application.Application;
import javafx.stage.Stage;

// The JavaFX application class sets up the window and shows the login screen first
public class ChatApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("💬 TypeIT ");
        primaryStage.setResizable(false);

        // Start at the login screen
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        loginScreen.show();
    }
}