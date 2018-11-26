package ru.sokolov;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;

public class CaptchaCoordsProvider extends WebDriverCoordsProvider {

    @Override
    public Coords ofElement(WebDriver driver, WebElement element) {
        Point point = element.getLocation();
        Dimension dimension = element.getSize();
        return new Coords(point.getX() + 1, point.getY(), dimension.getWidth(), dimension.getHeight());
    }
}
