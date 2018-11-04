package ru.sokolov;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sokolov.gui.MainScreen;
import ru.sokolov.gui.RequestPopup;
import ru.sokolov.model.entities.LoginEntity;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.pages.AbstractPage;
import ru.sokolov.model.pages.AllRequestsPage;
import ru.sokolov.model.pages.LoginPage;
import ru.sokolov.model.pages.RequestOverviewPage;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static ru.sokolov.gui.MainScreen.DOWNLOADED_STATUS;
import static ru.sokolov.gui.MainScreen.downloadFirefox;

public final class CoreKernelSupaClazz {

    private static final String APPDATA_PATH = System.getenv("APPDATA") + "\\egrn";
    private static final String APPDATA_TMP_PATH = APPDATA_PATH + "\\tmp";
    private static final String SAVED_KEY_PATH = APPDATA_PATH + "\\saved_key.txt";
    private static final String LOGS_PATH = APPDATA_PATH + "\\logs";
    public static final String MAIN_PAGE = "https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn";

    public static final String TEST_KEY = "f5939ffe-f955-421a-b30b-884a5c527803";
    public static final String TEST_CADASTRE_NUM = "50:27:0040215:179;77:22:0040215;50:21:0000000:5";

    private static WebDriver driver;
    private static ObjectMapper mapper = new ObjectMapper();
    private static Thread requestsChecker;
    private static FirefoxProfile profile = new FirefoxProfile();
    private static FirefoxOptions options = new FirefoxOptions();
    private static final Logger LOGGER;

    public static boolean driverLoaded = loadDriver();

