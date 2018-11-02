package ru.sokolov.gui.utils;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.SentRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.sokolov.gui.RequestPopup.SENDING;

public class TableItemsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableItemsManager.class);

    private static TableView<SentRequest> table;
    private static TextField filter;
    private static List<SentRequest> allItems = new ArrayList<>();
    private static TableItemsManager INSTANCE;

    public void loadItems(List<TextField> fields){
        try {
            allItems.addAll(!StringUtils.isEmpty(fields.get(4).getText())
                    ? CoreKernelSupaClazz.readAllRequests(fields.stream().map(t -> t.getText()).collect(Collectors.toList()))
                    : CoreKernelSupaClazz.readAllRequests());
        } catch (IOException e) {
            LOGGER.error("ERROR: {}", e);
        }
    }

    public void addItems(List<SentRequest> items){
        allItems.addAll(items);
        refreshItems();
    }

    public List<SentRequest> getFilteredItems(){
        String filterExp = filter.getText();
        return allItems
                .stream()
                .filter(t -> t.getRequestNum().contains(filterExp) ||
                        t.getCreationDate().contains(filterExp) ||
                        t.getStatus().contains(filterExp))
                .collect(Collectors.toList());
    }

    public List<SentRequest> getNotSent(){
        return allItems
                .stream()
                .filter(t -> t.getStatus().equals(SENDING))
                .collect(Collectors.toList());
    }

    public void setTable(TableView table) {
        TableItemsManager.table = table;
    }

    public void refreshItems(){
        List<SentRequest> filteredItems = getFilteredItems();
        filteredItems.sort(Comparator.comparingLong(o -> Long.parseLong(o.getRequestNum().replaceAll("\\D*", ""))));
        table.setItems(new ObservableListWrapper<>(getFilteredItems()));
        table.refresh();
    }

    public void setFilter(TextField filter) {
        TableItemsManager.filter = filter;
    }

    public static TableItemsManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new TableItemsManager();
        }
        return INSTANCE;
    }

    private TableItemsManager() {
    }
}
