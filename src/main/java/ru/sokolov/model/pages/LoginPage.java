package ru.sokolov.model.pages;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.exceptions.CouldntLoginException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ru.sokolov.CoreKernelSupaClazz.MAIN_PAGE;
import static ru.sokolov.model.pages.SecondPage.MY_REQUESTS_BUTTON_ELEMENT_NAME;

public class LoginPage extends AbstractPage {

    private static final String TEXT_FIELD_CLASSNAME = "v-textfield";
    private static final String BUTTON_CLASSNAME = "normalButton";
    private static final String ERROR = "v-Notification-error";
    private static final String SYSTEM_NOTIFICATION = "v-Notification-system";

    public static final String LOADING_INDICATOR_CLASSNAME = "v-loading-indicator";
    public static final String LOADING_INDICATOR_DELAY_CLASSNAME = "v-loading-indicator-delay";
    public static final String LOADING_INDICATOR_WAIT_CLASSNAME = "v-loading-indicator-wait";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginPage.class);

    public static void setPageData(LoginEntity entity) throws InterruptedException {
        LOGGER.info("Waiting for login page loaded");
        waitForPageLoad(driver);
        driverWait = new WebDriverWait(driver, 30);
        for(int i=0; i<3; i++){
            try{
                LOGGER.info("ATTEMPT 1");
                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(TEXT_FIELD_CLASSNAME)));
                LOGGER.info("SUCCESS");
                break;
            } catch (Exception e){
                LOGGER.info("FAILED");
                driver.navigate().to(MAIN_PAGE);
                waitForPageLoad(driver);
            }
        }
        driverWait = new WebDriverWait(driver, 200);
        LOGGER.info("WAITING FOR ELEMENT: {}", TEXT_FIELD_CLASSNAME);
        List<WebElement> list = new ArrayList<>();
        while (list.size() != 5) {
            list.addAll(driver.findElements(By.className("v-textfield")));
        }
        String key = StringUtils.join(entity.getKeyParts(), "-");
        LOGGER.info("Sending key: {}", key);
        list.get(0).sendKeys(key);
        LOGGER.info("Waiting for fields populated by key parts");
        driverWait.until(ExpectedConditions.attributeContains(list.get(4), "value", entity.getKeyParts().get(4)));
    }

    public static void login() throws Exception {
        LOGGER.info("LOGGING IN");
        driver.findElement(By.className(BUTTON_CLASSNAME)).click();
        LOGGER.info("Waiting for error/successfully logged in");
        driverWait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.className(ERROR)),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ MY_REQUESTS_BUTTON_ELEMENT_NAME +"')]")), 
                ExpectedConditions.presenceOfElementLocated(By.className(SYSTEM_NOTIFICATION))));
        TimeUnit.MILLISECONDS.sleep(250);
        WebElement element = null;
        try {
            element = driver.findElement(By.className(ERROR));
        } catch (Exception e) {
            try{
                driver.findElement(By.className(SYSTEM_NOTIFICATION)).findElement(By.tagName("u")).click();
            } catch (Exception ok){
                LOGGER.info("Logged in");
                return;
            }
        }
        if(element != null){
            LOGGER.info("LOGIN IS INCORRECT: {}");
            throw new CouldntLoginException();
        }
    }
}
