package ru.sokolov.model.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.gui.MainScreen;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.sokolov.model.pages.LoginPage.LOADING_INDICATOR_DELAY_CLASSNAME;
import static ru.sokolov.model.pages.LoginPage.LOADING_INDICATOR_WAIT_CLASSNAME;
import static ru.sokolov.model.pages.SecondPage.MY_REQUESTS_BUTTON_ELEMENT_NAME;

public class AllRequestsPage extends AbstractPage {

    private static final String ROW_ELEMENT_CLASS_NAME_EVEN = "v-table-row";
    private static final String ROW_ELEMENT_CLASS_NAME_ODD = "v-table-row-odd";
    private static final String NUM_AND_DATE_CLASS_NAME = "v-table-cell-wrapper";
    private static final String STATUS_CLASS_NAME = "v-label-undef-w";
    private static final String BUTTON_CLASS_NAME = "v-link";
    private static final String READY_STATUS = "Завершена";
    private static final String SEARCH_BY_NUM_FIELD_CLASS_NAME = "v-textfield";
    private static final String UPDATE_BUTTON_CLASS_NAME = "v-button-caption";
    private static final String UPDATE_BUTTON_NAME = "Обновить";
    private static final String REMOVE_FILTER_BUTTON_NAME = "Очистить фильтр";
    private static final String DOWNLOAD_BUTTON_CLASS_NAME = "v-icon"; //For some reason this is class of download button, not v-link

    private static WebElement update;
    private static WebElement reset;
    private static List<WebElement> rows;

    private static final Logger LOGGER = LoggerFactory.getLogger(AllRequestsPage.class);

    public static void setPageData(){
        waitForPageLoad(driver);
        for (WebElement element : driver.findElements(By.className(UPDATE_BUTTON_CLASS_NAME))){
            if(UPDATE_BUTTON_NAME.equals(element.getText())){
                update = element;
            } else if(REMOVE_FILTER_BUTTON_NAME.equals(element.getText())){
                reset = element;
            }
        }
    }

    public static void process (LoginEntity entity) throws Exception{
        SecondPage.openRequests(entity);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(ROW_ELEMENT_CLASS_NAME_EVEN)));
        LOGGER.info("My requests page opened");
        setPageData();
    }

    //Unused atm
    @Deprecated
    public static List<SentRequest> getRequests(){
        List<SentRequest> requests = new ArrayList<>();
        for(WebElement element : rows){
            SentRequest request = new SentRequest();
            List<WebElement> numAndDate = element.findElements(By.className(NUM_AND_DATE_CLASS_NAME));
            request.setRequestNum(numAndDate.get(0).getAttribute("value"));
            request.setCreationDate(numAndDate.get(1).getAttribute("value"));
            String status = element.findElement(By.className(STATUS_CLASS_NAME)).getAttribute("value");
            request.setStatus(status);
            request.setDownload(READY_STATUS.equals(status));
            requests.add(request);
        }
        return requests;
    }

    public static void updateRequestsStatus(List<SentRequest> requests) throws Exception{
        Map<List<String>, List<SentRequest>> keysToRequestMap = new HashMap<>();
        for(SentRequest request : requests){
            List<String> keyParts = request.getKeyParts();
            if(keysToRequestMap.containsKey(keyParts)){
                keysToRequestMap.get(keyParts).add(request);
            } else {
                List<SentRequest> mapVal = new ArrayList<>();
                mapVal.add(request);
                keysToRequestMap.put(keyParts, mapVal);
            }
        }
        for(Map.Entry<List<String>, List<SentRequest>> entry : keysToRequestMap.entrySet()){
            RequestEntity loginEntity = new RequestEntity();
            loginEntity.setKeyParts(entry.getKey());
            process(loginEntity);
            LOGGER.info("Updating requests with key: {]", entry.getKey());
            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(ROW_ELEMENT_CLASS_NAME_ODD)));
            LOGGER.info("Odd element found");
            for(SentRequest request : entry.getValue()){
                updateRequestStatus(request);

            }
        }
    }

    public static void updateRequestStatus(SentRequest request) throws Exception{
        LOGGER.info("Searching for request with num: " + request.getRequestNum());
        WebElement element = getRequestWebElement(request);
        LOGGER.info("Waiting for status element located");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(STATUS_CLASS_NAME)));
        TimeUnit.MILLISECONDS.sleep(250);
        LOGGER.info("Request found");
        String status = element.findElement(By.className(STATUS_CLASS_NAME)).getText();
        LOGGER.info("Old status: {} New status: {}", request.getStatus(), status);
        request.setStatus(status);
        request.setDownload(READY_STATUS.equals(status));
        MainScreen.table.refresh();
        if(request.isDownload()){
            LOGGER.info("Downloading request: {}");
            downloadRequest(element);
            CoreKernelSupaClazz.unzipDownloadedRequest(request);
        }
    }

    public static void downloadRequest(WebElement element) throws Exception{
        element.findElement(By.className(DOWNLOAD_BUTTON_CLASS_NAME)).click();
        LOGGER.info("Download started");
        //Hope this is enough
    }

    public static void downloadRequest(SentRequest request) throws Exception{
        process(request);
        LOGGER.info("Downloading request: {}", request.getRequestNum());
        getRequestWebElement(request).findElement(By.className(DOWNLOAD_BUTTON_CLASS_NAME)).click();
        LOGGER.info("Download started");
    }

    private static WebElement getRequestWebElement(SentRequest request) throws Exception{
        LOGGER.info("Waiting for search field");
        WebElement textField = driver.findElement(By.className(SEARCH_BY_NUM_FIELD_CLASS_NAME));
        textField.clear();
        textField.sendKeys(Keys.ENTER);
        try{
            WebElement element = driver.findElement(By.xpath("//*[contains(text(), '"+ request.getRequestNum() +"')]")).findElement(By.xpath("..")).findElement(By.xpath(".."));
            return element;
        } catch (NoSuchElementException e){
            LOGGER.info("No such element on 1st page");
        }
        LOGGER.info("Textfield filled");
        TimeUnit.MILLISECONDS.sleep(250);
        textField.sendKeys(request.getRequestNum());
        textField.sendKeys(Keys.ENTER);
        TimeUnit.MILLISECONDS.sleep(250);
        LOGGER.info("Waiting till textfield have required value");
        driverWait.until(ExpectedConditions.attributeContains(textField, "value", request.getRequestNum()));
        setPageData();
        LOGGER.info("Updating search params");
        update.click();
        textField.clear();
        textField.sendKeys(Keys.ENTER);
        TimeUnit.MILLISECONDS.sleep(250);
        LOGGER.info("Waiting till even element has ID: {}", request.getRequestNum());
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ request.getRequestNum() +"')]")));
        LOGGER.info("Providing request element");
        return driver.findElement(By.xpath("//*[contains(text(), '"+ request.getRequestNum() +"')]")).findElement(By.xpath("..")).findElement(By.xpath(".."));
    }
}
