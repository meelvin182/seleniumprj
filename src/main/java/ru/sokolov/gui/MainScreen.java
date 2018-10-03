package ru.sokolov.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.sokolov.model.entities.SentRequest;

import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Application {

    private static int width = 1920 / 2;
    private static int height = 1080 / 2;

    private final TableView<SentRequest> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        table.getColumns().addAll(getColumns());
        Button testButton = new TestButton(primaryStage);

        StackPane layout = new StackPane();
        layout.getChildren().add(testButton);

        StackPane root = new StackPane();
        root.setPadding(new Insets(4));
        root.getChildren().add(table);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(layout, root);

        Scene scene = new Scene(vbox, width, height);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                System.exit(0);
            }
        });

        primaryStage.setTitle("slnmprj");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<TableColumn<SentRequest, String>> getColumns(){
        List<TableColumn<SentRequest, String>> columns = new ArrayList<>();
        TableColumn<SentRequest, String> num = new TableColumn<SentRequest, String>("Номер запроса");
        num.setCellValueFactory(new PropertyValueFactory<>("requestNum"));

        TableColumn<SentRequest, String> date = new TableColumn<SentRequest, String>("Дата создания");
        date.setCellValueFactory(new PropertyValueFactory<>("creationDate"));

        TableColumn<SentRequest, String> status = new TableColumn<SentRequest, String>("Статус");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<SentRequest, String> download = new TableColumn<SentRequest, String>("Скачать");
        Callback<TableColumn<SentRequest, String>, TableCell<SentRequest, String>> cellFactory =
                new Callback<TableColumn<SentRequest, String>, TableCell<SentRequest, String>>() {
                    @Override
                    public TableCell call(final TableColumn<SentRequest, String> param) {
                        final TableCell<SentRequest, String> cell = new TableCell<SentRequest, String>() {

                            final Button btn = new Button("Скачать");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                    if (empty){
                                        setGraphic(null);
                                        setText(null);
                                    } else {
                                        SentRequest sentRequest = getTableView().getItems().get(getIndex());
                                        if (sentRequest.isDownload()) {
                                            btn.setOnAction(event -> {
                                                //TODO Add some logic here
                                            });
                                            setGraphic(btn);
                                            setText(null);
                                        }
                                    }
                            }
                        };
                        return cell;
                    }
                };
        download.setCellFactory(cellFactory);

        columns.add(num);
        columns.add(date);
        columns.add(status);
        columns.add(download);
        columns.forEach(column -> column.setPrefWidth(250));
        return columns;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
