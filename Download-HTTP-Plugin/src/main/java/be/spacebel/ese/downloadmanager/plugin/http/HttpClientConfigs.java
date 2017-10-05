package be.spacebel.ese.downloadmanager.plugin.http;


import esa.mep.downloader.plugin.EDownloadStatus;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 *
 * @author ane
 */
public abstract class HttpClientConfigs {

    private static final Logger LOG = LogManager.getLogger(HttpClientConfigs.class);

    // PoolingHttpClientConnectionManager settings: limit connections
    public static final int CM_MAX_TOTAL = 200;
    public static final int CM_DEFAULT_MAX_PER_ROUTE = 4;

    // ExecutorService settings (used by PoolingHttpClientConnectionManager) : fix the thread pool size
    public static final int POOL_MAX_THREADS = CM_MAX_TOTAL;

    // IdleConnectionMonitor settings: set the number of milliseconds to wait between two checks (set -1 to disable the monitor)
    public static final int IDLE_CONNECTION_MONITOR_SCHEDULE = 60000;


    /**
     * Creates a simple fixed thread pool ExecutorService.
     * The number of threads is fixed by POOL_MAX_THREADS.
     *
     * @return fixed thread pool ExecutorService.
     */
    public static ExecutorService createDefaultExecutorService() {
        return Executors.newFixedThreadPool(HttpClientConfigs.POOL_MAX_THREADS);
    }


    /**
     * Could be useful in the future.
     * Creates an ExecutorService with fixed thread pool and a bounded queue.
     * The thread pool is fixed by POOL_MAX_THREADS.
     * The queue is handled by a LinkedBlockingDeque.
     *
     * @return fixed thread pool + bounded queue ExecutorService.
     */
    public static ExecutorService createBoundedQueueExecutorService() {
        BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<>();
        return new ThreadPoolExecutor(1, POOL_MAX_THREADS, 60, TimeUnit.SECONDS, linkedBlockingDeque, new ThreadPoolExecutor.CallerRunsPolicy());
    }


    /**
     * @return Connection Manager with default configuration.
     */
    public static PoolingHttpClientConnectionManager createDefaultCM() {
        // init the SSL & plain connection socket factories
        SSLContext sslCtxt = SSLContexts.createSystemDefault();
        SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(sslCtxt);
        ConnectionSocketFactory plainsf = new PlainConnectionSocketFactory();

        // parameters for the connection manager
        int maxTotal = CM_MAX_TOTAL;
        int defaultMaxPerRoute = CM_DEFAULT_MAX_PER_ROUTE;

        // need to provide a SSLConnectionSocketFactory to the connection manager in order to handle HTTPS scheme
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf)
                .register("https", sslcsf)
                .build();

