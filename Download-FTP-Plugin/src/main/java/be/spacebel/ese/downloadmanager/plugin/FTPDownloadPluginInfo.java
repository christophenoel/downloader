package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.plugin.IDownloadPluginInfo;
import esa.mep.downloader.plugin.PluginConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class FTPDownloadPluginInfo implements IDownloadPluginInfo {

    private final Map<String, FTPDownloadAccount> ftpAccounts;

    private Logger log = Logger.getLogger(getClass());

    public FTPDownloadPluginInfo(List<FTPDownloadAccount> newFtpAccounts) {
        this.ftpAccounts = new HashMap<String, FTPDownloadAccount>();

        if (newFtpAccounts != null) {
            for (FTPDownloadAccount ftpAccount : newFtpAccounts) {
                this.ftpAccounts.put(ftpAccount.getBase(), ftpAccount);
            }
        }
    }

    public String getName() {
        log.debug("Enter method getName().");
        return "Downloader FTP Plugin";
    }

    public int[] getPluginVersion() {
        log.debug("Enter method getPluginVersion().");
        return new int[]{1, 0, 0};
    }

    public String[] getMatchingPatterns() {
        log.debug("Enter method getMatchingPatterns().");
        return new String[]{"ftp://.*", "FTP://.*"};
    }

    public int[] getDMMinVersion() {
        log.debug("Enter method getDMMinVersion().");
        return new int[]{0, 7, 0};
    }

    public boolean handlePause() {
        log.debug("Enter method handlePause().");
        return false;
    }

    public PluginConfiguration[] getConfigurations(String[] servers) {
        List<PluginConfiguration> configs = new ArrayList<PluginConfiguration>();

        if (servers != null && servers.length > 0) {
            for (String server : servers) {
                if (ftpAccounts.containsKey(server)) {
                    configs.add(toPluginConfiguration(ftpAccounts.get(server)));
                }
            }
        } else {
            for (Map.Entry<String, FTPDownloadAccount> entry : ftpAccounts.entrySet()) {
                configs.add(toPluginConfiguration(entry.getValue()));
            }
        }

        return configs.toArray(new PluginConfiguration[configs.size()]);

    }

    private PluginConfiguration toPluginConfiguration(FTPDownloadAccount ftpAccount) {
        PluginConfiguration pConfig = new PluginConfiguration();
        pConfig.setName(ftpAccount.getBase());
        pConfig.setUser(ftpAccount.getUser());
        pConfig.setProtocol(PluginConfiguration.Protocol.FTP);
        pConfig.setMaxConnections(ftpAccount.getMaxConnections());
        return pConfig;
    }

    public PluginConfiguration.Protocol getProtocol() {
        return PluginConfiguration.Protocol.FTP;
    }
}
