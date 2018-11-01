package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.exceptions.WrongCadastreNumException;

import java.util.concurrent.TimeUnit;

public class RequestsPage extends AbstractPage {

    private static final String COULDNT_FIND_ERROR_CLASS_NAME = "gwt-HTML";
    private static final String FOUND_NOTIFICATION_CLASS_NAME = "v-table-cell-content-cadastral_num";
    private static final String SEND_BUTTON_CLASS_NAME = "v-table-cell-content-cadastral_num";

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestsPage.class);

    public static void continueToRequestOverview(RequestEntity entity) throws Exception{
        SearchObjectsPage.sendRequest(entity);
        driverWait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.className(FOUND_NOTIFICATION_CLASS_NAME)),
                ExpectedConditions.presenceOfElementLocated(By.className(COULDNT_FIND_ERROR_CLASS_NAME))));
        TimeUnit.MILLISECONDS.sleep(500);
        try {
            driver.findElement(By.className(FOUND_NOTIFICATION_CLASS_NAME));
        } catch (Exception e){
            LOGGER.info("Found nothing");
            throw new WrongCadastreNumException();
        }
        LOGGER.info("Opening request overview");
        driver.findElement(By.className(SEND_BUTTON_CLASS_NAME)).click();
    }
}
