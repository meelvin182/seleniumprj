package ru.sokolov.model.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class AbstractPage {

    protected static WebDriverWait driverWait;
    public static WebDriver driver;

    public static void waitForPageLoad(WebDriver driver) {
        setDriver(driver);
        if(driverWait == null) {
            driverWait = new WebDriverWait(driver, 180);
        }
        driverWait.until(input -> ((JavascriptExecutor) input).executeScript("return document.readyState").equals("complete"));
    }

    public static void setDriverWait(WebDriverWait driverWait) {
        AbstractPage.driverWait = driverWait;
    }

    public static void setDriver(WebDriver driver) {
        AbstractPage.driver = driver;
        setDriverWait(new WebDriverWait(driver, 180));
    }
}
