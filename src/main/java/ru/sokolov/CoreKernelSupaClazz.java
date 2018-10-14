package ru.sokolov;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.sokolov.gui.RequestPopup;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.pages.AbstractPage;
import ru.sokolov.model.pages.AllRequestsPage;
import ru.sokolov.model.pages.RequestOverviewPage;
import ru.sokolov.model.pages.SentSuccesfullyPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class CoreKernelSupaClazz {

    private static final ReentrantLock checkrequestsLock = new ReentrantLock();
    private static Thread requestsChecker;
    private static final String APPDATA_PATH = System.getenv("APPDATA") + "\\egrn";

    private static WebDriver driver;
    public static final String MAIN_PAGE = "https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn";
    private static ObjectMapper mapper = new ObjectMapper();
    public static boolean driverLoaded = loadDriver();


    static {
        //TODO Make setProperty work properly both in jar and IDE
        File file = new File(APPDATA_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        File file1 = new File(APPDATA_PATH + "\\requests");
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

    //Unused atm
    @Deprecated
    public static void checkForProcessedRequests() {
        checkrequestsLock.lock();
        if (driverLoaded) {
            AbstractPage.setDriver(new ChromeDriver());
        }
        RequestEntity entity = new RequestEntity();
        entity.setKeyParts(Arrays.stream("f5939ffe-f955-421a-b30b-884a5c527803".split("-")).collect(Collectors.toList()));
        try {
            getRequests(entity);
        } catch (Exception e) {
            System.out.println(e);
            AbstractPage.driver.close();
        }
        AbstractPage.driver.close();
        checkrequestsLock.unlock();
    }

    public static SentRequest sendRequest(RequestEntity entity) throws Exception {
        checkrequestsLock.lock();
        if (driverLoaded) {
            AbstractPage.setDriver(new ChromeDriver());
            driver = AbstractPage.driver;
        }
        driver.navigate().to(MAIN_PAGE);
        RequestOverviewPage.sendRequest(entity);
        SentRequest request = new SentRequest(entity);
        request.setRequestNum(SentSuccesfullyPage.getRequestNum());
        driver.close();
        checkrequestsLock.unlock();
        return saveRequestToJson(request);
    }

    public static void getRequests(RequestEntity entity) throws Exception {
        checkrequestsLock.lock();
        if (driverLoaded) {
            AbstractPage.setDriver(new ChromeDriver());
        }
        AbstractPage.driver.navigate().to(MAIN_PAGE);
        AllRequestsPage.process(entity);
        AbstractPage.driver.close();
        checkrequestsLock.unlock();
    }

    public static void downloadRequest(SentRequest request) throws Exception
    {
        checkrequestsLock.lock();
        if (driverLoaded) {
            ChromeOptions chromeOptions = new ChromeOptions();
            Map<String, Object> preferences = new Hashtable<String, Object>();
            preferences.put("profile.default_content_settings.popups", 0);
            preferences.put("download.prompt_for_download", "false");
            preferences.put("download.default_directory", request.getPath());

            chromeOptions.setExperimentalOption("prefs", preferences);
            WebDriver driver = new ChromeDriver(chromeOptions);
            AbstractPage.setDriver(driver);
        }
        AbstractPage.driver.navigate().to(MAIN_PAGE);
        AllRequestsPage.downloadRequest(request);
        AbstractPage.driver.close();
        checkrequestsLock.unlock();
    }

    public static void updateRequestsStatus(List<SentRequest> requests) throws Exception{
        checkrequestsLock.lock();
        if (driverLoaded) {
            AbstractPage.setDriver(new ChromeDriver());
        }
        AbstractPage.driver.navigate().to(MAIN_PAGE);
        AllRequestsPage.updateRequestsStatus(requests);
        AbstractPage.driver.close();
        checkrequestsLock.unlock();
    }

    private static boolean loadDriver() {
        try {
            File temp = File.createTempFile("driver", ".exe");
            temp.deleteOnExit();
            InputStream in = RequestPopup.class.getResourceAsStream("/chromedriver.exe");
            Files.copy(in, Paths.get(temp.toURI()), StandardCopyOption.REPLACE_EXISTING);
            System.setProperty("webdriver.chrome.driver", temp.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static SentRequest saveRequestToJson(SentRequest request) throws Exception {
        StringBuilder builder = new StringBuilder();
        String fileName = builder
                .append("\\")
                .append(request.getRequestNum())
                .append("")
                .append(request.getCreationDate())
                .append(".json").toString()
                .replaceAll("/", "_")
                .replaceAll(" ", "_")
                .replaceAll(":", "_");
        String path = APPDATA_PATH + fileName;
        File json = new File(path);
        json.createNewFile();
        mapper.writeValue(json, request);
        return request;
    }

    //Unused atm
    @Deprecated
    public static List<SentRequest> readAllRequests() throws IOException {
        List<File> filesInFolder = Files.walk(Paths.get(APPDATA_PATH))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        List<SentRequest> requests = new ArrayList<>();
        for (File file : filesInFolder) {
            requests.add(mapper.readValue(file, SentRequest.class));
        }
        return requests;
    }

    //TODO This one will close program if it's unpaid
    public static void twentyThousandsMethod() {
    }

}
