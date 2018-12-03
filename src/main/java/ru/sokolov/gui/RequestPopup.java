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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.gui.utils.RequestsManager;
import ru.sokolov.gui.utils.TableItemsManager;
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
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static ru.sokolov.CoreKernelSupaClazz.closeDriver;

public class RequestPopup {

    public static final String ENTER_REQUEST_PARAMS = "Введите данные запроса";
    public static final String KEY = "Ключ доступа";
    public static final String CADASTRE_NUM = "Кадастровый номер";
    public static final String CHOOSE_REGION = "Выбрать регион";
    public static final String CHOOSE_FOLDER_PROMPT = "Папка загрузки";
    public static final String FOLDER_SELECTOR_WINDOW_NAME = "Request save folder";
    public static final String RADIO_BUTTON_ONE = "Запросить сведения об объекте";
    public static final String RADIO_BUTTON_TWO = "Запросить сведения о переходе прав на объект";
    public static final String COULDNT_LOGIN = "Проверьте ключ";
    public static final String SENDING = "Отправляется";
    public static final String SEND = "Отправить";
    public static final String SENT = "Отправлен";
    public static final String NOT_FOUND = "Ничего не найдено по запросу";

    private static final TableItemsManager itemsManager = TableItemsManager.getInstance();
    private static final RequestsManager requestsManager = RequestsManager.getInstance();

    public static Map<Integer, String> regions = loadRegions();
    public static Collection<String> ruzkeRegions = regions.values();

    private Text textHolder = new Text();
    private double oldHeight = 0;

    public static Logger LOGGER;

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

        TextArea nums = new CopyPasteNumsArea();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            c.setText(c.getText().replaceAll(";", "\n"));
            return c ;
        };
        nums.setTextFormatter(new TextFormatter<>(filter));
        nums.setPromptText(CADASTRE_NUM);
        nums.setWrapText(true);
        nums.setPrefSize(200, 10);

        textHolder.textProperty().bind(nums.textProperty());
        textHolder.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldHeight != newValue.getHeight()) {
                oldHeight = newValue.getHeight();
                nums.setPrefHeight(textHolder.getLayoutBounds().getHeight() + 20); // +20 is for paddings
            }
        });
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
        successAlert.setOnCloseRequest(event -> stage.close());

        Alert partlySucces = new Alert(Alert.AlertType.INFORMATION);
        partlySucces.setTitle("Notification");
        partlySucces.setOnCloseRequest(event -> stage.close());

        Alert incorrectKey = new Alert(Alert.AlertType.ERROR);
        incorrectKey.setHeaderText(COULDNT_LOGIN);
        incorrectKey.setTitle("Notification");

        HBox bottom = new HBox();
        Button sendButton = new Button();
        sendButton.setOnAction((ActionEvent event) -> {
            boolean incorrectInput = false;
            nums.setStyle(null);
            if (nums.getText().isEmpty()) {
                nums.setStyle("-fx-background-color: #ff736e;");
                incorrectInput = true;
            }
            if (incorrectInput) {
                return;
            }

            String enteredNums = nums.getText();
            String[] allNums = enteredNums.contains("\n") ? enteredNums.split("\n") : new String[]{enteredNums};
            List<RequestEntity> entities = new ArrayList<>();
            LOGGER.info("SENDING REQUESTS:");
            for(String num : allNums){
                if(!StringUtils.isEmpty(num) && num.length()>=2 && StringUtils.isNumeric(num.substring(0,2))){
                    String region = regions.get(Integer.parseInt(num.substring(0,2)));
                    LOGGER.info("NUM: {} REGION: {}", num, region);
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
            LOGGER.info(" ");
            Map<RequestEntity, SentRequest> requestMap = entities.stream().collect(Collectors.toMap(t -> t, SentRequest::new));
            itemsManager.addToBeSent(requestMap);
            requestsManager.send();
            stage.close();
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
