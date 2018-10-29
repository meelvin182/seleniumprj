package ru.sokolov.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.exceptions.CouldntLoginException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static ru.sokolov.CoreKernelSupaClazz.closeDriver;

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

    public static Map<Integer, String> regions = loadRegions();
    public static Collection<String> ruzkeRegions = regions.values();

    public RequestPopup(Stage parent) {
        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle(ENTER_REQUEST_PARAMS);
        stage.initOwner(parent);
        VBox vbox = new VBox();
        vbox.setSpacing(5);//Set vbox spacing
        vbox.setAlignment(Pos.CENTER_LEFT);

//        ComboBox box = new ComboBox();
//        box.getItems().addAll(ruzkeRegions);
//        box.setPromptText(CHOOSE_REGION);

        TextArea nums = new TextArea();
        nums.setPromptText(CADASTRE_NUM);
        nums.setWrapText(true);
        nums.setPrefHeight(50);
//        nums.setOnKeyTyped(event -> {
//            System.out.println(event.getCharacter());
//            String num = nums.getText();
//            String region;
//            if(num.length()<1){
//                return;
//            } else if(num.length() == 1){
//                region = num + event.getCharacter();
//            } else {
//                region = num.substring(0, 2);
//            }
//            System.out.println(region);
//            if(StringUtils.isNumeric(region)){
//                System.out.println("ACTION TRIGGERED");
//                box.setValue(regions.get(Integer.parseInt(region)));
//            }
//        });

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

        Alert partlySucces = new Alert(Alert.AlertType.INFORMATION);
        partlySucces.setTitle("Notification");

        Alert incorrectKey = new Alert(Alert.AlertType.ERROR);
        incorrectKey.setHeaderText(COULDNT_LOGIN);
        incorrectKey.setTitle("Notification");

        HBox bottom = new HBox();
        Button sendButton = new Button();
        sendButton.setOnAction((ActionEvent event) -> {
            boolean incorrectInput = false;
            nums.setStyle(null);
            System.out.println(regions.get(50));
//            box.setStyle(null);
//
//            if (box.getValue() == null) {
//                box.setStyle("-fx-background-color: #ff736e;");
//                incorrectInput = true;
//            }

            if (nums.getText().isEmpty()) {
                nums.setStyle("-fx-background-color: #ff736e;");
                incorrectInput = true;
            }
            if (incorrectInput) {
                return;
            }

            String enteredNums = nums.getText().replaceAll("\n", "");
            String[] allNums = enteredNums.contains(";") ? enteredNums.split(";") : new String[]{enteredNums};
            List<RequestEntity> entities = new ArrayList<>();
            for(String num : allNums){
                if(!StringUtils.isEmpty(num) && num.length()>=2 && StringUtils.isNumeric(num.substring(0,2))){
                    RequestEntity entity =  new RequestEntity(
                            KeyPopup.fields.stream().map(s -> s == null ? null : s.getText()).collect(Collectors.toList()),
                            num,
                            regions.get(Integer.parseInt(num.substring(0,2))),
                            pathField.getText(),
                            one.isSelected(),
                            two.isSelected());
                    entities.add(entity);
                }
            }

            System.out.println("Entities formed, size: " + entities.size());


            sendButton.setText(SENDING);
            sendButton.setDisable(true);

            new Thread(() -> {
                List<RequestEntity> wrongCadastreNums = new ArrayList<>();
                List<SentRequest> success = new ArrayList<>();
                try {
                    List<List<LoginEntity>> sent = CoreKernelSupaClazz.sendRequests(entities);
                    wrongCadastreNums.addAll(sent.get(0).stream().map(t -> (RequestEntity) t).collect(Collectors.toList()));
                    success.addAll(sent.get(1).stream().map(t -> (SentRequest) t).collect(Collectors.toList()));
                } catch (CouldntLoginException e){
                    Platform.runLater(incorrectKey::showAndWait);
                    closeDriver();
                    Platform.runLater(() -> {
                        sendButton.setText(SEND);
                        sendButton.setDisable(false);
                    });
                    return;
                } catch (Exception e){
                    e.printStackTrace(System.out);
                    Platform.runLater(errorAlert::showAndWait);
                } finally {
                    closeDriver();
                }
                if(!success.isEmpty()){
                    MainScreen.table.getItems().addAll(success);
                    MainScreen.table.refresh();
                }
                if (!wrongCadastreNums.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Ничего не найдена для запросов: \n");
                    for(RequestEntity entity : wrongCadastreNums){
                        builder.append("Номер: ").append(entity.getCadastreNums()).append(" Регион: ").append(entity.getRegion()).append("\n");
                    }
                    builder.append("Всего успешно отправленных запросов: " + (entities.size() - wrongCadastreNums.size()));
                    partlySucces.setHeaderText(builder.toString());
                    Platform.runLater(partlySucces::showAndWait);
                } else {
                    Platform.runLater(successAlert::showAndWait);
                }
                Platform.runLater(() -> {
                    sendButton.setText(SEND);
                    sendButton.setDisable(false);
                });

            }).start();
        });

        sendButton.setText(SEND);
        bottom.getChildren().addAll(sendButton);
        bottom.setAlignment(Pos.BOTTOM_CENTER);

        vbox.getChildren().addAll(nums, /*box,*/ path, one, two, bottom);
        layout.getChildren().add(vbox);

        Scene scene = new Scene(layout, 450, 450);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                stage.close();
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    private static Map<Integer, String> loadRegions() {
        Map<Integer, String> values = new HashMap<>();
        try {
            InputStream in = RequestPopup.class.getResourceAsStream("/regions.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()) {
                String[] region = scanner.nextLine().split(";");
                values.put(Integer.parseInt(region[0]), region[1]);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        return values;
    }
}
