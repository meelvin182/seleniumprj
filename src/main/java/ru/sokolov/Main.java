package ru.sokolov;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.model.pages.SearchObjectsPage;
import ru.sokolov.model.pages.SecondPage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Deprecated
public class Main {
    public static void main(String[] args) throws InterruptedException {
       // ClassLoader classLoader = getClass().getClassLoader();
        //File file = new File(classLoader.getResource("chromedriver.exe").getFile());
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.navigate().to("https://rosreestr.ru/wps/portal/p/cc_present/ir_egrn");
       // TimeUnit.SECONDS.sleep(5);
        List<WebElement> list = new ArrayList<>();
        while (list.size() != 5) {
            list.addAll(driver.findElements(By.className("v-textfield")));
        }
        System.out.println(list.size());
        System.out.println(list);
        list.get(0).click();
        list.get(0).sendKeys("f5939ffe");
        TimeUnit.SECONDS.sleep(1);
        list.get(1).sendKeys("f955");
        TimeUnit.SECONDS.sleep(1);
        list.get(2).sendKeys("421a");
        TimeUnit.SECONDS.sleep(1);
        list.get(3).sendKeys("b30b");
        TimeUnit.SECONDS.sleep(1);
        list.get(4).sendKeys("884a5c527803");
        driver.findElement(By.className("normalButton")).click();
        TimeUnit.SECONDS.sleep(1);

        SecondPage page = new SecondPage(driver);
        page.openSearchParams();

        TimeUnit.SECONDS.sleep(2);
        SearchObjectsPage page1 = new SearchObjectsPage(driver);
        page1.setCadastreNum("1232132132");
        page1.setStreetName("QWEQWEQWEQWEQEW");
    }
}
