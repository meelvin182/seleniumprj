package ru.sokolov.gui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.sokolov.gui.MainScreen.DOWNLOADED_STATUS;
import static ru.sokolov.gui.MainScreen.UPDATING_STATUS_STATUS;
import static ru.sokolov.gui.RequestPopup.NOT_FOUND;
import static ru.sokolov.gui.RequestPopup.SENDING;

public class RequestsManager {

    public static Logger LOGGER;

    private static final Semaphore mutex = new Semaphore(1, true);
    private static RequestsManager INSTANCE = init();
    private final ExecutorService updateService = Executors.newSingleThreadExecutor();
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();
    private final TableItemsManager itemsManager = TableItemsManager.getInstance();
    private final ScheduledExecutorService sendScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService updateScheduler = Executors.newScheduledThreadPool(1);

    public void send(){
        sendService.execute(() -> {
            try {
                LOGGER.info("{} Trying to obtain mutex", this.toString());
                mutex.acquire();
                CoreKernelSupaClazz.sendRequests(itemsManager.getAllItems()
                        .stream()
                        .filter(t -> t.getStatus().equals(SENDING))
                        .collect(Collectors.toMap(SentRequest::getRequestEntity, t -> t)));
            } catch (Exception e) {
                LOGGER.error("Error when sending requests: {}", e);
            } finally {
                LOGGER.info("{} Releasing mutex", this.toString());
                    mutex.release();
            }
        });
    }

    public void updateStatuses(){
        LOGGER.info("Updating statuses");
        Map<SentRequest, String> toUpdate =
                itemsManager
                        .getAllItems()
                        .stream()
                        .filter(t -> !DOWNLOADED_STATUS.equals(t.getStatus()) && !SENDING.equals(t.getStatus()) && !NOT_FOUND.equals(t.getStatus()))
                        .collect(Collectors.toMap(k -> k, SentRequest::getStatus));
        toUpdate.keySet().forEach(t -> t.setStatus(UPDATING_STATUS_STATUS));
        updateService.execute(() -> {
            try {
                LOGGER.info("{} Trying to obtain mutex", this.toString());
                mutex.acquire();
                CoreKernelSupaClazz.updateRequestsStatus(new ArrayList<>(toUpdate.keySet()));
            } catch (Exception e) {
                LOGGER.error("Error when updating statuses: {}", e);
            } finally {
                toUpdate.keySet().stream()
                        .filter(t -> UPDATING_STATUS_STATUS.equals(t.getStatus()))
                        .collect(Collectors.toList())
                        .forEach(t -> t.setStatus(toUpdate.get(t)));
                LOGGER.info("{} Releasing mutex", this.toString());
                itemsManager.refreshItems();
                mutex.release();
            }
        });
    }

    private RequestsManager() { }

    private static RequestsManager init(){
        RequestsManager manager = new RequestsManager();
        manager.sendScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Map<RequestEntity, SentRequest> notSentAtm = new HashMap<>(manager.itemsManager.getAllItems()
                        .stream()
                        .filter(t -> t.getStatus().equals(SENDING))
                        .collect(Collectors.toMap(SentRequest::getRequestEntity, t -> t)));
                try {
                    LOGGER.info("{} Trying to obtain mutex", this.toString());
                    mutex.acquire();
                    CoreKernelSupaClazz.sendRequests(notSentAtm);
                } catch (Exception e) {
                    LOGGER.error("Error when sending requests: {}", e);
                } finally {
                    LOGGER.info("{} Releasing mutex", this.toString());
                    manager.itemsManager.refreshItems();
                    mutex.release();
                }
            }
        }, 5, 15, TimeUnit.MINUTES);
        manager.updateScheduler.scheduleAtFixedRate(manager::updateStatuses, 10, 33, TimeUnit.MINUTES);
        return manager;
    }

    public void shutdown(){
        updateScheduler.shutdown();
        sendScheduler.shutdown();
        updateService.shutdown();
        sendService.shutdown();
    }
    public static RequestsManager getInstance(){
        return INSTANCE;
    }
}
