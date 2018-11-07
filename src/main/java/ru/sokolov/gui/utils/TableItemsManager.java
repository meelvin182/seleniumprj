package ru.sokolov.gui.utils;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.sokolov.gui.RequestPopup.SENDING;

public class TableItemsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableItemsManager.class);

    private TableView<SentRequest> table;
    private TextField filter;
    private List<SentRequest> allItems = new ArrayList<>();
    private static final TableItemsManager INSTANCE = new TableItemsManager();
    private Map<RequestEntity, SentRequest> toSend = new HashMap<>();

    public void loadItems(List<TextField> fields){
        try {
            allItems.clear();
            allItems.addAll(!StringUtils.isEmpty(fields.get(4).getText())
                    ? CoreKernelSupaClazz.readAllRequests(fields.stream().map(t -> t.getText()).collect(Collectors.toList()))
                    : CoreKernelSupaClazz.readAllRequests());
        } catch (IOException e) {
            LOGGER.error("ERROR: {}", e);
        }
    }

    public void addItems(Collection<SentRequest> items){
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
                .filter(t -> SENDING.equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    public void setTable(TableView table) {
        this.table = table;
    }

    public void refreshItems(){
        List<SentRequest> filteredItems = getFilteredItems();
        filteredItems.sort(Comparator.comparingLong(o -> Long.parseLong(o.getRequestNum().replaceAll("\\D*", ""))));
        table.setItems(new ObservableListWrapper<>(getFilteredItems()));
        table.refresh();
    }

    public void addToBeSent(Map<RequestEntity, SentRequest> requests){
        requests.values().stream().forEach(System.out::println);
        addItems(requests.values());
        toSend.putAll(requests);
    }

    public void setFilter(TextField filter) {
        this.filter = filter;
    }

    public List<SentRequest> getAllItems() {
        return allItems;
    }

    public Map<RequestEntity, SentRequest> getToSend() {
        toSend = toSend.entrySet().stream()
                .filter(t -> t.getValue().getStatus().equals(SENDING))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return toSend;
    }

    public static TableItemsManager getInstance(){
        return INSTANCE;
    }

    private TableItemsManager() {
    }
}
