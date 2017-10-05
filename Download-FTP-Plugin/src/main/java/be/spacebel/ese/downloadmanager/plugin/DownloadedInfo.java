package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.plugin.EDownloadStatus;
import java.io.File;
import java.util.ArrayList;

public class DownloadedInfo {

    private String productName;
    private int numberOfFiles;
    private long overallSize;
    private long downloadedSize;
    private ArrayList<File> downloadedFiles;
    private EDownloadStatus status;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public long getOverallSize() {
        return overallSize;
    }

    public void setOverallSize(long overallSize) {
        this.overallSize = overallSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public ArrayList<File> getDownloadedFiles() {
        return downloadedFiles;
    }

    public void setDownloadedFiles(ArrayList<File> downloadedFiles) {
        this.downloadedFiles = downloadedFiles;
    }

    public EDownloadStatus getStatus() {
        return status;
    }

    public void setStatus(EDownloadStatus status) {
        this.status = status;
    }

    public String debug() {
        StringBuffer sb = new StringBuffer();
        sb.append("DownloadedInfo[ ");
        sb.append("productName = " + productName);
        sb.append(", numberOfFiles = " + numberOfFiles);
        sb.append(", overallSize = " + overallSize);
        sb.append(", downloadedSize = " + downloadedSize);
        sb.append(", status = " + status);
        sb.append(", downloadedFiles[");
        for (File f : this.downloadedFiles) {
            sb.append(f.getAbsolutePath());
            sb.append(";");
        }
        sb.append("]");
        sb.append("]");
        return sb.toString();
    }
}
