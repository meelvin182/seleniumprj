package ru.sokolov;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static ru.sokolov.gui.MainScreen.DOWNLOADED_STATUS;
import static ru.sokolov.gui.MainScreen.downloadFirefox;

public final class CoreKernelSupaClazz {


    public static final String APPDATA_PATH = System.getenv("SystemDrive") + "\\egrntmp";
    public static final String LOGS_PATH = APPDATA_PATH + "\\logs";
    public static String APPDATA_TMP_PATH = APPDATA_PATH + "\\tmp";
    private static final String SAVED_KEY_PATH = APPDATA_PATH + "\\saved_key.txt";
    public static final String MAIN_PAGE = "https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn";

    public static final String TEST_KEY = "f5939ffe-f955-421a-b30b-884a5c527803";
    public static final String TEST_CADASTRE_NUM = "50:27:0040215:179;77:22:0040215;50:21:0000000:5";

    private static WebDriver driver;
    private static ObjectMapper mapper = new ObjectMapper();
    private static FirefoxProfile profile = new FirefoxProfile();
    private static FirefoxOptions options = new FirefoxOptions();

    private static final Logger LOGGER;

    private static CaptchaSolver solver;

    public static boolean driverLoaded = loadDriver();

    public static final int headlessWidth = 1920;
    public static final int headlessHeight = 1080;

    static {
        System.setProperty("logs.path", LOGS_PATH);
        LOGGER = LoggerFactory.getLogger(CoreKernelSupaClazz.class);
        initInEnv();

        profile.setPreference("browser.download.dir", APPDATA_PATH);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.download.panel.shown", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/x-gzip");
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/zip");
        profile.setPreference("browser.download.manager.showWhenStarting", false);

        options.setProfile(profile);
        options.setHeadless(true);


