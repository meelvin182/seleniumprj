package ru.sokolov.model.pages;

import jdk.nashorn.internal.runtime.regexp.joni.constants.TargetInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SecondPage extends LoginPage{

    public static final String SECOND_PAGE_ELEMENTS_CLASS = "v-button-caption";
    public static final String SEARCH_BUTTON_ELEMENT_NAME =  "Поиск объектов недвижимости";
    public static final String RIGHTHOLDER_REQUEST_BUTTON_ELEMENT_NAME = "Запрос по правообладателю";
    public static final String MY_REQUESTS_BUTTON_ELEMENT_NAME = "Мои заявки";
    public static final String MY_BILLS_BUTTON_ELEMENT_NAME = "Мои счета";

    private static WebElement searchButton;
    private static WebElement rightHolderRequestButton;
    private static WebElement myRequestsButton;
    private static WebElement myBillsButton;

    public static void setPageData() {
        waitForPageLoad(driver);
        waitForButtonsLoaded();
        System.out.println("Second page is loaded");
        for(WebElement element : driver.findElements(By.className(SECOND_PAGE_ELEMENTS_CLASS))){
            if(SEARCH_BUTTON_ELEMENT_NAME.equals(element.getText())){
                setSearchButton(element);
            } else if (RIGHTHOLDER_REQUEST_BUTTON_ELEMENT_NAME.equals(element.getText())){
                setRightHolderRequestButton(element);
            } else if (MY_REQUESTS_BUTTON_ELEMENT_NAME.equals(element.getText())){
                setMyRequestsButton(element);
            } else if (MY_BILLS_BUTTON_ELEMENT_NAME.equals(element.getText())) {
                setMyBillsButton(element);
            }
        }
        System.out.println("Buttons found");
    }

    public static WebElement getSearchButton() {
        return searchButton;
    }

    public static void setSearchButton(WebElement searchButton) {
        SecondPage.searchButton = searchButton;
    }

    public static WebElement getRightHolderRequestButton() {
        return rightHolderRequestButton;
    }

    public static void setRightHolderRequestButton(WebElement rightHolderRequestButton) {
        SecondPage.rightHolderRequestButton = rightHolderRequestButton;
    }

    public static WebElement getMyRequestsButton() {
        return myRequestsButton;
    }

    public static void setMyRequestsButton(WebElement myRequestsButton) {
        SecondPage.myRequestsButton = myRequestsButton;
    }

    public static WebElement getMyBillsButton() {
        return myBillsButton;
    }

    public static void setMyBillsButton(WebElement myBillsButton) {
        SecondPage.myBillsButton = myBillsButton;
    }

    public static void waitForButtonsLoaded(){
        Iterator<String> iterator = Arrays.stream(new String[]{
                SEARCH_BUTTON_ELEMENT_NAME,
                RIGHTHOLDER_REQUEST_BUTTON_ELEMENT_NAME,
                MY_REQUESTS_BUTTON_ELEMENT_NAME,
                MY_BILLS_BUTTON_ELEMENT_NAME}).collect(Collectors.toList()).iterator();
        while (iterator.hasNext()){
            String name = iterator.next();
            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ name +"')]")));
        }
    }

    public static void openSearchParams(){
        searchButton.click();
    }

    public static void openRequestsPage(){
        myRequestsButton.click();
    }

    public static void openRhldr(){
        setPageData();
        rightHolderRequestButton.click();
    }

    public static void search(RequestEntity entity) throws Exception{
        driverWait = new WebDriverWait(driver, 60);
        waitForPageLoad(driver);
        setPageData();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Opening request params");
        openSearchParams();
    }

    public static void openRequests(LoginEntity entity) throws Exception{
        waitForPageLoad(driver);
        driverWait = new WebDriverWait(driver, 180);
        waitForPageLoad(driver);
        setPageData();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Openning My Requests Page");
        openRequestsPage();
    }
}
