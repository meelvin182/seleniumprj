package ru.sokolov.gui;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.gui.utils.TableItemsManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyPopup {
    private static final String ENRET_KEY = "Введите ключ";
    private static final String KEY = "Ключ доступа";
    private static final String SAVE = "Сохранить";
    private static final String CLEAN = "Очистить";

    private static final TableItemsManager itemsManager = TableItemsManager.getInstance();

    public static List<TextField> fields = Stream.generate(KeyTextField::new).limit(5).collect(Collectors.toList());
    private List<String> fieldLenghts = Arrays.stream("6F9619FF-8B86-D011-B42D-00CF4FC964FF".split("-"))
            .collect(Collectors.toList());

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyPopup.class);

    public KeyPopup(Stage parent) {
        CoreKernelSupaClazz.loadKey(fields);
        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle(ENRET_KEY);
        stage.initOwner(parent);

        VBox vbox = new VBox();
        vbox.setSpacing(5);//Set vbox spacing
        vbox.setAlignment(Pos.CENTER);

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

        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);

        Button saveButton = new Button();
        saveButton.setText(SAVE);
        saveButton.setOnAction(event -> {
            itemsManager.loadItems(fields);
            itemsManager.refreshItems();
            stage.close();
        });
        saveButton.setPadding(new Insets(16));

        Button clearButton = new Button();
        clearButton.setText(CLEAN);
        clearButton.setOnAction(event -> {
            fields.forEach(t -> t.setText(null));
        });
        clearButton.setPadding(new Insets(16));

        buttons.getChildren().addAll(saveButton, clearButton);

        vbox.getChildren().addAll(key, buttons);
        layout.getChildren().add(vbox);

        Scene scene = new Scene(layout, 900, 450);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                stage.close();
            }
        });
        stage.setOnCloseRequest(event -> {
                    itemsManager.loadItems(fields);
                    itemsManager.refreshItems();
                });
        stage.setScene(scene);
        stage.show();

    }
}
