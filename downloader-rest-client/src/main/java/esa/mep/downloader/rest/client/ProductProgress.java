/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.rest.client;

import java.io.Serializable;

/**
 *
 * @author mng
 */
public class ProductProgress implements Serializable {

    public enum Status {

        NOT_STARTED, IDLE, RUNNING, CANCELLED, IN_ERROR, COMPLETED
    }

    public ProductProgress() {
    }

    private long downloadedSize;
    private String message;
    private int progressPercentage;
    private String statusCode;
    private Status status;

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
