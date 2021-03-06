package ru.sokolov.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.gui.utils.RequestsManager;
import ru.sokolov.gui.utils.TableItemsManager;
import ru.sokolov.model.entities.SentRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.sokolov.CoreKernelSupaClazz.LOGS_PATH;
import static ru.sokolov.gui.KeyPopup.fields;
import static ru.sokolov.gui.RequestPopup.NOT_FOUND;


public class MainScreen extends Application {

    public static final String SEND_REQUEST = "Отправить Запрос";
    public static final String ENTER_KEY = "Ввести ключ";
    public static final String UPDATE_STATUSES_BUTTON_NAME = "Обновить статус запросов";
    public static final String WINDOW_TITLE_NAME = "ЕГРН Запросы";
    public static final String DOWNLOADED_STATUS = "Скачано";
    public static final String DOWNLOADING_STATUS = "Скачивается...";
    public static final String FILE_NOT_FOUND = "Будет скачан при обновлении статусов";
    public static final String UPDATING_STATUS_STATUS = "Статус обновляется";

    private static final TableItemsManager itemsManager = TableItemsManager.getInstance();
    private static final RequestsManager requestsManager = RequestsManager.getInstance();

    public static final TableView<SentRequest> table = new TableView<>();
    public static Alert downloadFirefox = new Alert(Alert.AlertType.ERROR);
    public static Logger LOGGER;

    private static int width = 1920 / 2;
    private static int height = 1080 / 2;

    static {
        downloadFirefox.setTitle("No Firefox found");
        downloadFirefox.setHeaderText("Для работы приложения требуется Mozilla Firefox");
        downloadFirefox.setOnCloseRequest(event -> {
            LOGGER.info("EXIT AS NO FF FOUND");
            System.exit(0);
        });
    }


    @Override
    public void start(Stage primaryStage) {
        table.getColumns().addAll(getColumns());
        table.setColumnResizePolicy((param) -> true);
        table.setPrefHeight(height - 100);

        Alert downloadFirefox = new Alert(Alert.AlertType.ERROR);
        downloadFirefox.setTitle("No Firefox found");
        downloadFirefox.setHeaderText("Для работы приложения требуется Mozilla Firefox");
        downloadFirefox.setOnCloseRequest(event -> System.exit(1));

        TextField filter = new TextField();
        filter.setPromptText("Поиск запросов");
        filter.setOnKeyPressed(event -> itemsManager.refreshItems());
        itemsManager.setTable(table);
        itemsManager.setFilter(filter);

        HBox buttons = new HBox();
        Button sendButton = new Button();
        sendButton.setText(SEND_REQUEST);
        sendButton.setOnAction(event -> {
            new RequestPopup(primaryStage);
        });

        Button keyButton = new Button();
        keyButton.setText(ENTER_KEY);
        keyButton.setOnAction(event -> {
            KeyPopup keyPopup = new KeyPopup(primaryStage);
        });

        Button updateRequestsButton = new Button();
        updateRequestsButton.setOnAction(event -> {
            new Thread(() -> {
                try {
                    requestsManager.updateStatuses();
                } catch (Exception e) {
                    downloadFirefox.showAndWait();
                    e.printStackTrace(System.out);
                }
            }).start();
        });
        updateRequestsButton.setText(UPDATE_STATUSES_BUTTON_NAME);
        buttons.getChildren().addAll(sendButton, updateRequestsButton, keyButton);

        StackPane layout = new StackPane();
        layout.getChildren().add(buttons);

        StackPane root = new StackPane();
        root.setPadding(new Insets(4));
        root.getChildren().add(table);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(layout, filter, root);

        Scene scene = new Scene(vbox, width, height);

        primaryStage.setTitle(WINDOW_TITLE_NAME);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            CoreKernelSupaClazz.deleteBeforeSave(fields.stream().map(TextInputControl::getText).collect(Collectors.toList()));
            for (SentRequest request : itemsManager.getAllItems()
                    .stream()
                    .filter(t -> !t.getStatus().equals(NOT_FOUND))
                    .collect(Collectors.toList())) {
                CoreKernelSupaClazz.saveRequestToJson(request);
            }
            CoreKernelSupaClazz.saveKey(fields);
            requestsManager.shutdown();
            CoreKernelSupaClazz.closeDriver();
            LOGGER.info("Closing app");
            System.exit(0);
        });
        primaryStage.show();
        new KeyPopup(primaryStage);
    }

    private List<TableColumn<SentRequest, String>> getColumns() {
        List<TableColumn<SentRequest, String>> columns = new ArrayList<>();
        TableColumn<SentRequest, String> num = new TableColumn<SentRequest, String>("Номер запроса");
        num.setCellValueFactory(new PropertyValueFactory<>("requestNum"));

        TableColumn<SentRequest, String> cadastreNum = new TableColumn<SentRequest, String>("Кадастровый номер");
        cadastreNum.setCellValueFactory(new PropertyValueFactory<>("cadastreNum"));

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
                            final Button btn = new Button("Открыть");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    SentRequest sentRequest = getTableView().getItems().get(getIndex());
                                    if (sentRequest.isDownload()) {
                                        setGraphic(btn);
                                        setText(null);
                                        btn.setOnAction(event -> {
                                            try {
                                                if (!CoreKernelSupaClazz.openRequest(sentRequest)) {
                                                    sentRequest.setStatus(FILE_NOT_FOUND);
                                                    itemsManager.refreshItems();
                                                }
                                            } catch (Exception e) {
                                                LOGGER.error("Error occured when tried to open request folder: {}", e);
                                            }
                                        });
                                    }
                                }
                            }
                        };
                        return cell;
                    }
                };
        download.setCellFactory(cellFactory);

        columns.add(cadastreNum);
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
