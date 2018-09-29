package ru.sokolov.gui;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestButton extends Button {

    public TestButton(Stage parent) {
        this.setText("Отправить Запрос");
        this.setOnAction(event -> {
            RequestPopup requestPopup = new RequestPopup(parent);
        });
    }
}
