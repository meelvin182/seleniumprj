package ru.sokolov;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.model.RequestEntity;
import ru.sokolov.model.pages.AbstractPage;
import ru.sokolov.model.pages.RequestOverviewPage;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public final class CoreKernelSupaClazz {

    private static final ReentrantLock checkrequestsLock = new ReentrantLock();
    private static Thread requestsChecker;

    private static WebDriver driver;
    private static final String MAIN_PAGE = "https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn";

    static {
        //TODO Make setProperty work properly both in jar and IDE
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe");
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

    public static void checkForProcessedRequests() {
        checkrequestsLock.lock();
        //TODO Add logic here
        checkrequestsLock.unlock();
    }

    public static void sendRequest(RequestEntity entity) throws Exception {
        checkrequestsLock.lock();
        driver.navigate().to(MAIN_PAGE);
        RequestOverviewPage.sendRequest(entity);
        checkrequestsLock.unlock();
    }

    public static void peformPut(String URI, String body) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPut putRequest = new HttpPut(URI);
            StringEntity input = new StringEntity(body);
            input.setContentType("application/json");

            putRequest.setEntity(input);
            HttpResponse response = httpClient.execute(putRequest);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //TODO This one will close program if it's unpaid
    public static void twentyThousandsMethod() {
    }

}