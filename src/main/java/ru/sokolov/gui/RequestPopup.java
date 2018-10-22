package ru.sokolov.gui;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.pages.AbstractPage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.sokolov.CoreKernelSupaClazz.sendRequest;

public class RequestPopup {

    private List<TextField> fields = Stream.generate(TextField::new).limit(5).collect(Collectors.toList());
    private List<String> fieldLenghts = Arrays.stream("6F9619FF-8B86-D011-B42D-00CF4FC964FF".split("-"))
            .collect(Collectors.toList());
    public static List<String> ruzkeRegions = loadRegions();

    private static final String SEND = "Отправить";

    public RequestPopup(Stage parent) {

        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle("Введите данные запроса");
        stage.initOwner(parent);
        VBox vbox = new VBox();
        vbox.setSpacing(5);//Set vbox spacing
        vbox.setAlignment(Pos.CENTER_LEFT);

        HBox key = new HBox();
        key.getChildren().add(new Text("Ключ доступа"));
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
        nums.setPromptText("Кадастровый номер");

        ComboBox box = new ComboBox();
        box.getItems().addAll(ruzkeRegions);
        box.setPromptText("Выбрать регион");

        HBox path = new HBox();
        path.setSpacing(10);
        path.setPrefWidth(200);

        Button button = new Button();
        button.setText("...");
        TextField pathField = new TextField();
        pathField.setPromptText("Choose folder");
        button.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Request save folder");
            File selectedDirectory = chooser.showDialog(stage);
            pathField.setText(selectedDirectory == null ? null : selectedDirectory.getAbsolutePath());
        });
        path.getChildren().add(pathField);
        path.getChildren().add(button);

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton one = new RadioButton("Запросить сведения об объекте");
        RadioButton two = new RadioButton("Запросить сведения о переходе прав на объект");
        one.setToggleGroup(toggleGroup);
        two.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(one);

        HBox bottom = new HBox();
        Button sendButton = new Button();
        sendButton.setOnAction(event -> {
            RequestEntity entity = new RequestEntity(
                    fields.stream().map(s -> s == null ? null : s.getText()).collect(Collectors.toList()),
                    nums == null ? null : nums.getText(),
                    box.getValue() == null ? null : box.getValue().toString(),
                    pathField.getText(),
                    one.isSelected(),
                    two.isSelected());
            try {
                MainScreen.table.getItems().add(sendRequest(entity));
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
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
