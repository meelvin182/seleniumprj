package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.entities.RequestEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginPage extends AbstractPage {

    private static final String TEXT_FIELD_CLASSNAME = "v-textfield";
    private static final String BUTTON_CLASSNAME = "normalButton";

    public static void setPageData(RequestEntity entity) throws InterruptedException {
        driverWait = new WebDriverWait(driver, 2000);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(TEXT_FIELD_CLASSNAME)));
        List<WebElement> list = new ArrayList<>();
        while (list.size() != 5) {
            list.addAll(driver.findElements(By.className("v-textfield")));
        }
        Iterator<String> iterator = entity.getKeyParts().iterator();
        for(WebElement element : list){
            String text = iterator.next();
            element.sendKeys(text);
            driverWait.until(ExpectedConditions.attributeContains(element, "value", text));
            //Unavoidable hack here, sometime random fields are skipped for unknown reason
            TimeUnit.MILLISECONDS.sleep(400);
        }

    }

    public static void login(){
        driver.findElement(By.className("normalButton")).click();
    }
}
