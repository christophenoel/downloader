package esa.mep.downloader.plugin.usgs;

import be.spacebel.ese.downloadmanager.plugin.http.DownloadProcessInfo;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPHostInfo;
import be.spacebel.ese.downloadmanager.plugin.http.HttpClientController;
import esa.downloader.config.DownloaderConfig;
import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.IDownloadPlugin;
import esa.mep.downloader.plugin.IDownloadPluginInfo;
import esa.mep.downloader.plugin.IDownloadProcess;
import esa.mep.downloader.plugin.IProductDownloadListener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpHost;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cnl
 */
public class USGSDownloadPlugin implements IDownloadPlugin {

    private USGSPluginConfiguration pluginConfig;
    
    private HttpClientController httpClientController;
    private static final Logger LOG = LogManager.getLogger(
            USGSDownloadPlugin.class);
    private HtmlUnitDriver driver;

    @Override
    public IDownloadPluginInfo initialize(File tmpRootDir, File pluginCfgRootDir) throws DMPluginException {
        // Load the USGS Configuration (from  usgs-account.xml)
        if (pluginConfig == null) {
            pluginConfig = new USGSPluginConfiguration(pluginCfgRootDir);
        }
        this.driver = new HtmlUnitDriver();
         DownloaderConfig dc = new DownloaderConfig();
        dc.loadValues();
         LOG.info("Checking proxy configuration");
        if (dc.getProxyHost() != null && !dc.getProxyHost().isEmpty()) {
           this.driver.setProxy(dc.getProxyHost(), dc.getProxyPort());
            LOG.info("Selenium configured with proxy host"+dc.getProxyHost());
        }
        
        // initialize Selenium dreicver
               // Initialize HTTP Controller
        if (httpClientController == null) {
            try {
                // Create an HTTP Configuration (only server and maxConnections are used)
                HTTPHostInfo hostInfo = new HTTPHostInfo();
                hostInfo.setServer(pluginConfig.hostInfo.getServer());
                hostInfo.setProtocol("HTTP");
                hostInfo.setMaxConnections(
                        pluginConfig.hostInfo.getMaxConnections());
                HTTPDownloadConfiguration httpconfig = new HTTPDownloadConfiguration(
                        hostInfo);
                httpClientController = new HttpClientController(httpconfig);
            } catch (Exception ex) {
                LOG.error("Failed to initialise USGSHttpClientController.", ex);
                // cleanly shutdown in case HttpClientController was partially initialised
                terminate();
                httpClientController = null;
                throw new DMPluginException(
                        "Failed to initialise USGSDownloadPlugin.", ex);
            }
        }
        return new USGSDownloadPluginInfo(pluginConfig);
    }

    public IDownloadProcess createDownloadProcess(URI productURI,
            File downloadDir, String user, String password,
            IProductDownloadListener downloadListener, String proxyLocation,
            int proxyPort, String proxyUser, String proxyPassword) throws DMPluginException {
        // Create a download process info
        DownloadProcessInfo processInfo = new DownloadProcessInfo(productURI,
                downloadDir, user, password, proxyLocation, proxyPort,
                proxyUser,
                proxyPassword);
        // create process and include the USGS user password
        USGSDownloadProcess process = new USGSDownloadProcess(driver,downloadListener,
                 httpClientController, processInfo,
                pluginConfig.hostInfo.getUsername(),
                pluginConfig.hostInfo.getPassword());
        return process;
    }

    public void terminate() throws DMPluginException {
        if (httpClientController != null) {
            try {
                // cleanly shutdown connection manager, thread pool, and other threads
                httpClientController.shutdown();
                httpClientController = null;
            } catch (IOException ex) {
                throw new DMPluginException(ex);
            }
        }
    }

    @Override
    public String[] expandUrl(URI uri) throws DMPluginException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
