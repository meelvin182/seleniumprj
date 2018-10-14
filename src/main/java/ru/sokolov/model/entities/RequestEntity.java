package ru.sokolov.model.entities;

import java.util.List;

public class RequestEntity implements LoginEntity{

    private List<String> keyParts;
    private String cadastreNums;
    private String region;
    private String filePath;
    private boolean getObjectInfo;
    private boolean getChangeRightsInfo;

    public RequestEntity() {
    }

    public RequestEntity(List<String> keyParts, String cadastreNums, String region, String filePath, boolean getObjectInfo, boolean getChangeRightsInfo) {
        this.keyParts = keyParts;
        this.cadastreNums = cadastreNums;
        this.region = region;
        this.filePath = filePath;
        this.getObjectInfo = getObjectInfo;
        this.getChangeRightsInfo = getChangeRightsInfo;
    }

    public List<String> getKeyParts() {
        return keyParts;
    }

    public void setKeyParts(List<String> keyParts) {
        this.keyParts = keyParts;
    }

    public String getCadastreNums() {
        return cadastreNums;
    }

    public void setCadastreNums(String cadastreNums) {
        this.cadastreNums = cadastreNums;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isGetObjectInfo() {
        return getObjectInfo;
    }

    public void setGetObjectInfo(boolean getObjectInfo) {
        this.getObjectInfo = getObjectInfo;
    }

    public boolean isGetChangeRightsInfo() {
        return getChangeRightsInfo;
    }

    public void setGetChangeRightsInfo(boolean getChangeRightsInfo) {
        this.getChangeRightsInfo = getChangeRightsInfo;
    }
}
