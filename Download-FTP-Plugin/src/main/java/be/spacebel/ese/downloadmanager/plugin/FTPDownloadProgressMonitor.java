package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.plugin.EDownloadStatus;
import esa.mep.downloader.plugin.IProductDownloadListener;
import java.io.File;

public class FTPDownloadProgressMonitor {

    private EDownloadStatus status;
    private int percentageComplete;
    private long totalFileDownloadedSize;
    private File[] downloadedFiles;
    private String message;

    private IProductDownloadListener productDownloadListener;

    public FTPDownloadProgressMonitor(IProductDownloadListener productDownloadListener) {
        this.status = EDownloadStatus.NOT_STARTED;
        this.productDownloadListener = productDownloadListener;
    }

    public synchronized EDownloadStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(EDownloadStatus newStatus) {
        setStatus(newStatus, null);
    }

    public synchronized void setStatus(EDownloadStatus newStatus, String message) {
        this.status = newStatus;
        if (this.status == EDownloadStatus.COMPLETED) {
            setPercentageComplete(100);
        }
        if (message != null) {
            this.message = message;
        }

        notifyProgressListener();
    }

    public synchronized int getPercentageComplete() {
        return percentageComplete;
    }

    public synchronized void setPercentageComplete(int percentageComplete) {
        this.percentageComplete = percentageComplete;
    }

    public synchronized long getTotalFileDownloadedSize() {
        return this.totalFileDownloadedSize;
    }

    public synchronized void setTotalFileDownloadedSize(long totalFileDownloadedSize) {
        this.totalFileDownloadedSize = totalFileDownloadedSize;
    }

    public synchronized File[] getDownloadedFiles() {
        return downloadedFiles;
    }

    public synchronized void setDownloadedFiles(File[] downloadedFiles) {
        this.downloadedFiles = downloadedFiles;
    }

    public void notifyOfProductDetails(String productName, int numberOfFiles, long overallSize) {
        this.productDownloadListener.productDetails(productName, Integer.valueOf(numberOfFiles),
                Long.valueOf(overallSize));
    }

    private void notifyProgressListener() {
        this.productDownloadListener.progress(Integer.valueOf(getPercentageComplete()),
                Long.valueOf(getTotalFileDownloadedSize()), getStatus(), this.message);

    }

}
