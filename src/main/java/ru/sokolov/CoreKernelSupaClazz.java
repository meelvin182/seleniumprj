package ru.sokolov;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.gui.RequestPopup;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.pages.AbstractPage;
import ru.sokolov.model.pages.AllRequestsPage;
import ru.sokolov.model.pages.RequestOverviewPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class CoreKernelSupaClazz {

    private static final ReentrantLock checkrequestsLock = new ReentrantLock();
    private static Thread requestsChecker;

    private static WebDriver driver;
    private static final String MAIN_PAGE = "https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn";
    public static boolean driverLoaded = loadDriver();


    static {
        //TODO Make setProperty work properly both in jar and IDE
        driver = new ChromeDriver();
        AbstractPage.setDriver(driver);
        requestsChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkForProcessedRequests();
                    }
                }, 0, 1000 * 60 * 30); //Once per 30 minutes
            }
        });
        requestsChecker.setDaemon(true);
    }

    public static void checkForProcessedRequests(){
        checkrequestsLock.lock();
        //Stub login entity
        RequestEntity entity = new RequestEntity();
        entity.setKeyParts(Arrays.stream("f5939ffe-f955-421a-b30b-884a5c527803".split("-")).collect(Collectors.toList()));
        try {
            getRequests(entity);
        } catch (Exception e){
            System.out.println(e);
        }
        checkrequestsLock.unlock();
    }

    public static void sendRequest(RequestEntity entity) throws Exception{
        checkrequestsLock.lock();
        driver.navigate().to(MAIN_PAGE);
        RequestOverviewPage.sendRequest(entity);
        checkrequestsLock.unlock();
    }

    public static void getRequests(RequestEntity entity) throws Exception{
        driver.navigate().to(MAIN_PAGE);
        AllRequestsPage.process(entity);
    }

    private static boolean loadDriver(){
        try{
            File temp = File.createTempFile("driver", ".exe");
            temp.deleteOnExit();
            InputStream in = RequestPopup.class.getResourceAsStream("/chromedriver.exe");
            Files.copy(in, Paths.get(temp.toURI()), StandardCopyOption.REPLACE_EXISTING);
            System.setProperty("webdriver.chrome.driver", temp.getAbsolutePath());
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    //TODO This one will close program if it's unpaid
    public static void twentyThousandsMethod(){}

}
