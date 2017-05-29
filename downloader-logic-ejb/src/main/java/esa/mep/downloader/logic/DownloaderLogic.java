package esa.mep.downloader.logic;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.DownloadStatus;
import esa.mep.downloader.plugin.PluginConfiguration;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.LoggerFactory;

/**
 *
 * @author martin
 */
@Stateless
@LocalBean
public class DownloaderLogic {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DownloaderLogic.class);
    @EJB
    /**
     * DownloadManager handles all operations related to the handling of a
     * DownloadRequest It self invoke the ProductDownloadManager handling
     * individual product URL downloads
     */
    private DownloadManager manager;

    /**
     * Start the handling of a DownloadRequest by the DownloadManager
     *
     * @param request the download request including products URLs.
     * @return the task unique identifier
     */
    public String download(DownloadRequest request) {
        LOGGER.debug("Handle download request " + request.getName());
        // create the download task (and implicitly it will create productDownloadTasks)
        DownloadTask task = manager.createDownloadTask(request);
        // start the download task
        manager.executeDownloadTask(task);
        return task.getIdentifier();
    }

    /**
     * Get the status document of the given download task.
     *
     * @param identifier download task identifier
     * @return the download status document
     */
    public DownloadStatus getStatus(String identifier) throws DownloaderException {
        LOGGER.debug("Get download task " + identifier);
        DownloadTask task = manager.getTask(identifier);
        if (task != null) {
            manager.createReleaseSchedule(identifier);
            return task.getStatus();
        } else {
            LOGGER.debug("No available download task for the identifier " + identifier);
            throw new DownloaderException("No available download task for the identifier " + identifier);
        }

    }

    /**
     * Cancel the download task
     *
     * @param identifier download task identifier
     * @return download status document
     */
    public DownloadStatus cancel(String identifier) {
        LOGGER.debug("Cancel download task " + identifier);
        DownloadTask task = manager.cancelTask(identifier);
        if (task != null) {
            return task.getStatus();
        } else {
            LOGGER.debug("No available download task for the identifier " + identifier);
            return null;
        }
    }

    public PluginConfiguration[] getConfiguration(String protocol, String[] servers) {
        return manager.getConfiguration(protocol, servers);
    }

    public String[] expandUrl(String url) throws DownloaderException {
        return manager.expandUrl(url);
    }

}
