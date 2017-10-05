package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.EDownloadStatus;
import esa.mep.downloader.plugin.IDownloadProcess;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

public class FTPDownloadProcess implements IDownloadProcess {

    private FTPDownloadProgressMonitor progressMonitor;
    private DownloadProcessInfo downloadProcessInfo;
    private ExecutorService executorService;
    private String queueItemId = null;
    private FTPDownload ftpDownload = null;
    private boolean isCancel;

    private Logger log = Logger.getLogger(getClass());

    public FTPDownloadProcess(DownloadProcessInfo dwlInfo) {
        log.debug("Create a new instance of FTPDownloadProcess (downloadInfo = " + dwlInfo.debug() + ").");
        this.downloadProcessInfo = dwlInfo;
        ftpDownload = new FTPDownload(dwlInfo);
    }

    public EDownloadStatus startDownload() throws DMPluginException {
        log.debug("Enter method startDownload()");
        log.debug(downloadProcessInfo.debug());

        if (this.downloadProcessInfo.getAccount().getMaxConnections() < 0) {
            downloadNoLimit();
        } else {
            queueItemId = UUID.randomUUID().toString();
            FTPDownloadPlugin.addToDownloadQueue(queueItemId, this.downloadProcessInfo);
            downloadWithQueue();
        }
        return getProgressMonitor().getStatus();
    }

    public EDownloadStatus pauseDownload() throws DMPluginException {
        log.debug("Enter method pauseDownload()");
        return null;
    }

    public EDownloadStatus resumeDownload() throws DMPluginException {
        log.debug("Enter method resumeDownload()");
        return null;
    }

    public EDownloadStatus cancelDownload() throws DMPluginException {
        log.debug("Enter method cancelDownload()");
        log.debug(downloadProcessInfo.debug());
        this.isCancel = true;
        if (queueItemId != null) {
            if (FTPDownloadPlugin.existInThreadsDownloadQueue(queueItemId, downloadProcessInfo)) {
                log.debug("The product: " + downloadProcessInfo.getProductURI() + " is in the queue. Remove it from the queue due to it's cancelled.");
                FTPDownloadPlugin.removeFromThreadsDownloadQueue(queueItemId, downloadProcessInfo);
            }
        }
        ftpDownload.setCancel(true);
        return getProgressMonitor().getStatus();
    }

    public EDownloadStatus getStatus() {
        log.debug("Enter method getStatus()");
        return getProgressMonitor().getStatus();
    }

    public File[] getDownloadedFiles() {
        log.debug("Enter method getDownloadedFiles()");
        return getProgressMonitor().getDownloadedFiles();
    }

    public void disconnect() throws DMPluginException {
        log.debug("Enter method disconnect()");
    }

    public synchronized FTPDownloadProgressMonitor getProgressMonitor() {
        if (this.progressMonitor == null) {
            this.progressMonitor = new FTPDownloadProgressMonitor(this.downloadProcessInfo.getDownloadListener());
        }
        return this.progressMonitor;
    }

