package ru.sokolov.model.entities;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SentRequest extends RequestEntity implements Serializable {

    private RequestEntity entity;
    private String name;
    private String status;

    public SentRequest(RequestEntity entity) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        name = entity.getCadastreNums() + " " + dateFormat.format(new Date());
    }

    public RequestEntity getEntity() {
        return entity;
    }

    public void setEntity(RequestEntity entity) {
        this.entity = entity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
