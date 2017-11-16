package com.krypton;

import java.io.File;

/**
 * Created by krypton on 3/17/2017.
 */
public class Part {
    private int id;
    private String url;
    private String tmpFileName;
    private String actualFileName;
    private long startbyte;
    private long endbyte;
    private long downloaded;
    private states status;
    public enum states{
        QUEUED, DOWNLOADING, RETRYING, ERROR, STOP, DONE;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTmpFileName() {
        return tmpFileName;
    }

    public void setTmpFileName(String tmpFileName) {
        this.tmpFileName = tmpFileName;
    }

    public String getActualFileName() {
        return actualFileName;
    }

    public void setActualFileName(String actualFileName) {
        this.actualFileName = actualFileName;
    }

    public long getStartbyte() {
        return startbyte;
    }

    public void setStartbyte(long startbyte) {
        this.startbyte = startbyte;
    }

    public long getEndbyte() {
        return endbyte;
    }

    public void setEndbyte(long endbyte) {
        this.endbyte = endbyte;
    }

    public synchronized states getStatus() {
        return status;
    }

    public synchronized void setStatus(states status) {
        this.status = status;
    }
}
