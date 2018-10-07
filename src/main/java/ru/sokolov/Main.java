package ru.sokolov;


import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.model.entities.RequestEntity;
import ru.sokolov.model.entities.SentRequest;
import ru.sokolov.model.pages.AbstractPage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static ru.sokolov.CoreKernelSupaClazz.checkForProcessedRequests;

@Deprecated
public class Main {

    private static List<String> regions = loadRegions();

    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe");
        RequestEntity entity = new RequestEntity();
        entity.setKeyParts(Arrays.stream("f5939ffe-f955-421a-b30b-884a5c527803".split("-")).collect(Collectors.toList()));
        entity.setRegion(regions.get(30));
        entity.setCadastreNums("50:27:0040215:179");
        entity.setGetChangeRightsInfo(true);
        CoreKernelSupaClazz.saveRequestToJson(entity);
        for(SentRequest request : CoreKernelSupaClazz.readAllRequests()){
            System.out.println(request);
        }
    }

    //CTRL CV TO TEST
    @SuppressWarnings("all")
    private static List<String> loadRegions() {
        List<String> values = new ArrayList<>();
        try {
            InputStream in = Main.class.getResourceAsStream("/regions.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
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
