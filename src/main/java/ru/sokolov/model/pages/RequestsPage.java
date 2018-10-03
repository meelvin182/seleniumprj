package ru.sokolov.model.pages;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.sokolov.model.entities.RequestEntity;

public class RequestsPage extends AbstractPage {

    public static void continueToRequestOverview(RequestEntity entity) throws Exception{
        SearchObjectsPage.sendRequest(entity);
        String[] requiredParts = entity.getCadastreNums().split(":");
        String elemName = StringUtils.substringAfterLast(entity.getCadastreNums(), ":");
        driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className("v-table-cell-content-cadastral_num")));
        System.out.println("Wait is over");
        driver.findElement(By.className("v-table-cell-content-cadastral_num")).click();

    }
}
