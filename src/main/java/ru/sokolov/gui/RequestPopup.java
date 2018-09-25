package ru.sokolov.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.awt.*;

public class RequestPopup {

    private static int width = 600;
    private static int height = 600;

    public RequestPopup(Stage parent) {
        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle("ENTER REQUEST SHIED PLEZ");
        stage.initOwner(parent);
        Scene scene = new Scene(layout, 450, 450);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(KeyCode.ESCAPE.equals(event.getCode())){
                    System.exit(0);
                }
            }
        });
        stage.setScene(scene);
        stage.show();

    }
}
