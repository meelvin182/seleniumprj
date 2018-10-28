package ru.sokolov.gui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyPopup {
    private static final String ENRET_KEY = "Введите ключ";
    private static final String KEY = "Ключ доступа";
    private static final String SAVE = "Сохранить";

    public static List<TextField> fields = Stream.generate(KeyTextField::new).limit(5).collect(Collectors.toList());
    private List<String> fieldLenghts = Arrays.stream("6F9619FF-8B86-D011-B42D-00CF4FC964FF".split("-"))
            .collect(Collectors.toList());

    public KeyPopup(Stage parent) {
        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle(ENRET_KEY);
        stage.initOwner(parent);

        VBox vbox = new VBox();
        vbox.setSpacing(5);//Set vbox spacing
        vbox.setAlignment(Pos.CENTER_LEFT);

        HBox key = new HBox();
        Text keyText = new Text(KEY);
        key.getChildren().add(keyText);
        fields.forEach(textField -> {
            int index = fields.indexOf(textField);
            textField.setPromptText(fieldLenghts.get(index));
            textField.setOnKeyTyped(event -> {
                int maxLength = fieldLenghts.get(index).length();
                if (textField.getText().length() == maxLength) {
                    if (index < fields.size() - 1) {
                        event.consume();
                        fields.get(index + 1).requestFocus();
                    } else {
                        event.consume();
                    }
                }
            });
            ObservableList<Node> chld = key.getChildren();
            chld.add(textField);
            chld.add(new Text("-"));
        });
        key.getChildren().remove(key.getChildren().size() - 1);
        key.setSpacing(10);
        key.setPrefWidth(200);

        Button saveButton = new Button();
        saveButton.setText(SAVE);
        saveButton.setOnAction(event -> stage.close());

        vbox.getChildren().addAll(key, saveButton);
        layout.getChildren().add(vbox);

        Scene scene = new Scene(layout, 900, 450);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                stage.close();
            }
        });
        stage.setScene(scene);
        stage.show();

    }
}
