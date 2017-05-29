/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.logic;

/**
 *
 * @author mng
 */
public class DownloaderException extends RuntimeException {

    private int code;
    private String status;
    private String developerMessage;
    
    public DownloaderException() {
        this("No message", 500);
    }

    public DownloaderException(String message) {
        this(message, 500);
    }

    public DownloaderException(String message, int code) {
        super(message);
        this.code = code;
    }

    public DownloaderException(String message, int code, String status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public DownloaderException(String message, int code, String status, String developerMessage) {
        super(message);
        this.code = code;
        this.status = status;
        this.developerMessage = developerMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }   
}
