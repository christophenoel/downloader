package be.spacebel.ese.downloadmanager.plugin.http;

import esa.mep.downloader.plugin.IDownloadPluginInfo;
import esa.mep.downloader.plugin.PluginConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPDownloadPluginInfo implements IDownloadPluginInfo {

    private final Map<String, HTTPHostInfo> httpAccounts;

    public HTTPDownloadPluginInfo(List<HTTPHostInfo> newHttpAccounts) {
        this.httpAccounts = new HashMap<String, HTTPHostInfo>();

        if (newHttpAccounts != null) {
            for (HTTPHostInfo httpAccount : newHttpAccounts) {
                this.httpAccounts.put(httpAccount.getServer(), httpAccount);
            }
        }
    }

    @Override
    public String getName() {
        return "ESE HTTP Download Plugin";
    }

    @Override
    public int[] getPluginVersion() {
        return new int[]{1, 0, 0};
    }

    @Override
    public String[] getMatchingPatterns() {
        return new String[]{"http://.*", "HTTP://.*", "https://.*", "HTTPS://.*"};
    }

    @Override
    public int[] getDMMinVersion() {
        return new int[]{0, 7, 0};
    }

    @Override
    public boolean handlePause() {
        return false;
    }

    @Override
    public PluginConfiguration[] getConfigurations(String[] servers) {
        List<PluginConfiguration> configs = new ArrayList<PluginConfiguration>();

        if (servers != null && servers.length > 0) {
            for (String server : servers) {
                if (httpAccounts.containsKey(server)) {
                    configs.add(toPluginConfiguration(httpAccounts.get(server)));
                }
            }
        } else {
            for (Map.Entry<String, HTTPHostInfo> entry : httpAccounts.entrySet()) {
                configs.add(toPluginConfiguration(entry.getValue()));
            }
        }

        return configs.toArray(new PluginConfiguration[configs.size()]);
    }

    private PluginConfiguration toPluginConfiguration(HTTPHostInfo httpAccount) {
        PluginConfiguration pConfig = new PluginConfiguration();
        pConfig.setName(httpAccount.getServer());
        pConfig.setUser(httpAccount.getUsername());
        pConfig.setProtocol(PluginConfiguration.Protocol.HTTP);
        pConfig.setMaxConnections(httpAccount.getMaxConnections());
        return pConfig;
    }

    @Override
    public PluginConfiguration.Protocol getProtocol() {
        return PluginConfiguration.Protocol.HTTP;
    }

}
