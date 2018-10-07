package ru.sokolov.model.entities;

import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SentRequest implements Serializable{

    private String requestNum;
    private String creationDate;
    private String status;
    private boolean download;

    public SentRequest() {
    }

    public SentRequest(RequestEntity entity) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();
        this.requestNum = entity.getCadastreNums();
        this.creationDate = dtf.format(currentDate);
        this.status = "Отправлен";
        this.download = false;
    }

    public String getRequestNum() {
        return requestNum;
    }

    public void setRequestNum(String requestNum) {
        this.requestNum = requestNum;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return "SentRequest{" +
                "requestNum='" + requestNum + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", status='" + status + '\'' +
                ", download=" + download +
                '}';
    }
}
