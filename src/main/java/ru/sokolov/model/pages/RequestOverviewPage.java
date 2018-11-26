package ru.sokolov.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.CaptchaCoordsProvider;
import ru.sokolov.CoreKernelSupaClazz;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.exceptions.WrongCadastreNumException;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.sokolov.CoreKernelSupaClazz.APPDATA_PATH;
import static ru.sokolov.gui.RequestPopup.NOT_FOUND;
import static ru.sokolov.gui.RequestPopup.SENT;
import static ru.sokolov.model.pages.SentSuccesfullyPage.POPUP_CLASS_NAME;

public class RequestOverviewPage extends AbstractPage{

    private static final String REQUEST_RIGHTS_CHECKBOX = "Запросить сведения о переходе прав на объект";
    private static final String SEND_REQUEST_BUTTON = "Отправить запрос";
    private static final String SIGN_AND_SEND_BUTTON = "Подписать и отправить запрос";
    private static final String NEXT_IMAGE = "Другую картинку";
    private static final String WRONG_CAPTCHA_CLASSNAME = "f-alert-content";

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
        for(Map.Entry<RequestEntity, SentRequest> requests : entities.entrySet()){
            RequestEntity entity = requests.getKey();
            SentRequest request = requests.getValue();
            try{
                RequestsPage.continueToRequestOverview(entity);
                setPageData(entity);
                LOGGER.info("Waiting for final send button");
                driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")));
                LOGGER.info("SOLVING CAPCHA");
                while(true){
                    Map.Entry<File, WebElement> entry = downloadCaptcha();
                    String sovedCaptcha = CoreKernelSupaClazz.solveCapcha(entry.getKey());
                    LOGGER.info("CAPTCHA SOLVED AS: {}", sovedCaptcha);
                    WebElement textField = driver.findElement(By.className("v-textfield"));
                    textField.sendKeys(sovedCaptcha);
                    textField.sendKeys(Keys.ENTER);
                    LOGGER.info("LOCATING {} BUTTON", SEND_REQUEST_BUTTON);
                    driver.findElement(By.xpath("//*[contains(text(), '"+ SEND_REQUEST_BUTTON +"')]")).click();
                    TimeUnit.MILLISECONDS.sleep(250);
                    LOGGER.info("WAIT UNTIL CAPTCHA NOT PRESENTED");
                    driverWait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.className(WRONG_CAPTCHA_CLASSNAME)),
                            ExpectedConditions.presenceOfElementLocated(By.className(POPUP_CLASS_NAME))));
                    LOGGER.info("CHECKING POPUP");
                    try{
                        driver.findElement(By.className(POPUP_CLASS_NAME)).getText();
                        LOGGER.info("Request sent");
                        break;
                    } catch (NoSuchElementException e){
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                }
                request.setRequestNum(SentSuccesfullyPage.getRequestNum());
                request.setStatus(SENT);
                LOGGER.info("SWITCHING TO RIGHTHOLDER");
                SecondPage.openRhldr();
            } catch (WrongCadastreNumException e){
                System.out.println("Nothing found for Cadastre number: " + entity.getCadastreNums());
                request.setStatus(NOT_FOUND);
            }
            LOGGER.info("SWITCHING TO RIGHTHOLDER");
            SecondPage.openRhldr();
        }
    }

    public static void nextCaptha(){
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), '" + NEXT_IMAGE + "')]")));
        driver.findElement(By.xpath("//*[contains(text(), '" + NEXT_IMAGE + "')]")).click();
    }

    public static Map.Entry<File, WebElement> downloadCaptcha() throws Exception{
        String images_folder = APPDATA_PATH + "\\images";
        File folder = new File(images_folder);
        if (!folder.exists()){
            folder.mkdir();
        }
        WebElement image = driver.findElements(By.className("v-embedded-image")).get(1).findElement(By.tagName("img")); /**/
        LOGGER.info("Waiting until captcha is presented");
        driverWait.until(ExpectedConditions.visibilityOf(image));
        AShot ashot = new AShot();
        ashot.shootingStrategy(ShootingStrategies.viewportPasting(100));
        Screenshot shot = ashot.coordsProvider(new CaptchaCoordsProvider()).takeScreenshot(driver, image);
        File f = new File(images_folder + "\\captcha.png");
        ImageIO.write(shot.getImage(), "PNG", f);
        return new AbstractMap.SimpleEntry<>(f, image);
    }

}
