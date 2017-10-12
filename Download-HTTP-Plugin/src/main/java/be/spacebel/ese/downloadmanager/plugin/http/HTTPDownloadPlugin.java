package be.spacebel.ese.downloadmanager.plugin.http;

import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.IDownloadPlugin;
import esa.mep.downloader.plugin.IDownloadPluginInfo;
import esa.mep.downloader.plugin.IDownloadProcess;
import esa.mep.downloader.plugin.IProductDownloadListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class HTTPDownloadPlugin implements IDownloadPlugin {

    private static final Logger LOG = LogManager.getLogger(HTTPDownloadPlugin.class);

    private static HttpClientController httpClientController;
    private static HTTPDownloadConfiguration pluginConfig;

    /**
     * For debug purpose.
     *
     * @deprecated only use this method to debug the HttpClientController
     *
     * @param httpClientController provided HttpClientController
     * @param pluginConfig provided HTTPDownloadConfiguration
     * @throws IOException
     */
//    public HTTPDownloadPlugin(final HttpClientController httpClientController, final HTTPDownloadConfiguration pluginConfig) throws IOException {
    public void initDebugMode(final HttpClientController httpClientController, final HTTPDownloadConfiguration pluginConfig) throws IOException {
        HTTPDownloadPlugin.httpClientController = httpClientController;
        if (pluginConfig != null) {
            HTTPDownloadPlugin.pluginConfig = pluginConfig;
        }
    }

    @Override
    public IDownloadProcess createDownloadProcess(URI productURI, File downloadDir, String user, String password,
            IProductDownloadListener downloadListener, String proxyLocation, int proxyPort, String proxyUser, String proxyPassword) throws DMPluginException {
        LOG.debug("downloadDir = " + downloadDir.getAbsolutePath() + ", productURI = " + productURI);
        if (httpClientController == null) {
            throw new IllegalStateException("HTTPDownloadPlugin is not correctly initialised.");
        }

        // check if credentials exist for URL
        if (productURI.getUserInfo() == null) {
            LOG.debug("The credentials exist in the URL.");
            // check if user/password are provided
            if (user != null) {
                try {
                    productURI = new URIBuilder(productURI).setUserInfo(user, password).build();
                } catch (URISyntaxException ex) {
                    throw new DMPluginException("URI is incorrect: " + ex.getClass().getName());
                }
            } else {
                // check the host info in plugin configuration
                HTTPHostInfo hostInfo = pluginConfig.getHostInfo(productURI.getHost());
                if (hostInfo != null && hostInfo.getUsername() != null && !hostInfo.getUsername().trim().isEmpty()) {
                    try {
                        productURI = new URIBuilder(productURI).setUserInfo(hostInfo.getUsername(), hostInfo.getPassword()).build();
                    } catch (URISyntaxException ex) {
                        throw new DMPluginException("URI is incorrect: " + ex.getClass().getName());
                    }
                }
            }
        }else{
            LOG.debug("The credentials don't exist in the URL.");
        }

        LOG.debug("productURI = " + productURI);

        // create the download process and return its reference
        DownloadProcessInfo processInfo = new DownloadProcessInfo(productURI, downloadDir, user, password, proxyLocation, proxyPort, proxyUser, proxyPassword);
        HTTPDownloadProcess dlProcess = new HTTPDownloadProcess(downloadListener, processInfo, httpClientController);
        return dlProcess;
    }

    @Override
    public void terminate() throws DMPluginException {
        if (httpClientController != null) {
            try {
                // cleanly shutdown connection manager, thread pool, and other threads
                httpClientController.shutdown();
                HTTPDownloadPlugin.httpClientController = null;
            } catch (IOException ex) {
                throw new DMPluginException(ex);
            }
        }
    }

    @Override
    public IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir) throws DMPluginException {
        // only one instance of HttpClient controller must exist
        if (httpClientController == null) {
            if (pluginConfig == null) {
                pluginConfig = new HTTPDownloadConfiguration(pluginCfgRootDir);
            }
            try {
                HTTPDownloadPlugin.httpClientController = new HttpClientController(pluginConfig);
            } catch (Exception ex) {
                LOG.error("Failed to initialise HttpClientController.", ex);
                // cleanly shutdown in case HttpClientController was partially initialised
                terminate();
                HTTPDownloadPlugin.httpClientController = null;
                throw new DMPluginException("Failed to initialise HTTPDownloadPlugin.", ex);
            }
        }
        return new HTTPDownloadPluginInfo(pluginConfig.getListOfHostInfos());
    }

    @Override
    public String[] expandUrl(URI uri) throws DMPluginException {
        return new String[]{uri.toString()};
    }

}
