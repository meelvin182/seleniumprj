package ru.sokolov.gui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.SentRequest;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static ru.sokolov.gui.MainScreen.DOWNLOADED_STATUS;
import static ru.sokolov.gui.MainScreen.UPDATING_STATUS_STATUS;
import static ru.sokolov.gui.RequestPopup.SENDING;

public class RequestsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestsManager.class);

    private final Semaphore mutex = new Semaphore(1, true);
    private static final RequestsManager INSTANCE = new RequestsManager();
    private final ExecutorService updateService = Executors.newSingleThreadExecutor();
    private final ExecutorService sendService = Executors.newScheduledThreadPool(5);
    private static final TableItemsManager itemsManager = TableItemsManager.getInstance();

    public void send(){
        sendService.execute(() -> {
            try {
                mutex.acquire();
                CoreKernelSupaClazz.sendRequests(itemsManager.getToSend());
            } catch (Exception e) {
                LOGGER.error("Error when sending requests: {}", e);
            } finally {
                mutex.release();
            }
        });
    }

    public void updateStatuses(){
        Map<SentRequest, String> toUpdate =
                itemsManager
                        .getAllItems()
                        .stream()
                        .filter(t -> !DOWNLOADED_STATUS.equals(t.getStatus()) && !SENDING.equals(t.getStatus()))
                        .collect(Collectors.toMap(k -> k, SentRequest::getStatus));
        toUpdate.keySet().forEach(t -> t.setStatus(UPDATING_STATUS_STATUS));
        updateService.execute(() -> {
            try {
                mutex.acquire();
                CoreKernelSupaClazz.updateRequestsStatus(new ArrayList<>(toUpdate.keySet()));
            } catch (Exception e) {
                LOGGER.error("Error when updating statuses: {}", e);
            } finally {
                toUpdate.keySet().stream()
                        .filter(t -> UPDATING_STATUS_STATUS.equals(t.getStatus()))
                        .collect(Collectors.toList())
                        .forEach(t -> t.setStatus(toUpdate.get(t)));
                mutex.release();
            }
        });
    }

    private RequestsManager() {}

    public static RequestsManager getInstance(){
        return INSTANCE;
    }
}
