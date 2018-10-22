package ru.sokolov.gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.pages.AllRequestsPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Application {

    private static int width = 1920 / 2;
    private static int height = 1080 / 2;

    public static final TableView<SentRequest> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        table.getColumns().addAll(getColumns());
        try {
            table.getItems().addAll(CoreKernelSupaClazz.readAllRequests());
        } catch (IOException e) {
            System.out.println("COULDN'T LOAD REQUESTS");
        }
        HBox buttons = new HBox();
        Button testButton = new TestButton(primaryStage);
        Button updateRequestsButton = new Button();
        updateRequestsButton.setOnAction(event -> {
            new Thread(() -> {
                try {
                    CoreKernelSupaClazz.updateRequestsStatus(table.getItems());
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }).start();
            table.refresh();
        });
        updateRequestsButton.setText("Обновить статус запросов");
        buttons.getChildren().addAll(testButton, updateRequestsButton);

        StackPane layout = new StackPane();
        layout.getChildren().add(buttons);

        StackPane root = new StackPane();
        root.setPadding(new Insets(4));
        root.getChildren().add(table);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(layout, root);

        Scene scene = new Scene(vbox, width, height);

        primaryStage.setTitle("ЕГРН Запросы");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            for (SentRequest request : table.getItems()) {
                try {
                    CoreKernelSupaClazz.saveRequestToJson(request);
                } catch (Exception e) {
                    System.out.println("COULDN'T SAVE REQUEST");
                }
            }
        });
        primaryStage.show();
    }

    private List<TableColumn<SentRequest, String>> getColumns() {
        List<TableColumn<SentRequest, String>> columns = new ArrayList<>();
        TableColumn<SentRequest, String> num = new TableColumn<SentRequest, String>("Номер запроса");
        num.setCellValueFactory(new PropertyValueFactory<>("requestNum"));

        TableColumn<SentRequest, String> date = new TableColumn<SentRequest, String>("Дата создания");
        date.setCellValueFactory(new PropertyValueFactory<>("creationDate"));

        TableColumn<SentRequest, String> status = new TableColumn<SentRequest, String>("Статус");
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<SentRequest, String> folder = new TableColumn<SentRequest, String>("Путь сохранения");
        folder.setCellValueFactory(new PropertyValueFactory<>("path"));


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
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    SentRequest sentRequest = getTableView().getItems().get(getIndex());
                                    if (sentRequest.isDownload()) {
                                        btn.setOnAction(event -> {
                                            try {
                                                CoreKernelSupaClazz.downloadRequest(sentRequest);
                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
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
        columns.add(folder);
        columns.add(download);
        columns.forEach(column -> column.setPrefWidth(150));
        return columns;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
