package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.sokolov.model.entities.RequestEntity;

import java.util.List;

public class AllRequestsPage extends AbstractPage {

    private static final String ROW_ELEMENT_CLASS_NAME_EVEN = "v-table-row";
    private static final String ROW_ELEMENT_CLASS_NAME_ODD = "v-table-row-odd";
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
        System.out.println(rows.size());
    }
}
