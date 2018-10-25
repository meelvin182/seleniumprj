package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.sokolov.model.entities.RequestEntity;

public class RequestOverviewPage extends AbstractPage{

    private static final String REQUEST_RIGHTS_CHECKBOX = "Запросить сведения о переходе прав на объект";
    private static final String SEND_REQUEST_BUTTON = "Отправить запрос";
    private static final String SIGN_AND_SEND_BUTTON = "Подписать и отправить запрос";

    public static void setPageData(RequestEntity entity){
        waitForPageLoad(driver);
        System.out.println("Checking whick checkbox to check");
        if(entity.isGetChangeRightsInfo()){
            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ REQUEST_RIGHTS_CHECKBOX +"')]")));
            driver.findElement(By.xpath("//*[contains(text(), '"+ REQUEST_RIGHTS_CHECKBOX +"')]")).click();
        }
    }

    public static void sendRequest(RequestEntity entity) throws Exception{
        RequestsPage.continueToRequestOverview(entity);
        setPageData(entity);
        System.out.println("Waiting for final send button");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")));
        System.out.println("Sent.");
        driver.findElement(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")).click();

    }
}
