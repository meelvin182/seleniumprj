package ru.sokolov.model.entities;

import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SentRequest implements Serializable, LoginEntity{

    private List<String> keyParts;

    private String requestNum;
    private String creationDate;
    private String status;
    private boolean download;
    private String path;

    public SentRequest() {
    }

    public SentRequest(RequestEntity entity) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();
        this.creationDate = dtf.format(currentDate);
        this.status = "Отправлен";
        this.download = false;
        this.path = entity.getFilePath();
        this.keyParts = entity.getKeyParts();
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getKeyParts() {
        return keyParts;
    }

    public void setKeyParts(List<String> keyParts) {
        this.keyParts = keyParts;
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
