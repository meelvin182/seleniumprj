package ru.sokolov.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.SentRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Application {

    private static final String SEND_REQUEST = "Отправить Запрос";
    private static final String UPDATE_STATUSES_BUTTON_NAME = "Обновить статус запросов";
    private static final String WINDOW_TITLE_NAME = "ЕГРН Запросы";
    private static final String DOWNLOADED_STATUS = "Скачано";
    private static final String DOWNLOADING_STATUS = "Скачивается...";
    private static final String DOWNLOAD_FAILED_STATUS = "Ошибка при загрузке";
    private static final String UPDATING_STATUS_STATUS = "Статус обновляется";

    public static final TableView<SentRequest> table = new TableView<>();


    private static int width = 1920 / 2;
    private static int height = 1080 / 2;

    @Override
    public void start(Stage primaryStage) {
        table.getColumns().addAll(getColumns());
        try {
            table.getItems().addAll(CoreKernelSupaClazz.readAllRequests());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        HBox buttons = new HBox();
        Button sendButton = new Button();
        sendButton.setText(SEND_REQUEST);
        sendButton.setOnAction(event -> {
            RequestPopup requestPopup = new RequestPopup(primaryStage);
        });
        Button updateRequestsButton = new Button();
        updateRequestsButton.setOnAction(event -> {
            new Thread(() -> {
                try {
                    List<SentRequest> items = table.getItems();
                    items.forEach(request -> request.setStatus(UPDATING_STATUS_STATUS));
                    table.refresh();
                    CoreKernelSupaClazz.updateRequestsStatus(table.getItems());
                    table.refresh();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }).start();
        });
        updateRequestsButton.setText(UPDATE_STATUSES_BUTTON_NAME);
        buttons.getChildren().addAll(sendButton, updateRequestsButton);

        StackPane layout = new StackPane();
        layout.getChildren().add(buttons);

        StackPane root = new StackPane();
        root.setPadding(new Insets(4));
        root.getChildren().add(table);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(layout, root);

        Scene scene = new Scene(vbox, width, height);

        primaryStage.setTitle(WINDOW_TITLE_NAME);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            for (SentRequest request : table.getItems()) {
                try {
                    CoreKernelSupaClazz.saveRequestToJson(request);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
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
                                            new Thread(() -> {
                                                sentRequest.setStatus(DOWNLOADING_STATUS);
                                                table.refresh();
                                                try {
                                                    CoreKernelSupaClazz.downloadRequest(sentRequest);
                                                    sentRequest.setStatus(DOWNLOADED_STATUS);
                                                    table.refresh();
                                                } catch (Exception e){
                                                    sentRequest.setStatus(DOWNLOAD_FAILED_STATUS);
                                                    table.refresh();
                                                    e.printStackTrace(System.out);
                                                }
                                            }).start();
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
