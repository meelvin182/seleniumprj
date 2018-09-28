package ru.sokolov;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.model.RequestEntity;
import ru.sokolov.model.pages.LoginPage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Deprecated
public class Main {

    private static List<String> regions = loadRegions();

    public static void main(String[] args) throws InterruptedException {
       // ClassLoader classLoader = getClass().getClassLoader();
        //File file = new File(classLoader.getResource("chromedriver.exe").getFile());
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.navigate().to("https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn");
       // TimeUnit.SECONDS.sleep(5);

        RequestEntity entity = new RequestEntity();
        entity.setKeyParts(Arrays.stream("f5939ffe-f955-421a-b30b-884a5c527803".split("-")).collect(Collectors.toList()));
        entity.setRegion(regions.get(84));
        new LoginPage(driver, entity).process();
    }


    //CTRL CV TO TEST
    @SuppressWarnings("all")
    private static List<String> loadRegions() {
        List<String> values = new ArrayList<>();
        try {
            InputStream in = Main.class.getResourceAsStream("/regions.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Scanner scanner = new Scanner(reader);       // create scanner to read
            while (scanner.hasNextLine()) {  // while there is a next line
                values.add(scanner.nextLine());
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return values;
    }
}
