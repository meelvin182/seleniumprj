package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.exceptions.WrongCadastreNumException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public static List<List<LoginEntity>> sendReuests(List<RequestEntity> entities) throws Exception{
        List<LoginEntity> notFound = new ArrayList<>();
        List<LoginEntity> successfully = new ArrayList<>();
        for(RequestEntity entity : entities){
            try{
                RequestsPage.continueToRequestOverview(entity);
                setPageData(entity);
                System.out.println("Waiting for final send button");
                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")));
                System.out.println("Sent.");
                driver.findElement(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")).click();
                SentRequest request = new SentRequest(entity);
                request.setRequestNum(SentSuccesfullyPage.getRequestNum());
                successfully.add(request);
                SecondPage.openRhldr();
                TimeUnit.MILLISECONDS.sleep(5000);
            } catch (WrongCadastreNumException e){
                System.out.println("Nothing found for Cadastre number: " + entity.getCadastreNums());
                notFound.add(entity);
            }
            System.out.println("CLICKING RIGHTHOLDER");
            SecondPage.openRhldr();
            TimeUnit.MILLISECONDS.sleep(500);
        }
        List<List<LoginEntity>> both = new ArrayList<>();
        both.add(notFound);
        both.add(successfully);
        return both;
    }
}
