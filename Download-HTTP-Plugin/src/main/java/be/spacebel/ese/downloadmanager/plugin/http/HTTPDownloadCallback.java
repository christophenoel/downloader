package be.spacebel.ese.downloadmanager.plugin.http;


import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.EDownloadStatus;
import org.apache.commons.io.FileUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 *
 * @author ane
 */
public class HTTPDownloadCallback implements FutureCallback<DownloadProcessInfo> {

    private static final Logger LOG = LogManager.getLogger(HTTPDownloadCallback.class);

    private final IHTTPDownloadProcess downloadProcess;
    private final HTTPDownloadInfo dlInfo;


    public HTTPDownloadCallback(IHTTPDownloadProcess downloadProcess, HTTPDownloadInfo dlInfo) {
        super();
        this.downloadProcess = downloadProcess;
        this.dlInfo = dlInfo;
    }


    @Override
    public void completed(DownloadProcessInfo processInfo) {
        dlInfo.setStatus(EDownloadStatus.COMPLETED);
        LOG.debug("------------------------------------------------------");
        LOG.debug("Finished download task for: " + dlInfo.getUri().getPath() + "@" + dlInfo.getUri().getHost());
        LOG.debug("File downloaded to '" + dlInfo.getFile().getAbsolutePath() + "'");
        LOG.debug("Status = " + dlInfo.getStatus());
        LOG.debug("File size = " + FileUtils.byteCountToDisplaySize(dlInfo.getFile().length()));
        LOG.debug("------------------------------------------------------");
        downloadProcess.progress("Download completed.", null);
    }


    @Override
    public void failed(Exception ex) {
        if (dlInfo.getStatus() != EDownloadStatus.CANCELLED) {
            dlInfo.setStatus(EDownloadStatus.IN_ERROR);
            LOG.debug("------------------------------------------------------");
            LOG.debug("Failed to download task for: " + dlInfo.getUri().getPath() + "@" + dlInfo.getUri().getHost());
            LOG.debug("Error message = " + ex);
            LOG.debug("------------------------------------------------------");
            downloadProcess.progress(ex.getMessage(), new DMPluginException(ex));
//            deleteFile(); // performed by Data Manager
        }
    }


    @Override
    public void cancelled() {
        dlInfo.setStatus(EDownloadStatus.CANCELLED);
        LOG.debug("------------------------------------------------------");
        LOG.debug("Cancelled download task for: " + dlInfo.getUri().getPath() + "@" + dlInfo.getUri().getHost());
        LOG.debug("------------------------------------------------------");
        downloadProcess.progress("Download cancelled.", null);
//        deleteFile(); // performed by Data Manager
    }


    /**
     * Maybe not required.
     *
     * Deletes the file downloaded by the task.
     * See attached HTTPDownloadInfo that handle the file.
     *
     * ANE: check that files are deleted by the Data Manager.
     *
     * @return true IFF file has been deleted.
     */
    private boolean deleteFile() {
        if (dlInfo.getFile().isFile()) {
            FileUtils.deleteQuietly(dlInfo.getFile());
            if (LOG.isDebugEnabled()) {
                float percentage = -1;
                if (dlInfo.getContentLength() > 0) {
                    percentage = ((float) dlInfo.getFile().length() / (float) dlInfo.getContentLength()) * 100;
                }
                String msg = "[location='" + dlInfo.getFile().getPath() + "'"
                        + ", length=" + dlInfo.getFile().length() + "B"
                        + ", completion=" + percentage + "%]";
                LOG.debug("Deleted file " + msg);
            }
            return true;
        } else {
            return false;
        }
    }

}
