package ru.sokolov.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class MainScreen extends Application {

    private static int width = 1920 / 2;
    private static int height = 1080 / 2;

    @Override
    public void start(Stage primaryStage) {
        StackPane layout = new StackPane();
        Button testButton = new TestButton(primaryStage);
        layout.getChildren().add(testButton);
        Scene scene = new Scene(layout, width, height);

        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                System.exit(0);
            }
        });

        primaryStage.setTitle("slnmprj");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
