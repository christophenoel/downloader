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
public class ProductStatus implements Serializable {

    private String productURL;
    private String downloadDirectory;
    private String completedDownloadPath;
    private long totalFileSize;
    private String productName;
    private ProductProgress progress;

    public ProductStatus() {
    }

    public String getProductURL() {
        return productURL;
    }

    public void setProductURL(String productURL) {
        this.productURL = productURL;
    }

    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    public String getCompletedDownloadPath() {
        return completedDownloadPath;
    }

    public void setCompletedDownloadPath(String completedDownloadPath) {
        this.completedDownloadPath = completedDownloadPath;
    }

    public long getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductProgress getProgress() {
        return progress;
    }

    public void setProgress(ProductProgress progress) {
        this.progress = progress;
    }

}
