package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentSuccesfullyPage extends AbstractPage {

    public static final String POPUP_CLASS_NAME = "v-label-tipFont";
    public static final String BUTTON_CLASS_NAME = "v-button-wrap";
    public static final String BUTTON_TEXT = "Продолжить работу";

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestOverviewPage.class);


    public static String getRequestNum(){
        waitForPageLoad(driver);
        LOGGER.info("WAITING FOR FINAL POPUP");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(POPUP_CLASS_NAME)));
        String requestNum = driver.findElement(By.className(POPUP_CLASS_NAME)).findElement(By.tagName("b")).getText();
        for(WebElement element : driver.findElements(By.className(BUTTON_CLASS_NAME))){
            if(BUTTON_TEXT.equals(element.getText())){
                element.click();
            }
        }
        LOGGER.info("Request num: {} sent succesfully", requestNum);
        return requestNum;
    }
}
