package esa.mep.downloader.logic;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import esa.mep.downloader.config.DownloaderConfig;
import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.PluginConfiguration;
import esa.mep.downloader.products.ProductDownloadManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * The DownloadManager manage tasks related to a DownloadRequest (i.e. a set of
 * products to download) It maintains the status of the tasks, etc. TODO
 *
 * @author cnl
 */
@Singleton
public class DownloadManager {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DownloadManager.class);

    private Map<String, DownloadTask> downloadTasks;
    private Map<String, ScheduledFuture<?>> taskReleaseScheduledMap;

    @EJB
    private DownloaderConfig config;

    @EJB
    private ProductDownloadManager productManager;

    /**
     * Create a download task (and assign it an identifier) and return it
     *
     * @param request
     * @return
     */
    public DownloadTask createDownloadTask(DownloadRequest request) {
        String identifier = UUID.randomUUID().toString();
        LOGGER.debug("Create download task: " + identifier);

        DownloadTask task = new DownloadTask(request, identifier);

        LOGGER.debug("Create release schedule for task: " + identifier);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(new AutoReleaseDownloadTask(this, identifier), config.getTaskDuration(), TimeUnit.HOURS);
        scheduledExecutorService.shutdown();

        return task;
    }

    /**
     * Start asynchronously the download execution
     *
     * @param task
     */
    @Asynchronous
    public void executeDownloadTask(DownloadTask task) throws DownloaderException {
        LOGGER.debug("Execute the download task: " + task.getIdentifier());
        try {
            productManager.downloadProducts(task);
            if (downloadTasks == null) {
                downloadTasks = new HashMap<String, DownloadTask>();
            }
            downloadTasks.put(task.getIdentifier(), task);
        } catch (URISyntaxException ex) {
            LOGGER.error(ex.getMessage());
            throw new DownloaderException(ex.getMessage());
        } catch (DMPluginException ex) {
            LOGGER.error(ex.getMessage());
            throw new DownloaderException(ex.getMessage());
        }
    }

    public DownloadTask getTask(String identifier) {
        LOGGER.debug("Get download task: " + identifier);
        if (StringUtils.isNotEmpty(identifier) && downloadTasks != null && downloadTasks.containsKey(identifier)) {
            return downloadTasks.get(identifier);
        } else {
            return null;
        }

    }

    public DownloadTask cancelTask(String identifier) {
        LOGGER.debug("Cancel download task: " + identifier);
        if (StringUtils.isNotEmpty(identifier) && downloadTasks.containsKey(identifier)) {
            DownloadTask task = downloadTasks.get(identifier);
            task.cancel();
            removeTask(identifier);
            return task;
        } else {
            LOGGER.debug("No available download task for the identifier " + identifier);
            return null;
        }

    }

    @Lock(LockType.WRITE)
    public void createReleaseSchedule(String identifier) {
        LOGGER.debug("Create release schedule for task : " + identifier);

        if (taskReleaseScheduledMap == null) {
            taskReleaseScheduledMap = new HashMap<String, ScheduledFuture<?>>();
        }

        if (taskReleaseScheduledMap.containsKey(identifier)) {
            LOGGER.debug(String.format("Release schedule of task %s is existing ==> Cancel it !", identifier));
            ScheduledFuture<?> releaseSchedule = taskReleaseScheduledMap.get(identifier);
            boolean ok = releaseSchedule.cancel(true);
            if (ok) {
                LOGGER.debug("The schedule has been cancelled.");
            } else {
                LOGGER.debug("The schedule could not be cancelled.");
            }
            taskReleaseScheduledMap.remove(identifier);

            LOGGER.debug(String.format("Create new schedule for task %s .", identifier));
        }

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> releaseSchedule = scheduledExecutorService.schedule(new AutoReleaseDownloadTask(this, identifier), config.getTaskExpiration(), TimeUnit.MINUTES);
        scheduledExecutorService.shutdown();

        taskReleaseScheduledMap.put(identifier, releaseSchedule);
    }

    @Lock(LockType.WRITE)
    public void removeTask(String taskId) {
        if (StringUtils.isNotEmpty(taskId) && downloadTasks.containsKey(taskId)) {
            downloadTasks.remove(taskId);
            LOGGER.debug("Removed download task = " + taskId);
        }
    }

    public PluginConfiguration[] getConfiguration(String protocol, String[] servers) {
        return productManager.getPluginConfigs(protocol, servers);
    }

    public String[] expandUrl(String url) throws DownloaderException {
        try {
            return productManager.expandUrl(new URI(url));
        } catch (URISyntaxException e) {
            throw new DownloaderException(e.getMessage());
        } catch (DMPluginException e) {
            LOGGER.error(e.getMessage());
            throw new DownloaderException(e.getMessage());
        }

    }
}
