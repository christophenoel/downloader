package be.spacebel.ese.downloadmanager.plugin.http;

import esa.downloader.config.DownloaderConfig;
import esa.mep.downloader.plugin.EDownloadStatus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionMetrics;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author ane
 */
public final class HttpClientController {

    private static final Logger LOG = LogManager.getLogger(
            HttpClientController.class);

    private final CloseableHttpClient httpClient;
    private final PoolingHttpClientConnectionManager connManager;
    private final FutureRequestExecutionService futureRequestExecutionService;
    private final FutureRequestExecutionMetrics metrics;
    private final ExecutorService executorService;
    private final IdleConnectionMonitor idleConnectionMonitor;
    private final Thread idleConnectionMonitorT;

    private final Map<DownloadProcessInfo, TaskInfo> taskMap;

    /**
     * Initialises the controller of HttpClient. Only single one instance must
     * exist.
     *
     * @param pluginConfig The plugin configuration that is required to retrieve
     * settings for HttpClient's connection manager.
     * @throws Exception
     */
    public HttpClientController(HTTPDownloadConfiguration pluginConfig) throws Exception {
        // set the connection manager
        if (pluginConfig == null) {
            connManager = HttpClientConfigs.createDefaultCM();
        } else {
            List<HTTPHostInfo> maxPerHostList = new ArrayList<>();
            for (HTTPHostInfo hostInfo : pluginConfig.getListOfHostInfos()) {
                // only hosts with max connections setting are added to connection manager config
                if (hostInfo.getMaxConnections() > 0) {
                    maxPerHostList.add(hostInfo);
                }
            }
            connManager = HttpClientConfigs.createMaxPerHostCM(maxPerHostList);
        }
        HttpClientBuilder preparedClient;
        // init a custom HTTP Client
        if (LOG.isDebugEnabled()) {

            preparedClient = HttpClients.custom()
                    .addInterceptorLast(
                            HttpClientConfigs.createLogReqInterceptor())
                    .addInterceptorLast(
                            HttpClientConfigs.createLogRespInterceptor())
                    .setConnectionManager(connManager);

        } else {
            preparedClient = HttpClients.custom()
                    .setConnectionManager(connManager);

        }

        DownloaderConfig dc = new DownloaderConfig();
        dc.loadValues();
        LOG.info("Checking proxy configuration");
        if (dc.getProxyHost() != null && !dc.getProxyHost().isEmpty()) {
            HttpHost proxyHTTP;
            proxyHTTP = new HttpHost(dc.getProxyHost(), dc.getProxyPort());
            preparedClient.setProxy(proxyHTTP);
            LOG.info("Prepared client for proxy host"+dc.getProxyHost());
        }
        else {
            LOG.info("No configuration for HTTP proxy: skipping.");
        }
        httpClient = preparedClient.build();
        // init ExecutorService to manage async tasks
        executorService = HttpClientConfigs.createDefaultExecutorService();

        // init FutureRequestExecutionService to make async request by using ExecutorService
        this.futureRequestExecutionService = new FutureRequestExecutionService(
                httpClient, executorService);

        // retrieve Metrics
        this.metrics = futureRequestExecutionService.metrics();

        // prepare tasks container
        this.taskMap = new ConcurrentHashMap<>();

        // instantiate an idle connection monitor to kill expired/idle connections
        if (HttpClientConfigs.IDLE_CONNECTION_MONITOR_SCHEDULE > 0) {
            this.idleConnectionMonitor = new IdleConnectionMonitor(
                    connManager,
                    HttpClientConfigs.IDLE_CONNECTION_MONITOR_SCHEDULE);
            // start the idle connection monitor
            idleConnectionMonitorT = new Thread(idleConnectionMonitor);
            idleConnectionMonitorT.start();
        }
    }

