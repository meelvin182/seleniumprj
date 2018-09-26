package ru.sokolov.model;

import java.util.List;

public class RequestEntity {

    private List<String> keyParts;
    private List<String> cadastreNums;
    private String region;
    private String filePath;
    private boolean getObjectInfo;
    private boolean getChangeRightsInfo;

    public RequestEntity(List<String> keyParts, List<String> cadastreNums, String region, String filePath, boolean getObjectInfo, boolean getChangeRightsInfo) {
        this.keyParts = keyParts;
        this.cadastreNums = cadastreNums;
        this.region = region;
        this.filePath = filePath;
        this.getObjectInfo = getObjectInfo;
        this.getChangeRightsInfo = getChangeRightsInfo;
    }
}
