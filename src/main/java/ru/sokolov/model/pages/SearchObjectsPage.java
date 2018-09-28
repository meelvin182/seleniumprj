package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchObjectsPage extends AbstractPage {

    public final static String CADASTRE_AND_STREET_ELEMENTS_CLASS =  "v-textfield-prompt";
    public final static String dropdownClassName = "v-filterselect-suggestmenu";
    public final static String dropdownButtonClassName = "v-filterselect-button";
    public final static String findButtonClassName = "v-button-caption";

    private WebElement cadastreNums;
    private WebElement streetName;
    //getAttribute("value") should be used for this one, to retrieve it's text
    private WebElement region;
    private WebElement findButton;

    public SearchObjectsPage(WebDriver driver) {
        waitForPageLoad(driver);
        driverWait = new WebDriverWait(driver, 2000);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(dropdownButtonClassName)));
        driver.findElements(By.className(findButtonClassName))
                .forEach(s -> {if (s.getText().equals("Найти"))findButton = s;});
        setTextFieldElements(driver);
    }

    public void setTextFieldElements(WebDriver driver){
        List<WebElement> elementList = driver.findElements(By.className(CADASTRE_AND_STREET_ELEMENTS_CLASS));
        int maxWidth = Integer.parseInt(elementList.get(0).getAttribute("style").replaceAll("[\\D.]", ""));
        if(maxWidth > Integer.parseInt(elementList.get(1).getAttribute("style").replaceAll("[\\D.]", ""))){
            cadastreNums = elementList.get(0);
            streetName = elementList.get(1);
        } else {
            cadastreNums = elementList.get(1);
            streetName = elementList.get(0);
        }
    }

    public void setCadastreNums(String cadastreNumVal){
        cadastreNums.sendKeys(cadastreNumVal);
    }

    public void setStreetName(String streetNameVal){
        streetName.sendKeys(streetNameVal);
    }

    public WebElement getRegion() {
        return region;
    }

    public void setRegion(String region) throws InterruptedException{
        WebElement dropdownButton = driver.findElements(By.className(dropdownButtonClassName)).get(0);
        dropdownButton.click();
        selectDropdown(region);
        this.region = driver.findElement(By.className("v-filterselect-input"));
    }

    private void selectDropdown(String value){
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.className(dropdownClassName)));
        WebElement table = driver.findElement(By.className(dropdownClassName));
        WebElement next = driver.findElement(By.xpath("//span[contains(text(), 'Next')]"));
        List<WebElement> tableRows = table.findElement(By.tagName("table")).findElements(By.tagName("tr"));
        Iterator iterator = tableRows.iterator();
        while (iterator.hasNext()){
            WebElement element = (WebElement) iterator.next();
            if(element.getText().equals(value)){
                element.click();
                return;
            }
        }
        try{
            next.click();
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (Exception e){
            return;
        }
        selectDropdown(value);
    }

    private void pushFind(){
        findButton.click();
    }
}
