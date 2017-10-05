package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.IDownloadPlugin;
import esa.mep.downloader.plugin.IDownloadPluginInfo;
import esa.mep.downloader.plugin.IDownloadProcess;
import esa.mep.downloader.plugin.IProductDownloadListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;

public class FTPDownloadPlugin implements IDownloadPlugin {

    private static FTPDownloadConfiguration configuration;
    private static Logger log = Logger.getLogger(FTPDownloadPlugin.class);
    /*
     * <Account UUID, Queue<QueueItemId>>
     */
    private static Map<String, ArrayBlockingQueue<String>> threadsQueue = new HashMap<String, ArrayBlockingQueue<String>>();
    private static final int NUMBER_OF_ITEMS_IN_QUEUE = 1000;
    /*
     * <Account UUID, Number of allowed downloads>
     */
    private static Map<String, Integer> currentAvailableConnectionsPerAccountMapping = new HashMap<String, Integer>();
    /*
     * <QueueItemId, Download process info>
     */
    private static Map<String, DownloadProcessInfo> queueItemInfoMapping = new HashMap<String, DownloadProcessInfo>();

    private final static ReentrantReadWriteLock reentrantReadWrite = new ReentrantReadWriteLock();
    private final static Lock readWriteLock = reentrantReadWrite.writeLock();

    public static int SLEEP_TIME = 3000;

