package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private static final String DOWNLOAD_BUTTON_CLASS_NAME = "v-icon"; //For some reason this is class of download button, not v-link

    private static WebElement update;
    private static List<WebElement> rows;

    public static void setPageData(){
        waitForPageLoad(driver);
        for (WebElement element : driver.findElements(By.className(UPDATE_BUTTON_CLASS_NAME))){
            if(UPDATE_BUTTON_NAME.equals(element.getText())){
                update = element;
                break;
            }
        }
        rows = driver.findElements(By.className(ROW_ELEMENT_CLASS_NAME_EVEN));
        rows.addAll(driver.findElements(By.className(ROW_ELEMENT_CLASS_NAME_ODD)));
    }

    public static void process (LoginEntity entity) throws Exception{
        SecondPage.openRequests(entity);
        System.out.println("My requests page opened");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(ROW_ELEMENT_CLASS_NAME_EVEN)));
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
            System.out.println("Updating requests with key: " + entry.getKey());
            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(ROW_ELEMENT_CLASS_NAME_ODD)));
            for(SentRequest request : entry.getValue()){
                updateRequestStatus(request);
            }
        }
    }

    public static void updateRequestStatus(SentRequest request) throws Exception{
        System.out.println("Searching for request with num: " + request.getRequestNum());
        WebElement element = getRequestWebElement(request);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(STATUS_CLASS_NAME)));
        System.out.println("Request found");
        String status = element.findElement(By.className(STATUS_CLASS_NAME)).getText();
        System.out.println("Old status: " + request.getStatus() + " New status: " + status);
        request.setStatus(status);
        request.setDownload(READY_STATUS.equals(status));
    }

    public static void downloadRequest(SentRequest request) throws Exception{
        process(request);
        System.out.println("Downloading request");
        getRequestWebElement(request).findElement(By.className(DOWNLOAD_BUTTON_CLASS_NAME)).click();
        TimeUnit.SECONDS.sleep(3);
    }

    private static WebElement getRequestWebElement(SentRequest request) throws Exception{
        WebElement textField = driver.findElement(By.className(SEARCH_BY_NUM_FIELD_CLASS_NAME));
        textField.sendKeys(request.getRequestNum());
        driverWait.until(ExpectedConditions.attributeContains(textField, "value", request.getRequestNum()));
        update.click();
        TimeUnit.MILLISECONDS.sleep(500);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(ROW_ELEMENT_CLASS_NAME_EVEN)));
        return driver.findElement(By.className(ROW_ELEMENT_CLASS_NAME_EVEN));
    }
}