    static {
        File file = new File(APPDATA_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        File tmpDir = new File(APPDATA_TMP_PATH);
        if(!tmpDir.exists()){
            tmpDir.mkdir();
        }
        System.setProperty("logs.path", LOGS_PATH);
        LOGGER = LoggerFactory.getLogger(CoreKernelSupaClazz.class);

        //Set Location to store files after downloading.
        profile.setPreference("browser.download.dir", tmpDir.getPath());
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.panel.shown", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/x-gzip");
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/zip");
        profile.setPreference("browser.download.manager.showWhenStarting", false );
        profile.setPreference("pdfjs.disabled", true );

        options.setProfile(profile);
        options.setHeadless(false);

        if(SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_WINDOWS_10){
            String path = System.getenv("ProgramFiles") + "\\Mozilla Firefox\\firefox.exe";
            options.setBinary(new File(path).exists() ? path : path.replace("Program Files", "Program Files (x86)"));
        }

        try {
            options.getBinary();
        } catch (Exception e){
            Platform.runLater(() -> downloadFirefox.showAndWait());
        }

        requestsChecker = new Thread(() -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        updateRequestsStatus(MainScreen.table.getItems());
                    } catch (Exception e){
                        e.printStackTrace(System.out);
                    }
                }
            }, 0, 1000 * 60 * 30); //Once per 30 minutes
        });
        requestsChecker.setDaemon(true);
    }

    //Unused atm
    @Deprecated
    public static void checkForProcessedRequests() {
        RequestEntity entity = new RequestEntity();
        entity.setKeyParts(Arrays.stream(TEST_KEY.split("-")).collect(Collectors.toList()));
        try {
            getRequests(entity);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        closeDriver();;
    }
    public static void sendRequests(Map<RequestEntity, SentRequest> entities) throws Exception{
        if(entities.isEmpty()) return;
        initDriver(profile);
        LOGGER.info("Opening Main Page");
        driver.navigate().to(MAIN_PAGE);
        LoginPage.setPageData(entities.keySet().iterator().next());
        LoginPage.login();
        RequestOverviewPage.sendReuests(entities);
        driver.close();
    }

    @Deprecated
    public static void getRequests(RequestEntity entity) throws Exception {
        try{
            AllRequestsPage.process(entity);
        } catch (Exception e){
            e.printStackTrace(System.out);
        } finally {
            closeDriver();;
        }
    }

    public static void downloadRequest(SentRequest request) throws Exception
    {
        File file = new File(request.getPath() + "\\" + request.getRequestNum()+".xml");
        if(file.exists()){
            Desktop.getDesktop().open(file.getParentFile());
            return;
        }
        initDriver(profile);
        LOGGER.info("Opening Main Page");
        driver.navigate().to(MAIN_PAGE);
        LoginPage.setPageData(request);
        LoginPage.login();
        AllRequestsPage.downloadRequest(request);
        TimeUnit.SECONDS.sleep(3);
        closeDriver();

        try {
            unzipDownloadedRequest(request);
        } catch (Exception e){
            throw e;
        }
    }

    public static void unzipDownloadedRequest(SentRequest request) throws Exception{
        File tmpDir = new File(APPDATA_TMP_PATH);
        if(!tmpDir.exists()){
            tmpDir.mkdir();
        }
        try {
            ZipFile zipFile = new ZipFile(getFilesInDir(tmpDir.getPath()).get(0).getPath());
            File unzippedZipFile = unzipSpecificExtension("zip", zipFile, request.getRequestNum(), tmpDir.getPath());
            File unzippedUnzippedFIle = unzipSpecificExtension("xml", new ZipFile(unzippedZipFile.getPath()), unzippedZipFile.getName().replaceAll(".zip", ""), request.getPath());
            FileUtils.cleanDirectory(tmpDir);
            request.setStatus(DOWNLOADED_STATUS);
            Desktop.getDesktop().open(unzippedUnzippedFIle.getParentFile());
        } catch (Exception e){
            throw e;
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
        LOGGER.info("Started updating request statuses, requests to check amount: {}", requests.size());
        if(requests.isEmpty()) return;
        try {
            initDriver(profile);
            LOGGER.info("Opening Main Page");
            driver.navigate().to(MAIN_PAGE);
            LoginPage.setPageData(requests.get(0));
            LoginPage.login();
            AllRequestsPage.updateRequestsStatus(requests);
        } catch (Exception e){
            if(e.getMessage().contains("Cannot find firefox binary in PATH")){
                Platform.runLater(downloadFirefox::showAndWait);
            }
            e.printStackTrace(System.out);
        } finally {
            closeDriver();;
            LOGGER.info("Driver closed");
        }
    }

    private static boolean loadDriver() {
        try {
            File temp = File.createTempFile("driver", ".exe");
            temp.deleteOnExit();
            InputStream in = RequestPopup.class.getResourceAsStream("/geckodriver.exe");
            Files.copy(in, Paths.get(temp.toURI()), StandardCopyOption.REPLACE_EXISTING);
            System.setProperty("webdriver.gecko.driver", temp.getAbsolutePath());
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
        LOGGER.info("Reading all requests");
        List<SentRequest> requests = new ArrayList<>();
        for (File file : getFilesInDir(APPDATA_PATH, ".json")) {
            requests.add(mapper.readValue(file, SentRequest.class));
        }
        return requests;
    }

    public static List<SentRequest> readAllRequests(List<String> keyParts) throws IOException {
        LOGGER.info("Reading all requests");
        List<SentRequest> requests = new ArrayList<>();
        for (File file : getFilesInDir(APPDATA_PATH, ".json")) {
            SentRequest request = mapper.readValue(file, SentRequest.class);
            if(keyParts.equals(request.getKeyParts())){
                requests.add(mapper.readValue(file, SentRequest.class));
            }
        }
        return requests;
    }

    private static List<File> getFilesInDir(String dir) throws IOException{
        return getFilesInDir(dir, "");
    }

    private static List<File> getFilesInDir(String dir, String ext) throws IOException{
        return Files.walk(Paths.get(dir))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(ext))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    public static void saveKey(List<TextField> fields){
        StringBuilder key = new StringBuilder();
        for (TextField field : fields) {
            if(StringUtils.isEmpty(field.getText())){
                key.append("-");
                break;
            }
            key.append(field.getText()).append("-");
        }
        String finalKey = key.toString().substring(0, key.length()-1);
        LOGGER.info("SAVING KEY: {}", finalKey);
        try{
        BufferedWriter writer = new BufferedWriter(new FileWriter(SAVED_KEY_PATH));
        writer.write(finalKey);
        writer.close();
        } catch (IOException e){
            System.out.println("Couldn't read key");
            e.printStackTrace(System.out);
        }
    }

    public static void loadKey(List<TextField> fields){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(SAVED_KEY_PATH));
            List<String> keyParts = Arrays.asList(reader.readLine().split("-"));
            IntStream.range(0, keyParts.size()).forEach(i -> fields.get(i).setText(keyParts.get(i)));
        } catch (Exception e){
            LOGGER.error("Couldn't load key: {}", e);
        }
    }

    private static void initDriver(FirefoxProfile profile){
        if (driverLoaded) {
            WebDriver webDriver = new FirefoxDriver(options);
            driver = webDriver;
            driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
            AbstractPage.setDriver(webDriver);
        } else {
            driverLoaded = loadDriver();
            initDriver(profile);
        }
    }

    public static void initDriver(){
        initDriver(profile);
    }

    public static void closeDriver(){
        try {
            driver.close();
        } catch (Exception e){
            LOGGER.error("Driver already closed");
        }
    }

    //TODO This one will close program if it's unpaid
    public static void twentyThousandsMethod() {
    }

}
