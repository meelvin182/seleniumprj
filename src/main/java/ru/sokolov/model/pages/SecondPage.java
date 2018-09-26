package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SecondPage {

    public static final String SECOND_PAGE_ELEMENTS_CLASS = "v-button-caption";
    public static final String SEARCH_BUTTON_ELEMENT_NAME =  "Поиск объектов недвижимости";
    public static final String RIGHTHOLDER_REQUEST_BUTTON_ELEMENT_NAME = "Запрос по правообладателю";
    public static final String MY_REQUESTS_BUTTON_ELEMENT_NAME = "Мои заявки";
    public static final String MY_BILLS_BUTTON_ELEMENT_NAME = "Мои счета";

    private WebElement searchButton;
    private WebElement rightHolderRequestButton;
    private WebElement myRequestsButton;
    private WebElement myBillsButton;

    public SecondPage(WebDriver driver) {
        for(WebElement element : driver.findElements(By.className(SECOND_PAGE_ELEMENTS_CLASS))){
            if(SEARCH_BUTTON_ELEMENT_NAME.equals(element.getText())){
                searchButton = element;
            } else if (RIGHTHOLDER_REQUEST_BUTTON_ELEMENT_NAME.equals(element.getText())){
                rightHolderRequestButton = element;
            } else if (MY_REQUESTS_BUTTON_ELEMENT_NAME.equals(element.getText())){
                myRequestsButton = element;
            } else if (MY_BILLS_BUTTON_ELEMENT_NAME.equals(element.getText())) {
                myBillsButton = element;
            }
        }
    }

    public void openSearchParams(){
        searchButton.click();
    }

    public void openRightHolderRequestPage(){
        rightHolderRequestButton.click();
    }

    public void openMyRequestsPage(){
        myBillsButton.click();
    }

    public void openMyBillsPage(){
        myBillsButton.click();
    }
}
