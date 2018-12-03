package ru.sokolov.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.sokolov.gui.RequestPopup.SENDING;

public class SentRequest implements Serializable, LoginEntity{

    private List<String> keyParts;

    private String requestNum;
    private String cadastreNum;
    private String creationDate;
    private String status;
    private boolean download;
    private String path = "";
    private RequestEntity requestEntity;

    public SentRequest() {
    }

    public SentRequest(RequestEntity entity) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();
        this.cadastreNum = entity.getCadastreNums();
        this.requestNum = entity.getCadastreNums();
        this.creationDate = dtf.format(currentDate);
        this.status = SENDING;
        this.download = false;
        this.path = path + entity.getFilePath();
        this.keyParts = entity.getKeyParts();
        requestEntity = entity;
    }

    public String getCadastreNum() {
        return cadastreNum;
    }

    public void setCadastreNum(String cadastreNum) {
        this.cadastreNum = cadastreNum;
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

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    @Override
    public String toString() {
        return "SentRequest{" +
                "keyParts=" + keyParts +
                ", requestNum='" + requestNum + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", status='" + status + '\'' +
                ", download=" + download +
                ", path='" + path + '\'' +
                '}';
    }
}
