package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.exceptions.CouldntLoginException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ru.sokolov.model.pages.SecondPage.MY_REQUESTS_BUTTON_ELEMENT_NAME;

public class LoginPage extends AbstractPage {

    private static final String TEXT_FIELD_CLASSNAME = "v-textfield";
    private static final String BUTTON_CLASSNAME = "normalButton";
    private static final String ERROR = "v-Notification-error";

    public static final String LOADING_INDICATOR_CLASSNAME = "v-loading-indicator";
    public static final String LOADING_INDICATOR_DELAY_CLASSNAME = "v-loading-indicator-delay";
    public static final String LOADING_INDICATOR_WAIT_CLASSNAME = "v-loading-indicator-wait";

    public static void setPageData(LoginEntity entity) throws InterruptedException {
        System.out.println("Waiting for login page loaded");
        driverWait = new WebDriverWait(driver, 120);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(TEXT_FIELD_CLASSNAME)));
        List<WebElement> list = new ArrayList<>();
        while (list.size() != 5) {
            list.addAll(driver.findElements(By.className("v-textfield")));
        }
        Iterator<String> iterator = entity.getKeyParts().iterator();
        for (WebElement element : list) {
            String text = iterator.next();
            element.sendKeys(text);
            driverWait.until(ExpectedConditions.attributeContains(element, "value", text));
            //Unavoidable hack here, sometime random fields are skipped for unknown reason
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    public static void login() throws Exception {
        driver.findElement(By.className(BUTTON_CLASSNAME)).click();
        driverWait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.className(ERROR)),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ MY_REQUESTS_BUTTON_ELEMENT_NAME +"')]"))));
        TimeUnit.MILLISECONDS.sleep(250);
        WebElement element;
        try {
            element = driver.findElement(By.className(ERROR));
        } catch (Exception e) {
            System.out.println("Logged in");
            return;
        }
        if(element != null){
            System.out.println("Wrong Login");
            throw new CouldntLoginException();
        }
    }
}
