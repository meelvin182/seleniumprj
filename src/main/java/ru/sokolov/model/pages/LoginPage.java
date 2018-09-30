package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.RequestEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LoginPage extends AbstractPage {

    private static final String TEXT_FIELD_CLASSNAME = "v-textfield";
    private static final String BUTTON_CLASSNAME = "normalButton";

    List<WebElement> keyFields;

    @Override
    public LoginPage setPageData(WebDriver driver, RequestEntity entity) throws InterruptedException {
        this.entity = entity;
        this.driver = driver;
        driverWait = new WebDriverWait(driver, 2000);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(TEXT_FIELD_CLASSNAME)));
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(BUTTON_CLASSNAME)));
        List<WebElement> list = new ArrayList<>();
        while (list.size() != 5) {
            list.addAll(driver.findElements(By.className("v-textfield")));
        }
        Iterator<String> iterator = entity.getKeyParts().iterator();
        keyFields = list;
        for(WebElement element : list){
            String text = iterator.next();
            element.sendKeys(text);
            driverWait.until(ExpectedConditions.attributeContains(element, "value", text));
            //Unavoidable hack here, sometime random fields are skipped for unknown reason
            TimeUnit.MILLISECONDS.sleep(250);
        }
        driver.findElement(By.className("normalButton")).click();
        return this;
    }

    public LoginPage() {
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public SecondPage login(){
        return new SecondPage().setPageData(driver, entity);
    }
}