        try {
            options.getBinary();
        } catch (Exception e) {
            LOGGER.info("NO FIREFOX FOUND");
            Platform.runLater(() -> downloadFirefox.showAndWait());
        }
    }

    public static String solveCapcha(File file) {
        String solved = "";
        try {
            solved = solver.solve(file);
        } catch (Exception e) {
            LOGGER.error("Error while solving capthca: {]", e);
        }
        return solved;
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
        closeDriver();
    }

    public static void sendRequests(Map<RequestEntity, SentRequest> entities) throws Exception {
        if (entities.isEmpty()) return;
        try {
            initDriver(profile);
            LOGGER.info("Opening Main Page to send");
            driver.navigate().to(MAIN_PAGE);
            LoginPage.setPageData(entities.keySet().iterator().next());
            LoginPage.login();
            RequestOverviewPage.sendReuests(entities);
        } catch (Exception e) {
            LOGGER.error("Error: {]", e);
        } finally {
            LOGGER.info("Closing driver");
            closeDriver();
        }
    }

    @Deprecated
    public static void getRequests(RequestEntity entity) throws Exception {
        try {
            AllRequestsPage.process(entity);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            closeDriver();
            ;
        }
    }

    public static boolean openRequest(SentRequest request) throws Exception {
        File file = new File(request.getPath() + "\\" + request.getRequestNum() + ".xml");
        if (file.exists()) {
            Desktop.getDesktop().open(file.getParentFile());
            return true;
        } else {
            return false;
        }
    }

    public static void unzipDownloadedRequest(SentRequest request) throws Exception {
        File tmpDir = new File(APPDATA_TMP_PATH);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        try {
            while (tmpDir.list().length < 1 || !tmpDir.list()[0].endsWith("zip")) {
                TimeUnit.MILLISECONDS.sleep(250);
            }
            String folder = request.getPath();
            if (folder == null || folder.isEmpty() || folder.equals("null")) {
                folder = "C:/Users/" + System.getProperty("user.name") + "/Downloads/";
                request.setPath(folder);
            }
            TimeUnit.SECONDS.sleep(2);
            ZipFile zipFile = new ZipFile(getFilesInDir(tmpDir.getPath()).get(0).getPath());
            File unzippedZipFile = unzipSpecificExtension("zip", zipFile, request.getRequestNum(), tmpDir.getPath());
            File unzippedUnzippedFIle = unzipSpecificExtension("xml", new ZipFile(unzippedZipFile.getPath()), unzippedZipFile.getName().replaceAll(".zip", ""), folder);
            FileUtils.cleanDirectory(tmpDir);
            request.setStatus(DOWNLOADED_STATUS);
        } catch (Exception e) {
            FileUtils.cleanDirectory(tmpDir);
            throw e;
        }
    }

    private static File unzipSpecificExtension(String ext, ZipFile zipFile, String name, String folder) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (ext.equals(entry.getName().substring(entry.getName().length() - ext.length()))) {
                File tmpFile = new File(folder + "\\" + name + "." + ext);
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

    public static void updateRequestsStatus(List<SentRequest> requests) throws Exception {
        LOGGER.info("Started updating request statuses, requests to check amount: {}", requests.size());
        if (requests.isEmpty()) return;
        try {
            initDriver(profile);
            LOGGER.info("Opening Main Page to update statuses");
            driver.navigate().to(MAIN_PAGE);
            LoginPage.setPageData(requests.get(0));
            LoginPage.login();
            AllRequestsPage.updateRequestsStatus(requests);
        } catch (Exception e) {
            if (e.getMessage().contains("Cannot find firefox binary in PATH")) {
                Platform.runLater(downloadFirefox::showAndWait);
            }
            e.printStackTrace(System.out);
        } finally {
            closeDriver();
            LOGGER.info("Driver closed");
        }
    }

    private static void initInEnv() {
        File file = new File(APPDATA_PATH);
        File tmpDir = new File(APPDATA_TMP_PATH);
        try {
            if (!file.exists()) {
                file.mkdir();
                Files.setAttribute(Paths.get(file.toURI()), "dos:hidden", true);
            }
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            } else {
                FileUtils.cleanDirectory(tmpDir);
            }
            solver = new CaptchaSolver(loadModel().getAbsolutePath());
        } catch (Exception e) {
            LOGGER.info("ERROR WHILE INITIALIZING REQUIRED DIRECTORIES AND FILES: ", e);
        }
        try {
            solver = new CaptchaSolver(loadModel().getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Couldn't load model: {}", e);
            System.exit(1);
        }
    }

    private static File loadModel() {
        File model = new File(APPDATA_PATH + "\\model.zip");
        if (!model.exists()) {
            try {
                Files.copy(CoreKernelSupaClazz.class.getResourceAsStream("/model.zip"), Paths.get(model.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("COULDN'T LOAD MODEL ZIP: ", e);
            }
        }
        return model;
    }

    private static boolean loadDriver() {
        try {
            File driver = new File(APPDATA_PATH + "\\driver.exe");
            if (!driver.exists()) {
                InputStream in = CoreKernelSupaClazz.class.getResourceAsStream("/geckodriver.exe");
                Files.copy(in, Paths.get(driver.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }
            System.setProperty("webdriver.gecko.driver", driver.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String loadProfile() {
        try {
            File folder = new File(System.getenv("SystemDrive") + "\\egrnprofile");
            folder.mkdir();
            File copyTo = new File(System.getenv("SystemDrive") + "\\egrnprofile\\user.js");
            copyTo.createNewFile();
            InputStream in = CoreKernelSupaClazz.class.getResourceAsStream("/user.js");
            Files.copy(in, Paths.get(copyTo.getPath()), StandardCopyOption.REPLACE_EXISTING);
            return folder.getPath();
        } catch (Exception e) {
            LOGGER.info("COULDN'T COPY PROFILE: ", e);
        }
        return null;
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

    public static void deleteBeforeSave(List<String> keyParts) {
        try {
            for (File file : getFilesInDir(APPDATA_PATH, ".json")) {
                SentRequest request = mapper.readValue(file, SentRequest.class);
                if (keyParts.equals(request.getKeyParts())) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't clear folder {}", e);
        }
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
        LOGGER.info("Reading requests with key: {}", keyParts.stream().map(String::toString).reduce("", String::concat));
        List<SentRequest> requests = new ArrayList<>();
        for (File file : getFilesInDir(APPDATA_PATH, ".json")) {
            SentRequest request = mapper.readValue(file, SentRequest.class);
            if (keyParts.equals(request.getKeyParts())) {
                requests.add(mapper.readValue(file, SentRequest.class));
            }
        }
        return requests;
    }

    public static File getDownloadedCaptcha() throws Exception {
        File captcha = getFilesInDir(APPDATA_TMP_PATH, "").get(0);
        captcha.renameTo(new File(APPDATA_TMP_PATH + "captcha.png"));
        return captcha;
    }

    private static List<File> getFilesInDir(String dir) throws IOException {
        return getFilesInDir(dir, "");
    }

    private static List<File> getFilesInDir(String dir, String ext) throws IOException {
        return Files.walk(Paths.get(dir))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(ext))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    public static void saveKey(List<TextField> fields) {
        StringBuilder key = new StringBuilder();
        for (TextField field : fields) {
            if (StringUtils.isEmpty(field.getText())) {
                key.append("-");
                break;
            }
            key.append(field.getText()).append("-");
        }
        String finalKey = key.toString().substring(0, key.length() - 1);
        LOGGER.info("SAVING KEY: {}", finalKey);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(SAVED_KEY_PATH));
            writer.write(finalKey);
            writer.close();
        } catch (IOException e) {
            System.out.println("Couldn't read key");
            e.printStackTrace(System.out);
        }
    }

    public static void loadKey(List<TextField> fields) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SAVED_KEY_PATH));
            List<String> keyParts = Arrays.asList(reader.readLine().split("-"));
            IntStream.range(0, keyParts.size()).forEach(i -> fields.get(i).setText(keyParts.get(i)));
        } catch (Exception e) {
            LOGGER.error("Couldn't load key: {}", e);
        }
    }

    private static void initDriver(FirefoxProfile profile) {
        if (driverLoaded) {
            WebDriver webDriver = new FirefoxDriver(options);
            driver = webDriver;
            driver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);
            driver.manage().window().setSize(new Dimension(headlessWidth, headlessHeight));
            AbstractPage.setDriver(webDriver);
        } else {
            driverLoaded = loadDriver();
            initDriver(profile);
        }
    }

    public static void initDriver() {
        initDriver(profile);
    }

    public static void closeDriver() {
        try {
            driver.close();
        } catch (Exception e) {
            LOGGER.error("Driver already closed: {}", e);
        }
    }

}
