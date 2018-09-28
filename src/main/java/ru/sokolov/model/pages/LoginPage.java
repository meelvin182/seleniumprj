package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.RequestEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoginPage extends AbstractPage {

    List<WebElement> keyFields;

    public LoginPage(WebDriver driver, RequestEntity entity) {
        this.entity = entity;
        this.driver = driver;
        driverWait = new WebDriverWait(driver, 2000);
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
        }
        driver.findElement(By.className("normalButton")).click();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void process(){
        new SecondPage(driver, entity).process().process();
    }
}