        // init the connection manager
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(reg);
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return connManager;
    }


    /**
     * @param maxPerHostList List of host info containing the hostname and its max connections setting.
     * @return Connection Manager configured with max connections per route from given hosts.
     */
    public static PoolingHttpClientConnectionManager createMaxPerHostCM(List<HTTPHostInfo> maxPerHostList) {
        // init the SSL & plain connection socket factories
        SSLContext sslCtxt = SSLContexts.createSystemDefault();
        SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(sslCtxt);
        ConnectionSocketFactory plainsf = new PlainConnectionSocketFactory();

        // parameters for the connection manager
        int maxTotal = CM_MAX_TOTAL;
        int maxPerRoute = CM_DEFAULT_MAX_PER_ROUTE;

        // need to provide a SSLConnectionSocketFactory to the connection manager in order to handle HTTPS scheme
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf)
                .register("https", sslcsf)
                .build();

        // init the connection manager
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(reg);
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(maxPerRoute);
        // set max connection for given hosts
        for (HTTPHostInfo hostInfo : maxPerHostList) {
            HttpRoute route = createHttpRoute(hostInfo.getServer(), hostInfo.getProtocol());
            connManager.setMaxPerRoute(route, hostInfo.getMaxConnections());
            LOG.debug("Set MaxPerRoute to connection manager: [host='" + route.getTargetHost().getHostName()
                    + "', protocol=" + route.getTargetHost().toURI() + ", value=" + connManager.getMaxPerRoute(route) + ", hashcode=" + route.hashCode() + "]");
        }

        return connManager;
    }


    /**
     * For logging purpose.
     * Prints HTTP request status.
     *
     * @return an interceptor to print HTTP request status.
     */
    public static HttpRequestInterceptor createLogReqInterceptor() {
        HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest hr, HttpContext hc) throws HttpException, IOException {
                hc.setAttribute("time", System.currentTimeMillis());
                LogManager.getLogger(HttpClientController.class).debug("--- HTTP request status = " + hr.getRequestLine().toString());
                 LogManager.getLogger(HttpClientController.class).debug("--- HTTP request status = " + hr.toString());
            }
        };
        return requestInterceptor;
    }


    /**
     * For logging purpose.
     * Prints HTTP response status.
     *
     * @return an interceptor to print HTTP response status.
     */
    public static HttpResponseInterceptor createLogRespInterceptor() {
        HttpResponseInterceptor responseInterceptor = new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse hr, HttpContext hc) throws HttpException, IOException {
                long elapsed = System.currentTimeMillis() - (long) hc.getAttribute("time");
                hc.setAttribute("time", System.currentTimeMillis());
                LogManager.getLogger(HttpClientController.class).debug("--- HTTP response status = " + hr.getStatusLine().toString() + ", elapsed time = " + elapsed);
                 LogManager.getLogger(HttpClientController.class).debug("--- HTTP response status = " + hr.toString());
            }
        };
        return responseInterceptor;
    }


    /**
     * Not required.
     * Adds HTTP response status to HttpContext object.
     * Could be used to retrieve and use the HTTP response code in application.
     * (HTTP error codes are already reported by the HTTPDownloadCallback by using exception thrown by the ResponseHandler).
     *
     * @return an interceptor to put HTTP response status in HttpContext.
     */
    public static HttpResponseInterceptor createHttpCtxtInRespIntercept() {
        HttpResponseInterceptor responseInterceptor = new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse hr, HttpContext hc) throws HttpException, IOException {
                long elapsed = System.currentTimeMillis() - (long) hc.getAttribute("time");
                hc.setAttribute("http_status", "HTTP response code " + hr.getStatusLine().getStatusCode() + " " + hr.getStatusLine().getReasonPhrase());
            }
        };
        return responseInterceptor;
    }


    /**
     * Create Response Handler for a HTTP request that handle the file downloading.
     * It uses given to prepare the file to write.
     * When the download is finished, it returns the given DownloadProcessInfo which identifies the process.
     *
     * @param dlInfo All required info to write the file to download.
     * @param dpi    The info about the download process.
     * @return ResponseHandler that check & process the HTTP response and copy its content to file.
     */
    public static ResponseHandler createDownloadResponseHandler(final HTTPDownloadInfo dlInfo, final DownloadProcessInfo dpi) {
        return new ResponseHandler<DownloadProcessInfo>() {
            @Override
            public DownloadProcessInfo handleResponse(final HttpResponse hr) throws ClientProtocolException, IOException {
                // check status code and handle HTTP errors
                StatusLine status = hr.getStatusLine();
                if (status.getStatusCode() < 200 || status.getStatusCode() >= 300) {
                    throw new HttpResponseException(status.getStatusCode(), "HTTP response code " + status.getStatusCode() + " " + status.getReasonPhrase());
                }

                // check response has content
                HttpEntity entity = hr.getEntity();
                if (entity == null) {
                    throw new ClientProtocolException("HTTP response has no content.");
                }

                // find the file name and type in headers
                String headerFilename = null;
                String headerContentType = null;
                Header[] headers = hr.getAllHeaders();
                for (Header h : headers) {
                    if (h.getName().equalsIgnoreCase("content-disposition")) {
                        lookForFilename:
                        for (HeaderElement hEl : h.getElements()) { // should have only one element (the "content-disposition")
                            for (NameValuePair param : hEl.getParameters()) {
                                if (param.getName().equalsIgnoreCase("filename")) {
                                    headerFilename = param.getValue();
                                    break lookForFilename;
                                }
                            }
                        }
                    }
                    if (h.getName().equalsIgnoreCase("content-type")) {
                        headerContentType = h.getValue();
                    }
                    if (h.getName().equalsIgnoreCase("content-length")) {
                        dlInfo.setContentLength(Long.parseLong(h.getValue()));
                    }
                }
                // check whether filename exists in headers
                String filename;
                if (headerFilename != null && !headerFilename.isEmpty()) {
                    filename = headerFilename;
                } else {
                    // use filename defined in URI if it exists (look if there is an extension)
                    String lastPathElement = FilenameUtils.getName(dlInfo.getUri().getPath());
                    if (!FilenameUtils.getExtension(lastPathElement).isEmpty()) {
                        filename = lastPathElement;
                    } else {
                        // generate a name (without extension)
                        filename = UUID.randomUUID().toString();
                        // if headers have "Content-Type", then use it to add an extension to the file name
                        if (headerContentType != null && !headerContentType.isEmpty()) {
                            String extension = headerContentType.split("/")[1];
                            filename = filename.concat("." + extension);
                        }
                    }
                }

                // set the path where the file will be downloaded
                String newPath = FilenameUtils.concat(dlInfo.getFile().getPath(), filename);
                dlInfo.setFile(new File(newPath));

                // update the download task status
                dlInfo.setStatus(EDownloadStatus.RUNNING);
                // start the download
                LOG.debug("Downloaded file path = " + dlInfo.getFile().getAbsolutePath());
                FileUtils.copyInputStreamToFile(entity.getContent(), dlInfo.getFile());
                // close the streams safely
                EntityUtils.consume(entity);

                // return DownloadProcessInfo to be used in HTTPDownloadCallback
                return dpi;
            }
        };
    }


    /**
     * Creates a HttpRoute object that specifies a direct route between client and host.
     *
     * @param hostname The name of the host to join.
     * @return The HTTP route to the host.
     */
    private static HttpRoute createHttpRoute(String hostname, String protocol) {
        HttpRoute route;
        if (protocol.equalsIgnoreCase("https")) {
            HttpHost host = new HttpHost(hostname, 443, "https");
            route = new HttpRoute(host, // the host to which to route
                    null, // (proxy setting) the local address to route from
                    (HttpHost) null, // the proxy to use
                    true, // if the route is secure (HTTPS)
                    RouteInfo.TunnelType.PLAIN, // if route is tunnelled (vs plain) via the proxy
                    RouteInfo.LayerType.PLAIN); // (proxy tunnel setting) if the route includes a layered protocol

        } else {
            HttpHost host = new HttpHost(hostname);
            route = new HttpRoute(host);
        }
        return route;
    }

}
