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
import ru.sokolov.model.entities.RequestEntity;

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

import static ru.sokolov.CoreKernelSupaClazz.sendRequest;

public class RequestPopup {

    private static int width = 800;
    private static int height = 600;
    private List<TextField> fields = Stream.generate(TextField::new).limit(5).collect(Collectors.toList());
    private List<String> fieldLenghts = Arrays.stream("6F9619FF-8B86-D011-B42D-00CF4FC964FF".split("-"))
            .collect(Collectors.toList());
    public static List<String> ruzkeRegions = loadRegions();

    public RequestPopup(Stage parent) {

        StackPane layout = new StackPane();
        Stage stage = new Stage();
        stage.setTitle("ENTER REQUEST SHIED PLEZ");
        stage.initOwner(parent);
        VBox vbox = new VBox();
        vbox.setSpacing(5);//Set vbox spacing
        vbox.setAlignment(Pos.CENTER_LEFT);


        //Nachalo mata
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
        //Konec mata

        //KADASTROVIE NUMS LIST
        TextField nums = new TextField();
        nums.setPromptText("Enter cadastre nums");
        //LIST END

        //PADAT' VNIZ SPISOK
        ComboBox box = new ComboBox();
        box.getItems().addAll(ruzkeRegions);
        box.setPromptText("Select a region");
        //PADAT' VNIZ SPISOK KONEC

        //Failochooser
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
        //END FAILOCHOOSER

        //REQUEST SHTO TO TAM CHECKBOX
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton one = new RadioButton("Запросить сведения об объекте");
        RadioButton two = new RadioButton("Запросить сведения о переходе прав на объект");
        one.setToggleGroup(toggleGroup);
        two.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(one);
        //GET VALUE ETO .ISSELECTED

        //SEND BUTTON
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
            System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe");
            try {
                sendRequest(entity);
            } catch (Exception e){
                System.out.println(e);
            }
        });

        sendButton.setText("Отправить");
        bottom.getChildren().addAll(sendButton);
        bottom.setAlignment(Pos.BOTTOM_CENTER);
        //KONEC SEND MATON

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
            Scanner scanner = new Scanner(reader);       // create scanner to read
            while (scanner.hasNextLine()) {  // while there is a next line
                values.add(scanner.nextLine());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return values;
    }
}
