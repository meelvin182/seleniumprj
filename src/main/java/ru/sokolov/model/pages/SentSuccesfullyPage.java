package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SentSuccesfullyPage extends AbstractPage {

    private static final String POPUP_CLASS_NAME = "v-label-tipFont";
    private static final String BUTTON_CLASS_NAME = "v-button-wrap";
    private static final String BUTTON_TEXT = "Продолжить работу";

    public static String getRequestNum(){
        waitForPageLoad(driver);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(POPUP_CLASS_NAME)));
        String requestNum = driver.findElement(By.className(POPUP_CLASS_NAME)).findElement(By.tagName("b")).getText();
        System.out.println("Request num: " + requestNum);
        for(WebElement element : driver.findElements(By.className(BUTTON_CLASS_NAME))){
            if(BUTTON_TEXT.equals(element.getText())){
                element.click();
            }
        }
        return requestNum;
    }
}
