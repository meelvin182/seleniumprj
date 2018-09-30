package ru.sokolov.model.pages;

import com.google.common.base.Predicate;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.RequestEntity;

import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractPage {

    protected static WebDriverWait driverWait;
    protected static WebDriver driver;
    protected static RequestEntity entity;

    public static void waitForPageLoad(WebDriver driver) {
        setDriver(driver);
        driverWait = new WebDriverWait(driver, 2000);
        Predicate<WebDriver> pageLoaded = new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver input) {
                return ((JavascriptExecutor) input).executeScript("return document.readyState").equals("complete");
            }

        };
        driverWait.until(pageLoaded);
    }

    public static void setDriverWait(WebDriverWait driverWait) {
        AbstractPage.driverWait = driverWait;
    }

    public static void setDriver(WebDriver driver) {
        AbstractPage.driver = driver;
    }

    public static void setEntity(RequestEntity entity) {
        AbstractPage.entity = entity;
    }
}
