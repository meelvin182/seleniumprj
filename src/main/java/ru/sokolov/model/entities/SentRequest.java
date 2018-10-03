package ru.sokolov.model.entities;

import javafx.beans.property.StringProperty;
import org.openqa.selenium.WebElement;

public class SentRequest {

    private String requestNum;
    private String creationDate;
    private String status;
    private boolean download;

    public SentRequest() {
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
}
