package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.model.entities.RequestEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchObjectsPage extends AbstractPage {

    public final static String CADASTRE_AND_STREET_ELEMENTS_CLASS = "v-textfield-prompt";
    public final static String dropdownClassName = "v-filterselect-suggestmenu";
    public final static String dropdownButtonClassName = "v-filterselect-button";
    public final static String findButtonClassName = "v-button-caption";
    public final static String statusElementClassName = "v-filterselect-status";

    private static WebElement cadastreNums;
    private static WebElement streetName;
    //getAttribute("value") should be used for this one, to retrieve it's text
    private static WebElement region;
    private static WebElement findButton;

    private static int regionsAmountOnDropdown;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchObjectsPage.class);


    public static void setPageData(RequestEntity entity) {
        waitForPageLoad(driver);
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(dropdownButtonClassName)));
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(findButtonClassName)));
        driver.findElements(By.className(findButtonClassName))
                .forEach(s -> {
                    if (s.getText().equals("Найти")) findButton = s;
                });
        setTextFieldElements();
        LOGGER.info("Setting Cadastre Num: {}", entity.getCadastreNums());
        setCadastreNums(entity.getCadastreNums());
        LOGGER.info("Setting Region: {}", entity.getRegion());
        setRegion(entity.getRegion());
        LOGGER.info("Everything is set");
    }

    public static void setTextFieldElements() {
        LOGGER.info("Waiting FOR ELEMENTS LOCATED");
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
        LOGGER.info("ELEMENTS LOCATED");
    }

    public static void setCadastreNums(String cadastreNumVal) {
        cadastreNums.clear();
        cadastreNums.sendKeys(Keys.ENTER);
        cadastreNums.sendKeys(cadastreNumVal);
    }

    public void setStreetName(String streetNameVal) {
        streetName.sendKeys(streetNameVal);
    }

    public WebElement getRegion() {
        return region;
    }

    public static void setRegion(String region) {
        LOGGER.info("WAITING FOR DROPDOWN IS OPEN");
        driverWait.until(ExpectedConditions.elementToBeClickable(By.className(dropdownButtonClassName)));
        WebElement dropdownButton = driver.findElements(By.className(dropdownButtonClassName)).get(0);
        dropdownButton.click();
        selectDropdown(region);
    }

    private static void selectDropdown(String value) {
        LOGGER.info("WAITING FOR NEXT 10 LOCATED");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(statusElementClassName)));
        WebElement element = driver.findElement(By.className(statusElementClassName));
        regionsAmountOnDropdown = Integer.parseInt(element.getText().split("/")[1]);
        selectDropdown(value, 0);
    }

    private static void selectDropdown(String value, int startnum) {
        int endnum = regionsAmountOnDropdown - startnum >= 10 ? startnum + 9 : regionsAmountOnDropdown;
        String name = (startnum == 0 ? 1 : startnum) + "-" + endnum;
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className(dropdownClassName)));
        WebElement table = driver.findElement(By.className(dropdownClassName));
        WebElement next = driver.findElement(By.xpath("//span[contains(text(), 'Next')]"));
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '" + name + "')]")));
        List<WebElement> tableRows = table.findElement(By.tagName("table")).findElements(By.tagName("tr"));
        for(WebElement element : tableRows){
            if(element.getText().toLowerCase().equals(value.toLowerCase())){
                LOGGER.info("CHOOSING: {}", element.getText());
                element.click();
                return;
            }
        }
        next.click();
        selectDropdown(value, startnum + 10);

    }

    public static void pushFind() {
        findButton.click();
    }

    public static void sendRequest(RequestEntity entity) throws Exception{
        SecondPage.search(entity);
        setPageData(entity);
        TimeUnit.MILLISECONDS.sleep(300);
        pushFind();
        System.out.println("Request sent");
    }


    public static WebElement getCadastreNums() {
        return cadastreNums;
    }

    public static void setCadastreNums(WebElement cadastreNums) {
        SearchObjectsPage.cadastreNums = cadastreNums;
    }

    public static WebElement getStreetName() {
        return streetName;
    }

    public static void setStreetName(WebElement streetName) {
        SearchObjectsPage.streetName = streetName;
    }

    public static WebElement getFindButton() {
        return findButton;
    }

    public static void setFindButton(WebElement findButton) {
        SearchObjectsPage.findButton = findButton;
    }

    public int getRegionsAmountOnDropdown() {
        return regionsAmountOnDropdown;
    }

    public void setRegionsAmountOnDropdown(int regionsAmountOnDropdown) {
        this.regionsAmountOnDropdown = regionsAmountOnDropdown;
    }
}
