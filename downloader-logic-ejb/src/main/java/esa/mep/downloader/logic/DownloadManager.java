package esa.mep.downloader.logic;



import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadStatus;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProductStatusType;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProgressType;
import esa.mep.downloader.products.ProductDownload;
import esa.mep.downloader.products.ProductDownloadManager;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * The DownloadManager manage tasks related to a DownloadRequest (i.e. a set of products to download)
 * It maintains the status of the tasks, etc. TODO
 * @author cnl
 */
@Singleton
public class DownloadManager {
    
    private Map<String,DownloadTask> downloadTasks;

    @EJB
    private ProductDownloadManager productManager;
    
    /**
     * Create a download task (and assign it an identifier) and return it
     * @param request
     * @return 
     */
    public DownloadTask createDownloadTask(DownloadRequest request) {
        String identifier = UUID.randomUUID().toString();
        DownloadTask task = new DownloadTask(request,identifier);
        return task;
    }

    /**
     * Start asynchronously the download execution
     * @param task 
     */
    @Asynchronous
    public void executeDownloadTask(DownloadTask task) {
        try {
            productManager.downloadProducts(task);
        } catch (URISyntaxException ex) {
            Logger.getLogger(DownloadManager.class.getName()).log(Level.SEVERE, null, ex);
            // TODO create Exception Status
        } catch (DMPluginException ex) {
            Logger.getLogger(DownloadManager.class.getName()).log(Level.SEVERE, null, ex);
            // TODO create Exception Status
        }
    }

    public DownloadTask getTask(String identifier) {
        return downloadTasks.get(identifier);
    }

     public DownloadTask cancelTask(String identifier) {
        DownloadTask task = downloadTasks.get(identifier);
        task.cancel();
        downloadTasks.remove(task);
        return task;
    }


   
}
