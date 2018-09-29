package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.model.RequestEntity;

import java.util.List;

public class SearchObjectsPage extends AbstractPage {

    public final static String CADASTRE_AND_STREET_ELEMENTS_CLASS = "v-textfield-prompt";
    public final static String dropdownClassName = "v-filterselect-suggestmenu";
    public final static String dropdownButtonClassName = "v-filterselect-button";
    public final static String findButtonClassName = "v-button-caption";
    public final static String statusElementClassName = "v-filterselect-status";

    private WebElement cadastreNums;
    private WebElement streetName;
    //getAttribute("value") should be used for this one, to retrieve it's text
    private WebElement region;
    private WebElement findButton;

    private int regionsAmountOnDropdown;

    @Override
    public SearchObjectsPage setPageData(WebDriver driver, RequestEntity entity) {
        this.entity = entity;
        waitForPageLoad(driver);
        driverWait = new WebDriverWait(driver, 2000);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(dropdownButtonClassName)));
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(findButtonClassName)));
        driver.findElements(By.className(findButtonClassName))
                .forEach(s -> {
                    if (s.getText().equals("Найти")) findButton = s;
                });
        setTextFieldElements(driver);
        setCadastreNums(this.entity.getCadastreNums());
        setRegion(this.entity.getRegion());
        return this;
    }

    public void setTextFieldElements(WebDriver driver) {
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(CADASTRE_AND_STREET_ELEMENTS_CLASS)));
        List<WebElement> elementList = driver.findElements(By.className(CADASTRE_AND_STREET_ELEMENTS_CLASS));
        int maxWidth = Integer.parseInt(elementList.get(0).getAttribute("style").replaceAll("[\\D.]", ""));
        if (maxWidth > Integer.parseInt(elementList.get(1).getAttribute("style").replaceAll("[\\D.]", ""))) {
            cadastreNums = elementList.get(0);
            streetName = elementList.get(1);
        } else {
            cadastreNums = elementList.get(1);
            streetName = elementList.get(0);
        }
    }

    public void setCadastreNums(String cadastreNumVal) {
        cadastreNums.sendKeys(cadastreNumVal);
    }

    public void setStreetName(String streetNameVal) {
        streetName.sendKeys(streetNameVal);
    }

    public WebElement getRegion() {
        return region;
    }

    public void setRegion(String region) {
        driverWait.until(ExpectedConditions.elementToBeClickable(By.className(dropdownButtonClassName)));
        WebElement dropdownButton = driver.findElements(By.className(dropdownButtonClassName)).get(0);
        dropdownButton.click();
        selectDropdown(region);
        this.region = driver.findElement(By.className("v-filterselect-input"));
    }

    private void selectDropdown(String value) {
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(statusElementClassName)));
        WebElement element = driver.findElement(By.className(statusElementClassName));
        regionsAmountOnDropdown = Integer.parseInt(element.getText().split("/")[1]);
        selectDropdown(value, 0);
    }

    private void selectDropdown(String value, int startnum) {
        int endnum = regionsAmountOnDropdown - startnum >= 10 ? startnum + 9 : regionsAmountOnDropdown;
        String name = (startnum == 0 ? 1 : startnum) + "-" + endnum;
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(dropdownClassName)));
        WebElement table = driver.findElement(By.className(dropdownClassName));
        WebElement next = driver.findElement(By.xpath("//span[contains(text(), 'Next')]"));
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '" + name + "')]")));
        List<WebElement> tableRows = table.findElement(By.tagName("table")).findElements(By.tagName("tr"));
        for(WebElement element : tableRows){
            if(element.getText().equals(value)){
                element.click();
                return;
            }
        }
        next.click();
        selectDropdown(value, startnum + 10);

    }

    public void pushFind() {
        findButton.click();
    }
}
