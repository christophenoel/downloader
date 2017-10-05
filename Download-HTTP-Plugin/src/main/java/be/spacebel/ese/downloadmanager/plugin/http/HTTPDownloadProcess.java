package be.spacebel.ese.downloadmanager.plugin.http;

import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.EDownloadStatus;
import esa.mep.downloader.plugin.IDownloadProcess;
import esa.mep.downloader.plugin.IProductDownloadListener;
import java.io.File;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author ane
 */
public class HTTPDownloadProcess implements IHTTPDownloadProcess {

    private static final Logger LOG = LogManager.getLogger(HTTPDownloadProcess.class);

    private final HttpClientController httpClientController;

    private final DownloadProcessInfo processInfo;

    private final IProductDownloadListener downloadListener;

    private HTTPDownloadInfo dlInfo = null;

    public HTTPDownloadProcess(IProductDownloadListener downloadListener, DownloadProcessInfo processInfo, HttpClientController httpClientController) {
        this.processInfo = processInfo;
        this.httpClientController = httpClientController;
        this.downloadListener = downloadListener;
    }

    /**
     * For debug purpose. Calculate the percentage of progress.
     *
     * @deprecated Progress must be sent to the Product Download Listener (see
     * HTTPDownloadProcess#progress(String, DMPluginException))
     * @return percentage.
     */
    public float getProgress() {
        if (dlInfo.getFile() != null && dlInfo.getFile().isFile()) {
            if (dlInfo.getContentLength() != -1) {
                return ((float) dlInfo.getFile().length() / (float) dlInfo.getContentLength()) * 100;
            }
        }
        return -1;
    }

    /**
     * Informs the listener about the last status of download task. If the
     * listener does not exist, a warning message is displayed in console logs.
     *
     * @param message Message about the status (can be null if the "exception"
     * is not null).
     * @param ex (Optional) The exception that interrupted the download.
     */
    public void progress(String message, DMPluginException ex) {
        LOG.debug("progress ............................ " );
        // remove the task if download process is stopped (the "CANCELED" case is already handled int method "cancelDownload")
        if (dlInfo.getStatus() == EDownloadStatus.COMPLETED
                || dlInfo.getStatus() == EDownloadStatus.IN_ERROR) {
            LOG.debug("removing download task");
            httpClientController.removeDownloadTask(processInfo, false);
        }

        // call the Product Download Listener
        if (downloadListener == null) { // should be never null, condition exists only here for debug
            LOG.warn("No Download Listener instance available.");
            LOG.debug("Progress status for" + processInfo.getProductURI().getPath() + "@" + processInfo.getProductURI().getHost()
                    + " : [percentage=" + getProgress() + "%, downloadedSize=" + dlInfo.getFile().length() + "B, status=" + dlInfo.getStatus()
                    + ", message=" + message + (ex != null ? ", exception=" + ex.getMessage() : "") + "]");
        } else {
            Integer percentage = Math.round(getProgress());
            if (ex == null) {
                LOG.debug("downloaded file name: " + dlInfo.getFile().getName());
                LOG.debug("downloaded file size: " + dlInfo.getFile().length());
                /*
                    inform to DM the downloaded file name and size
                */
                downloadListener.productDetails(dlInfo.getFile().getName(), 1, dlInfo.getFile().length());
                /*
                    inform to DM the progress
                */                
                downloadListener.progress(percentage, dlInfo.getFile().length(), dlInfo.getStatus(), message);                
            } else {
                downloadListener.progress(percentage, dlInfo.getFile().length(), dlInfo.getStatus(), message, ex);
            }
        }
    }

    @Override
    public EDownloadStatus getStatus() {
        return dlInfo.getStatus();
    }

    @Override
    public File[] getDownloadedFiles() {
        final File theFile = dlInfo.getFile();
        return new File[]{theFile};
    }

    @Override
    public EDownloadStatus startDownload() throws DMPluginException {
        LOG.debug("Starting download of '" + processInfo.getProductURI().getPath() + "@" + processInfo.getProductURI().getHost() + "'.");
        dlInfo = httpClientController.addDownloadTask(this, processInfo);
        return dlInfo.getStatus();
    }

    @Override
    public EDownloadStatus cancelDownload() throws DMPluginException {
        LOG.debug("Cancel download of '" + processInfo.getProductURI().getPath() + "@" + processInfo.getProductURI().getHost() + "'.");
        httpClientController.removeDownloadTask(processInfo, true);
        return dlInfo.getStatus();
    }

    @Override
    public EDownloadStatus pauseDownload() throws DMPluginException {
        throw new DMPluginException(new UnsupportedOperationException("Not supported."));
    }

    @Override
    public EDownloadStatus resumeDownload() throws DMPluginException {
        throw new DMPluginException(new UnsupportedOperationException("Not supported."));
    }

    @Override
    public void disconnect() throws DMPluginException {
        throw new DMPluginException(new UnsupportedOperationException("Not supported."));
    }


}
