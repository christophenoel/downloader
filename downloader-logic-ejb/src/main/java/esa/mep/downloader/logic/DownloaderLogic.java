package esa.mep.downloader.logic;

import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadStatus;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;

/**
 *
 * @author martin
 */
@Stateless
@LocalBean
public class DownloaderLogic {

    @EJB
    /**
     * DownloadManager handles all operations related to the handling of a DownloadRequest
     * It self invoke the ProductDownloadManager handling individual product URL downloads
     */
    private DownloadManager manager;

    /**
     * Start the handling of a DownloadRequest by the DownloadManager
     * @param request the download request including products URLs.
     * @return the task unique identifier
     */
    public String download(DownloadRequest request) {
        // create the download task (and implicitly it will create productDownloadTasks)
        DownloadTask task = manager.createDownloadTask(request);
        // start the download task
        manager.executeDownloadTask(task);
        return task.getIdentifier();
    }
    
    /**
     * Get the status document of the given download task.
     * @param identifier download task identifier
     * @return the download status document
     */
    public DownloadStatus getStatus(String identifier) {
        return manager.getTask(identifier).getStatus();
        
    }
    
    /**
     * Cancel the download task
     * @param identifier download task identifier
     * @return download status document
     */
    public DownloadStatus cancel(String identifier) {
        return manager.cancelTask(identifier).getStatus();
    }

}
