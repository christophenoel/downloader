package esa.mep.downloader.plugin.usgs;

import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadConfiguration;
import esa.mep.downloader.plugin.IDownloadPluginInfo;
import esa.mep.downloader.plugin.PluginConfiguration;
import org.slf4j.LoggerFactory;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cnl
 */
public class USGSDownloadPluginInfo implements IDownloadPluginInfo{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(USGSDownloadProcess.class);
    private final String hostname;

    public String getHostname() {
        return hostname;
    }

    USGSDownloadPluginInfo(USGSPluginConfiguration pluginConfig) {
       this.hostname=pluginConfig.hostInfo.getServer();
                     
               }

    public String getName() {
        return "USGS Downloader Plugin";
    }

    public int[] getPluginVersion() {
       return new int[]{1, 0, 0};
    }

    public String[] getMatchingPatterns() {
        //earthexplorer.usgs.gov
        log.debug("matching pattern based on "+this.getHostname());
        return new String[]{"http://"+this.getHostname()+".*", "HTTP://"+this.getHostname()+".*","https://"+this.getHostname()+".*", "HTTPS://"+this.getHostname()+".*" };
    }

    public int[] getDMMinVersion() {
        return new int[]{0, 7, 0};
    }

    public boolean handlePause() {
        return false;
    }

    @Override
    public PluginConfiguration[] getConfigurations(String[] servers) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PluginConfiguration.Protocol getProtocol() {
        return PluginConfiguration.Protocol.HTTP;
    }
    
}
