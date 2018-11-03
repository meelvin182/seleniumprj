package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.exceptions.WrongCadastreNumException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.sokolov.gui.RequestPopup.SENT;

public class RequestOverviewPage extends AbstractPage{

    private static final String REQUEST_RIGHTS_CHECKBOX = "Запросить сведения о переходе прав на объект";
    private static final String SEND_REQUEST_BUTTON = "Отправить запрос";
    private static final String SIGN_AND_SEND_BUTTON = "Подписать и отправить запрос";

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestOverviewPage.class);

    public static void setPageData(RequestEntity entity){
        waitForPageLoad(driver);
        LOGGER.info("Checking whick checkbox to check");
        if(entity.isGetChangeRightsInfo()){
            LOGGER.info("WAITING FOR CHECKBOX WITH TEXT {} LOCATED", REQUEST_RIGHTS_CHECKBOX);
            driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ REQUEST_RIGHTS_CHECKBOX +"')]")));
            LOGGER.info("{} LOCATED, CHOOSING", REQUEST_RIGHTS_CHECKBOX);
            driver.findElement(By.xpath("//*[contains(text(), '"+ REQUEST_RIGHTS_CHECKBOX +"')]")).click();
        }
    }

    public static void sendRequest(RequestEntity entity) throws Exception{
        RequestsPage.continueToRequestOverview(entity);
        setPageData(entity);
        LOGGER.info("Waiting for final send button");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")));
        LOGGER.info("Sent.");
        driver.findElement(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")).click();
    }

    public static void sendReuests(Map<RequestEntity, SentRequest> entities) throws Exception{
        LOGGER.info("SENDING {} REQUESTS", entities.size());
        List<LoginEntity> notFound = new ArrayList<>();
        List<LoginEntity> successfully = new ArrayList<>();
        for(Map.Entry<RequestEntity, SentRequest> requests : entities.entrySet()){
            RequestEntity entity = requests.getKey();
            SentRequest request = requests.getValue();
            try{
                RequestsPage.continueToRequestOverview(entity);
                setPageData(entity);
                LOGGER.info("Waiting for final send button");
                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")));
                LOGGER.info("LOCATING {} BUTTON", SEND_REQUEST_BUTTON);
                driver.findElement(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")).click();
                LOGGER.info("Request sent");
                request.setRequestNum(SentSuccesfullyPage.getRequestNum());
                request.setStatus(SENT);
                successfully.add(request);
                LOGGER.info("SWITCHING TO RIGHTHOLDER");
                SecondPage.openRhldr();
            } catch (WrongCadastreNumException e){
                System.out.println("Nothing found for Cadastre number: " + entity.getCadastreNums());
                notFound.add(entity);
            }
            LOGGER.info("SWITCHING TO RIGHTHOLDER");
            SecondPage.openRhldr();
        }
    }
}
