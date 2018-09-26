package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SearchObjectsPage {

    public final static String CADASTRE_AND_STREET_ELEMENTS_CLASS =  "v-textfield-prompt";

    private WebDriver driver;
    private WebElement cadastreNum;
    private WebElement streetName;

    public SearchObjectsPage(WebDriver driver) {
        this.driver = driver;
        setTextFieldElements(driver);
    }

    public void setTextFieldElements(WebDriver driver){
        List<WebElement> elementList = driver.findElements(By.className(CADASTRE_AND_STREET_ELEMENTS_CLASS));
        int maxWidth = Integer.parseInt(elementList.get(0).getAttribute("style").replaceAll("[\\D.]", ""));
        if(maxWidth > Integer.parseInt(elementList.get(1).getAttribute("style").replaceAll("[\\D.]", ""))){
            cadastreNum = elementList.get(0);
            streetName = elementList.get(1);
        } else {
            cadastreNum = elementList.get(1);
            streetName = elementList.get(0);
        }
    }

    public void setCadastreNum(String cadastreNumVal){
        cadastreNum.sendKeys(cadastreNumVal);
    }

    public void setStreetName(String streetNameVal){
        streetName.sendKeys(streetNameVal);
    }
}
