package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;

import java.util.ArrayList;
import java.util.List;

public class AllRequestsPage extends AbstractPage {

    private static final String ROW_ELEMENT_CLASS_NAME_EVEN = "v-table-row";
    private static final String ROW_ELEMENT_CLASS_NAME_ODD = "v-table-row-odd";
    private static final String NUM_AND_DATE_CLASS_NAME = "v-table-cell-wrapper";
    private static final String STATUS_CLASS_NAME = "v-label-undef-w";
    private static final String BUTTON_CLASS_NAME = "v-link";
    private static final String READY_STATUS = "Завершена";
    private static List<WebElement> rows;

    public static void setPageData(){
        rows = driver.findElements(By.className(ROW_ELEMENT_CLASS_NAME_EVEN));
        rows.addAll(driver.findElements(By.className(ROW_ELEMENT_CLASS_NAME_ODD)));
    }

    //TODO Create stub entity with key
    public static void process (RequestEntity entity) throws Exception{
        SecondPage.openRequests(entity);
        waitForPageLoad(driver);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(ROW_ELEMENT_CLASS_NAME_EVEN)));
        setPageData();
    }

    public static List<SentRequest> getRequests(){
        List<SentRequest> requests = new ArrayList<>();
        for(WebElement element : rows){
            SentRequest request = new SentRequest();
            List<WebElement> numAndDate = element.findElements(By.className(NUM_AND_DATE_CLASS_NAME));
            request.setRequestNum(numAndDate.get(0).getAttribute("value"));
            request.setCreationDate(numAndDate.get(1).getAttribute("value"));
            String status = element.findElement(By.className(STATUS_CLASS_NAME)).getAttribute("value");
            request.setStatus(status);
            request.setDownload(READY_STATUS.equals(status) ? true : false);
            requests.add(request);
        }
        return requests;
    }
}
