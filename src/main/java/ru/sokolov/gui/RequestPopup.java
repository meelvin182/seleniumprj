package ru.sokolov.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.exceptions.CouldntLoginException;
import ru.sokolov.model.exceptions.WrongCadastreNumException;
import ru.sokolov.model.pages.AbstractPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.sokolov.CoreKernelSupaClazz.checkrequestsLock;
import static ru.sokolov.CoreKernelSupaClazz.sendRequest;

public class RequestPopup {

    private static final String ENTER_REQUEST_PARAMS = "Введите данные запроса";
    private static final String KEY = "Ключ доступа";
    private static final String CADASTRE_NUM = "Кадастровый номер";
    private static final String CHOOSE_REGION = "Выбрать регион";
    private static final String CHOOSE_FOLDER_PROMPT = "Папка загрузки";
    private static final String FOLDER_SELECTOR_WINDOW_NAME = "Request save folder";
    private static final String RADIO_BUTTON_ONE = "Запросить сведения об объекте";
    private static final String RADIO_BUTTON_TWO = "Запросить сведения о переходе прав на объект";
    private static final String COULDNT_LOGIN = "Проверьте ключ";
    private static final String SENDING = "Отправляется";
    private static final String SEND = "Отправить";

    public static List<TextField> fields = Stream.generate(KeyTextField::new).limit(5).collect(Collectors.toList());
    private List<String> fieldLenghts = Arrays.stream("6F9619FF-8B86-D011-B42D-00CF4FC964FF".split("-"))
            .collect(Collectors.toList());
    public static List<String> ruzkeRegions = loadRegions();

    public RequestPopup(Stage parent) {

        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle(ENTER_REQUEST_PARAMS);
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

        TextField nums = new TextField();
        nums.setPromptText(CADASTRE_NUM);

        ComboBox box = new ComboBox();
        box.getItems().addAll(ruzkeRegions);
        box.setPromptText(CHOOSE_REGION);

        HBox path = new HBox();
        path.setSpacing(10);
        path.setPrefWidth(200);

        Button button = new Button();
        button.setText("...");
        TextField pathField = new TextField();
        pathField.setPromptText(CHOOSE_FOLDER_PROMPT);
        button.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(FOLDER_SELECTOR_WINDOW_NAME);
            File selectedDirectory = chooser.showDialog(stage);
            pathField.setText(selectedDirectory == null ? null : selectedDirectory.getAbsolutePath());
        });
        path.getChildren().add(pathField);
        path.getChildren().add(button);

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton one = new RadioButton(RADIO_BUTTON_ONE);
        RadioButton two = new RadioButton(RADIO_BUTTON_TWO);
        one.setToggleGroup(toggleGroup);
        two.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(one);

        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Превышено время ожидания, попробуйте позже");
        errorAlert.setTitle("Notification");

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setHeaderText("Запрос отправлен");
        successAlert.setTitle("Notification");



        HBox bottom = new HBox();
        Button sendButton = new Button();
        sendButton.setOnAction((ActionEvent event) -> {
            RequestEntity entity = new RequestEntity(
                    fields.stream().map(s -> s == null ? null : s.getText()).collect(Collectors.toList()),
                    nums == null ? null : nums.getText(),
                    box.getValue() == null ? null : box.getValue().toString(),
                    pathField.getText(),
                    one.isSelected(),
                    two.isSelected());
            boolean incorrectInput = false;
            fields.forEach(t -> t.setStyle(null));
            nums.setStyle(null);
            box.setStyle(null);
            keyText.setText(KEY);

            if (box.getValue() == null) {
                box.setStyle("-fx-background-color: #ff736e;");
                incorrectInput = true;
            }
            for (TextField field : fields) {
                if (field.getText().isEmpty() || !(field.getPromptText().length() == field.getText().length())) {
                    field.setStyle("-fx-background-color: #ff736e;");
                    incorrectInput = true;
                }
            }
            if (nums.getText().isEmpty()) {
                nums.setStyle("-fx-background-color: #ff736e;");
                incorrectInput = true;
            }
            if (incorrectInput) {
                return;
            }
            sendButton.setText(SENDING);
            sendButton.setDisable(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MainScreen.table.getItems().add(sendRequest(entity));
                        Platform.runLater(() -> successAlert.showAndWait());
                    } catch (Exception e) {
                        AbstractPage.driver.close();
                        checkrequestsLock.unlock();
                        if (e instanceof CouldntLoginException) {
                            fields.forEach(textField -> textField.setStyle("-fx-text-fill: red;"));
                            Platform.runLater(() -> keyText.setText(COULDNT_LOGIN));
                        } else if (e instanceof WrongCadastreNumException) {
                            nums.setStyle("-fx-text-fill: red;");
                        } else {
                            Platform.runLater(() -> errorAlert.showAndWait());
                        }
                        e.printStackTrace(System.out);
                    } finally {
                        Platform.runLater(() -> {
                            sendButton.setText(SEND);
                            sendButton.setDisable(false);
                        });
                    }
                }
            }).start();
        });

        sendButton.setText(SEND);
        bottom.getChildren().addAll(sendButton);
        bottom.setAlignment(Pos.BOTTOM_CENTER);

        vbox.getChildren().addAll(key, nums, box, path, one, two, bottom);
        layout.getChildren().add(vbox);

        Scene scene = new Scene(layout, 450, 450);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                System.exit(0);
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    private static List<String> loadRegions() {
        List<String> values = new ArrayList<>();
        try {
            InputStream in = RequestPopup.class.getResourceAsStream("/regions.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()) {
                values.add(scanner.nextLine());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return values;
    }
}