    /**
     * Creates a new future task to execute the download process. the task is
     * automatically started. The task is added to a local task map to track the
     * processes.
     *
     * @param downloadProcess The process reference to allow external control
     * over the task.
     * @param processInfo The info required by the download process
     * @return An HTTPDownloadInfo containing info that are continuously updated
     * during the download process.
     */
    public synchronized HTTPDownloadInfo addDownloadTask(
            IHTTPDownloadProcess downloadProcess,
            DownloadProcessInfo processInfo) {
        HTTPDownloadInfo dlInfo = new HTTPDownloadInfo(
                EDownloadStatus.NOT_STARTED,
                new File(processInfo.getDownloadDir().toString()),
                processInfo.getProductURI(),
                HttpClientContext.create());
        LOG.debug(
                "Creating futur task for '" + dlInfo.getUri().getPath() + "@" + dlInfo.getUri().getHost() + "'");
        // Use HTTP URI Request for all special request (e.g. with headers from the USGS plugin)
        HttpUriRequest request = new HttpGet(dlInfo.getUri());

        LOG.debug("Process Info check");
        if (processInfo.getHttpUriRequest() != null) {
            LOG.debug("Process Info NOT NULL request contains security cookie");
            // fundamental to clear the session Id cookie..
            dlInfo.getHttpContext().setCookieStore(new BasicCookieStore());

            request = processInfo.getHttpUriRequest();

            System.out.println("Downloading " + request.getURI().toString());
            for (Header h : request.getAllHeaders()) {
                LOG.debug("Header:" + h.getName() + " value:" + h.getValue());
            }

        }

        HttpRequestFutureTask<DownloadProcessInfo> task = futureRequestExecutionService.execute(
                request,
                dlInfo.getHttpContext(),
                HttpClientConfigs.createDownloadResponseHandler(dlInfo,
                        processInfo),
                new HTTPDownloadCallback(downloadProcess, dlInfo));
        taskMap.put(processInfo, new TaskInfo(dlInfo, task));
        return dlInfo;
    }

    /**
     * Removes the future task associated with given download process info from
     * local task map. The task is not canceled automatically, to do so the
     * "stopTask" must be set to true.
     *
     * @param processInfo The info about the download process.
     * @param stopTask Set to true if the task must be canceled.
     *
     * @return true if "cancel" hast been invoked on the task and the task is
     * removed from the map. Otherwise, false if the task is not found.
     */
    public synchronized boolean removeDownloadTask(
            DownloadProcessInfo processInfo, boolean stopTask) {
        TaskInfo taskInfo = taskMap.remove(processInfo);
        if (taskInfo == null) {
            LOG.debug(
                    "Cannot remove task, no task found for '" + processInfo.getProductURI().getPath() + "@" + processInfo.getProductURI().getHost() + "'.");
            return false;
        } else {
            if (stopTask) {
                taskInfo.task.cancel(true);
            }
            return true;
        }
    }

    /**
     * For logging purpose.
     *
     * @return metrics about connections (provided by the connection manager).
     */
    public FutureRequestExecutionMetrics getMetrics() {
        return metrics;
    }

    /**
     * For logging purpose.
     *
     * @return statistics about tasks (provided by the connection manager).
     */
    public PoolStats getPoolStats() {
        return connManager.getTotalStats();
    }

    /**
     * Cleanly shutdown all resources. It closes: the Idle Connection Monitor
     * thread; the connection manager; the HttpClient; and the future executor
     * services.
     *
     * @throws IOException in case a thread failed to stop.
     */
    public void shutdown() throws IOException {
        if (idleConnectionMonitor != null) {
            idleConnectionMonitor.shutdown();
        }
        if (connManager != null) {
            connManager.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
        if (futureRequestExecutionService != null) {
            futureRequestExecutionService.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * TaskInfo wraps Download Info (track progress of download) and Future Task
     * objects. It is used with the local task map.
     */
    private class TaskInfo {

        private final HTTPDownloadInfo dlInfo;
        private final HttpRequestFutureTask<DownloadProcessInfo> task;

        public TaskInfo(HTTPDownloadInfo dlInfo,
                HttpRequestFutureTask<DownloadProcessInfo> task) {
            this.dlInfo = dlInfo;
            this.task = task;
        }

    }

}