    public IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir)
            throws DMPluginException {
        log.debug("Initialize FTPDownloadPlugin (tmpRootDir = " + tmpRootDir.getAbsolutePath()
                + ", pluginCfgRootDir = " + pluginCfgRootDir.getAbsolutePath() + ")");
        try {
            /*
             * load FTP accounts info and initiate
             * currentAvailableConnectionsPerAccountMapping with list of
             * <Account UUID, max connections per account>
             */
            configuration = new FTPDownloadConfiguration(pluginCfgRootDir,
                    currentAvailableConnectionsPerAccountMapping);
        } catch (Exception e) {
            throw new DMPluginException(e);
        }

        return new FTPDownloadPluginInfo(configuration.getFTPAccounts());
    }

    public void terminate() throws DMPluginException {
        log.debug("Terminate FTPDownloadPlugin.");
        threadsQueue.clear();
        queueItemInfoMapping.clear();
        currentAvailableConnectionsPerAccountMapping.clear();
    }

    public IDownloadProcess createDownloadProcess(URI productURI, File downloadDir, String user,
            String password, IProductDownloadListener downloadListener, String proxyLocation,
            int proxyPort, String proxyUser, String proxyPassword) throws DMPluginException {
        log.debug("Enter method createDownloadProcess(productURI = " + productURI
                + ", downloadDir = " + downloadDir.getAbsolutePath() + ", user = " + user + ")");

        FTPDownloadAccount ftpAccount = configuration.getFTPAccount(productURI.toString());

        if (ftpAccount == null) {
            throw new DMPluginException("Please configure FTP account for downloading the URL: " + productURI.toString());
        } else {
            log.debug("current FTP account: " + ftpAccount.debug());
        }

        DownloadProcessInfo downloadProcessInfo = new DownloadProcessInfo(ftpAccount, productURI,
                downloadDir, downloadListener, proxyLocation, proxyPort, proxyUser, proxyPassword);

        return new FTPDownloadProcess(downloadProcessInfo);
    }

    public synchronized static ArrayBlockingQueue<String> getArrayBlockingQueue(
            String accountUUID) {
        readWriteLock.lock();
        try {
            return threadsQueue.get(accountUUID);
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static void addToDownloadQueue(String queueItemId,
            DownloadProcessInfo dwInfo) {
        readWriteLock.lock();
        try {
            /*
             * add to thread queue mapping
             */
            String accountUUID = dwInfo.getAccount().getUuid();
            if (threadsQueue.containsKey(accountUUID)) {
                threadsQueue.get(accountUUID).put(queueItemId);
            } else {
                ArrayBlockingQueue<String> newQueue = new ArrayBlockingQueue<String>(
                        NUMBER_OF_ITEMS_IN_QUEUE);
                newQueue.put(queueItemId);
                threadsQueue.put(accountUUID, newQueue);
            }
            /*
             * add to queue item info mapping
             */
            queueItemInfoMapping.put(queueItemId, dwInfo);
        } catch (InterruptedException iex) {
            log.debug("Error while adding download to the queue: " + iex.getLocalizedMessage());
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static void removeFromThreadsDownloadQueue(String queueItemId,
            DownloadProcessInfo dwInfo) {
        readWriteLock.lock();
        try {
            /*
             * remove from thread queue mapping
             */
            String accountUUID = dwInfo.getAccount().getUuid();
            if (threadsQueue.containsKey(accountUUID)) {
                if (threadsQueue.get(accountUUID).contains(queueItemId)) {
                    threadsQueue.get(accountUUID).remove(queueItemId);
                }
            }
            /*
             * remove from queue item info mapping
             */
            if (queueItemInfoMapping.containsKey(queueItemId)) {
                queueItemInfoMapping.remove(queueItemId);
            }
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static boolean existInThreadsDownloadQueue(String queueItemId,
            DownloadProcessInfo dwInfo) {
        readWriteLock.lock();
        try {
            boolean exist = false;
            String accountUUID = dwInfo.getAccount().getUuid();
            if (threadsQueue.containsKey(accountUUID)) {
                if (threadsQueue.get(accountUUID).contains(queueItemId)) {
                    exist = true;
                }
            }
            return exist;
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static void viewCurrentAvailableConnectionsPerAccountMapping() {
        readWriteLock.lock();
        try {
            log.debug("CURRENT AVAILABLE CONNECTIONS PER ACCOUNT:");
            for (Map.Entry<String, Integer> entry : currentAvailableConnectionsPerAccountMapping
                    .entrySet()) {
                log.debug(entry.getKey() + " = " + entry.getValue());
            }
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static boolean isAllowedDownload(String accountUUID) {
        readWriteLock.lock();
        try {
            int allowedNum = currentAvailableConnectionsPerAccountMapping.get(accountUUID);
            log.debug("Current available connections: " + allowedNum);
            if (allowedNum > 0) {
                currentAvailableConnectionsPerAccountMapping.put(accountUUID, (allowedNum - 1));
                return true;
            } else {
                return false;
            }
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static void increaseAllowedDownload(String accountUUID) {
        readWriteLock.lock();
        try {
            int maxConnection = configuration.getMaxConnectionByUUID(accountUUID);
            int currentAvailConn = currentAvailableConnectionsPerAccountMapping.get(accountUUID);
            log.debug("Max connections = " + maxConnection
                    + ", current available connections before increasing = " + currentAvailConn);
            if (currentAvailConn < maxConnection) {
                currentAvailableConnectionsPerAccountMapping.put(accountUUID,
                        (currentAvailConn + 1));
            }
            currentAvailConn = currentAvailableConnectionsPerAccountMapping.get(accountUUID);
            log.debug("Max connections = " + maxConnection
                    + ", current available connections after increasing = " + currentAvailConn);
        } finally {
            readWriteLock.unlock();
        }
    }

    public synchronized static DownloadProcessInfo takeFromQueueItemInfoMapping(
            String queueItemId) {
        readWriteLock.lock();
        try {
            if (queueItemInfoMapping.containsKey(queueItemId)) {
                DownloadProcessInfo dwInfo = queueItemInfoMapping.get(queueItemId);
                queueItemInfoMapping.remove(queueItemId);
                return dwInfo;
            } else {
                return null;
            }
        } finally {
            readWriteLock.unlock();
        }
    }

    public String[] expandUrl(URI uri) throws DMPluginException {
        try {
            FTPDownloadAccount ftpAccount = configuration.getFTPAccount(uri.toString());

            List<String> urls = new FTPUtils().listFiles(ftpAccount, uri);

            if (urls != null) {
                if (urls.size() > 0) {
                    return urls.toArray(new String[urls.size()]);
                } else {
                    return new String[]{uri.toString()};
                }

            }
            return new String[]{uri.toString()};

        } catch (IOException e) {
            throw new DMPluginException(e);
        }
    }
}
