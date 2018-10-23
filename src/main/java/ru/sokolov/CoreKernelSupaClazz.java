package ru.sokolov;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class CoreKernelSupaClazz {

    private static final ReentrantLock checkrequestsLock = new ReentrantLock();
    private static Thread requestsChecker;
    private static final String APPDATA_PATH = System.getenv("APPDATA") + "\\egrn";
    private static final String APPDATA_TMP_PATH = APPDATA_PATH + "\\tmp";
    public static final String TEST_KEY = "f5939ffe-f955-421a-b30b-884a5c527803";
    public static final String TEST_CADASTRE_NUM = "50:27:0040215:179";

    private static WebDriver driver;
    public static final String MAIN_PAGE = "https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn";
    private static ObjectMapper mapper = new ObjectMapper();
    public static boolean driverLoaded = loadDriver();


    static {
        File file = new File(APPDATA_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
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
        initDriver(null);
        RequestEntity entity = new RequestEntity();
        entity.setKeyParts(Arrays.stream(TEST_KEY.split("-")).collect(Collectors.toList()));
        try {
            getRequests(entity);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        AbstractPage.driver.close();
        checkrequestsLock.unlock();
    }

    public static SentRequest sendRequest(RequestEntity entity) throws Exception {
        checkrequestsLock.lock();
        initDriver(null);
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
        initDriver(null);
        try{
            AbstractPage.driver.navigate().to(MAIN_PAGE);
            AllRequestsPage.process(entity);
        } catch (Exception e){
            e.printStackTrace(System.out);
        } finally {
            AbstractPage.driver.close();
            checkrequestsLock.unlock();
        }
    }

    public static void downloadRequest(SentRequest request) throws Exception
    {
        checkrequestsLock.lock();
        String downloadDir = APPDATA_TMP_PATH + "\\" + request.getRequestNum();
        File tmpDir = null;
        tmpDir = new File(downloadDir);
        if(!tmpDir.exists()){
            tmpDir.mkdir();
        }
        Map<String, Object> preferences = new Hashtable<String, Object>();
        preferences.put("profile.default_content_settings.popups", 0);
        preferences.put("download.prompt_for_download", "false");
        preferences.put("download.default_directory", tmpDir.getPath());
        initDriver(preferences);

        try {
            AbstractPage.driver.navigate().to(MAIN_PAGE);
            AllRequestsPage.downloadRequest(request);
            TimeUnit.SECONDS.sleep(2);
            AbstractPage.driver.close();

            ZipFile zipFile = new ZipFile(getFilesInDir(downloadDir).get(0).getPath());
            File unzippedZipFile = unzipSpecificExtension("zip", zipFile, request.getRequestNum(), downloadDir);
            unzipSpecificExtension("xml", new ZipFile(unzippedZipFile.getPath()), unzippedZipFile.getName().replaceAll(".zip", ""), request.getPath());
            FileUtils.deleteDirectory(tmpDir);
        } catch (Exception e){
            throw e;
        } finally {
            checkrequestsLock.unlock();
        }
    }

    private static File unzipSpecificExtension(String ext, ZipFile zipFile, String name, String folder) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(ext.equals(entry.getName().substring(entry.getName().length() - ext.length()))){
                File tmpFile = new File(folder + "\\" + name +"." + ext);
                InputStream in = zipFile.getInputStream(entry);
                OutputStream outputStream = new FileOutputStream(tmpFile);
                IOUtils.copy(in, outputStream);
                zipFile.close();
                outputStream.close();
                in.close();
                return tmpFile;
            }
        }
        return null;
    }

    public static void updateRequestsStatus(List<SentRequest> requests) throws Exception{
        checkrequestsLock.lock();
        initDriver(null);
        try {
            AbstractPage.driver.navigate().to(MAIN_PAGE);
            AllRequestsPage.updateRequestsStatus(requests);
        } catch (Exception e){
            e.printStackTrace(System.out);
        } finally {
            AbstractPage.driver.close();
            checkrequestsLock.unlock();
        }
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
                .append("_")
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

    public static List<SentRequest> readAllRequests() throws IOException {
        List<SentRequest> requests = new ArrayList<>();
        for (File file : getFilesInDir(APPDATA_PATH)) {
            requests.add(mapper.readValue(file, SentRequest.class));
        }
        return requests;
    }

    private static List<File> getFilesInDir(String dir) throws IOException{
        return Files.walk(Paths.get(dir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    private static void initDriver(Map<String, Object> preferences){
        if (driverLoaded) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            if(preferences != null){
                chromeOptions.setExperimentalOption("prefs", preferences);
            }
            WebDriver webDriver = new ChromeDriver(chromeOptions);
            driver = webDriver;
            AbstractPage.setDriver(webDriver);
        } else {
            driverLoaded = loadDriver();
            initDriver(preferences);
        }
    }

    //TODO This one will close program if it's unpaid
    public static void twentyThousandsMethod() {
    }

}
