package ru.sokolov.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.sokolov.Main;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestButton extends Button {

    public TestButton(Stage parent) {
        this.setText("SEND SHIET");
        this.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                RequestPopup requestPopup = new RequestPopup(parent);
            }
        });
    }

    @SuppressWarnings("all")
    private static void doAction() throws InterruptedException{
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Degrod\\Desktop\\chromedriver.exe");
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
    }
}
