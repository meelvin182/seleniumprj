package ru.sokolov.model.pages;

import com.google.common.base.Predicate;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.RequestEntity;

import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractPage {

    protected WebDriverWait driverWait;
    protected WebDriver driver;
    protected RequestEntity entity;

    public void waitForPageLoad(WebDriver driver) {
        this.driver = driver;
        driverWait = new WebDriverWait(driver, 2000);
        Predicate<WebDriver> pageLoaded = new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver input) {
                return ((JavascriptExecutor) input).executeScript("return document.readyState").equals("complete");
            }

        };
        driverWait.until(pageLoaded);
    }

    public abstract AbstractPage setPageData(WebDriver driver, RequestEntity entity) throws Exception;
}
