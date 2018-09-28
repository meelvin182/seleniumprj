package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class LoginPage extends AbstractPage {

    List<WebElement> keyFields;

    public LoginPage(WebDriver driver, String key) {
        this.driver = driver;
        driverWait = new WebDriverWait(driver, 2000);
        List<WebElement> list = new ArrayList<>();
        while (list.size() != 5) {
            list.addAll(driver.findElements(By.className("v-textfield")));
        }
        Iterator<String> iterator = Arrays.stream(key.split("-")).collect(Collectors.toList()).iterator();
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
}