    private void downloadNoLimit() {
        log.debug("Start downloading without limitation.");
        getProgressMonitor().setStatus(EDownloadStatus.RUNNING);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            public void run() {
                download(downloadProcessInfo);
            }
        });

        shutdownExecutorService();
    }

    private void downloadWithQueue() {
        log.debug("Start downloading with a queue.");
        getProgressMonitor().setStatus(EDownloadStatus.RUNNING);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new FTPDownloadThread());
        shutdownExecutorService();
    }

    private void shutdownExecutorService() {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            log.debug("Error when shut executorService down:");
            e.printStackTrace();
        }
    }

    private void download(DownloadProcessInfo newDwlProcessInfo) {
        try {
            log.debug("Downloaded process info : " + newDwlProcessInfo.debug());

            DownloadedInfo downloadedInfo = ftpDownload.download();

            log.debug("Downloaded info : " + downloadedInfo.debug());

            if (downloadedInfo.getStatus() == EDownloadStatus.COMPLETED) {
                log.debug("download successful.");
                log.debug("downloaded product name:" + downloadedInfo.getProductName());

                getProgressMonitor().setTotalFileDownloadedSize(downloadedInfo.getDownloadedSize());
                log.debug("total file downloaded size: " + downloadedInfo.getDownloadedSize());

                if (downloadedInfo.getDownloadedFiles() != null) {
                    log.debug("number of downloaded files: " + downloadedInfo.getDownloadedFiles().size());
                    File[] downloadedFiles = new File[downloadedInfo.getDownloadedFiles().size()];
                    downloadedInfo.getDownloadedFiles().toArray(downloadedFiles);
                    log.debug("file array size: " + downloadedFiles.length);
                    getProgressMonitor().setDownloadedFiles(downloadedFiles);
                }

                log.debug("notify to Download Manager.");
                getProgressMonitor().notifyOfProductDetails(downloadedInfo.getProductName(), downloadedInfo.getNumberOfFiles(), downloadedInfo.getOverallSize());
                log.debug("set status.");
                getProgressMonitor().setStatus(EDownloadStatus.COMPLETED);

                if (newDwlProcessInfo.getAccount().getMaxConnections() >= 0) {
                    log.debug("increase allowed download connections.");
                    FTPDownloadPlugin.increaseAllowedDownload(newDwlProcessInfo.getAccount().getUuid());
                }
            } else if (downloadedInfo.getStatus() == EDownloadStatus.CANCELLED) {
                getProgressMonitor().setStatus(EDownloadStatus.CANCELLED);
                if (newDwlProcessInfo.getAccount().getMaxConnections() >= 0) {
                    log.debug("increase allowed download connections.");
                    FTPDownloadPlugin.increaseAllowedDownload(newDwlProcessInfo.getAccount().getUuid());
                }
            } else {
                log.debug("download fails: " + downloadedInfo.getProductName());

                getProgressMonitor().setStatus(EDownloadStatus.IN_ERROR);
                if (newDwlProcessInfo.getAccount().getMaxConnections() >= 0) {
                    log.debug("increase allowed download connections.");
                    FTPDownloadPlugin.increaseAllowedDownload(newDwlProcessInfo.getAccount().getUuid());
                }
            }
        } catch (Exception e) {
            log.debug("download fails with exception: " + e.getMessage());
            getProgressMonitor().setStatus(EDownloadStatus.IN_ERROR, e.getMessage());
            if (newDwlProcessInfo.getAccount().getMaxConnections() >= 0) {
                log.debug("increase allowed download connections.");
                FTPDownloadPlugin.increaseAllowedDownload(newDwlProcessInfo.getAccount().getUuid());
            }
        }
    }

    public class FTPDownloadThread implements Runnable {

        public void run() {
            String accountUUID = downloadProcessInfo.getAccount().getUuid();
            int waitingTime = 0;
            log.debug("view connections mapping before checking allowing:");
            FTPDownloadPlugin.viewCurrentAvailableConnectionsPerAccountMapping();
            while (!FTPDownloadPlugin.isAllowedDownload(accountUUID)) {
                if (isCancel) {
                    log.debug("Stop waiting for downloading product: " + downloadProcessInfo.getProductURI() + " due to it's cancelled.");
                    break;
                }
                try {
                    waitingTime += (FTPDownloadPlugin.SLEEP_TIME / 1000);
                    log.debug("Waiting " + waitingTime + "s for downloading product: " + downloadProcessInfo.getProductURI());
                    Thread.sleep(FTPDownloadPlugin.SLEEP_TIME);
                } catch (InterruptedException iEx) {
                    log.debug("error while waiting for downloading: " + iEx.getLocalizedMessage());
                }
            }

            if (isCancel) {
                log.debug("Do not download product: " + downloadProcessInfo.getProductURI() + " due to it's cancelled.");
                getProgressMonitor().setStatus(EDownloadStatus.CANCELLED);
            } else {
                log.debug("Start downloading accountUUID = " + accountUUID + " after waiting " + waitingTime + "s.");
                // FTPDownloadPlugin.isAllowedDownload(key);

                log.debug("view connections mapping before downloading:");
                FTPDownloadPlugin.viewCurrentAvailableConnectionsPerAccountMapping();
                try {
                    String queueItemId = FTPDownloadPlugin.getArrayBlockingQueue(accountUUID).take();
                    if (queueItemId != null) {
                        DownloadProcessInfo executeDwlProcessInfo = FTPDownloadPlugin.takeFromQueueItemInfoMapping(queueItemId);
                        if (executeDwlProcessInfo != null) {
                            log.debug("downloading URL: " + executeDwlProcessInfo.getProductURI());
                            Thread thread = Thread.currentThread();
                            log.debug("Start thread: " + thread.getName() + " (" + thread.getId() + ")");
                            download(executeDwlProcessInfo);
                            log.debug("Stop thread: " + thread.getName() + " (" + thread.getId() + ")");
                        } else {
                            log.debug("DownloadProcessInfo of key = " + queueItemId + " is not in the mapping.");
                        }
                    } else {
                        log.debug("queue item is empty !");
                    }
                } catch (InterruptedException iEx) {
                    log.debug("error while downloading: " + iEx.getLocalizedMessage());
                }
                log.debug("End downloading accountUUID = " + accountUUID + " ..............");
            }
        }
    }
}
