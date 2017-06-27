/*
 * Copyright 2017 cnl.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package esa.mep.downloader.plugin;

import be.spacebel.ese.downloadmanager.plugin.FTPDownloadPlugin;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadPlugin;
import esa.mep.downloader.plugin.usgs.USGSDownloadPlugin;
import esa.mep.downloader.config.DownloaderConfig;
import esa.mep.downloader.exception.DMPluginException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cnl
 */
@Singleton
public class PluginFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginFactory.class);
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    @EJB
    private DownloaderConfig config;

    /**
     * List of plugins classes: IDownloadPluginInfo, IDownloadPlugin, and a map
     * between names and IDownloadPlugin
     */
    private List<IDownloadPluginInfo> downloadPluginInfoList;
    private Map<String, IDownloadPlugin> mapOfPluginNamesToPlugins;

    @PostConstruct
    public void initPlugins() {
        LOGGER.debug("Initialize PluginFactory");
        String tmp = System.getProperty(JAVA_IO_TMPDIR);
        LOGGER.debug("Tmp dir = " + tmp);

        Path tmpRootDir = Paths.get(tmp);

        Path pluginCfgRootDir = config.getPluginConfigurationDirectory();
        downloadPluginInfoList = new ArrayList<>();
        mapOfPluginNamesToPlugins = new HashMap<>();

        for (Class<? extends IDownloadPlugin> c : getPluginsClasses()) {
            try {
                IDownloadPlugin plugin = getPluginInstance(c);
                IDownloadPluginInfo downloadPluginInfo = plugin.initialize(tmpRootDir.toFile(), pluginCfgRootDir.toFile());
                downloadPluginInfoList.add(downloadPluginInfo);
                mapOfPluginNamesToPlugins.put(downloadPluginInfo.getName(), plugin);
            } catch (IllegalAccessException e) {
                LOGGER.error(String.format("Error when creating a new instance of a plugin: %s", e.getMessage()));

            } catch (InstantiationException e) {
                LOGGER.error(String.format("Error when creating a new instance of a plugin: %s", e.getMessage()));
            } catch (DMPluginException e) {
                LOGGER.error(String.format("Error when creating a new instance of a plugin: %s", e.getMessage()));
            }
        }
    }

    /**
     * Get the relevant plugin for a specific url (check url pattern)
     *
     * @param url the product url
     * @return the relevant plugin
     */
    public IDownloadPlugin getPlugin(String url) {
        LOGGER.debug("Determine a plugin corressponding to the URL " + url);

        if (downloadPluginInfoList == null) {
            LOGGER.debug("PluginFactory wasn't initialized.");
            initPlugins();
        }
        for (IDownloadPluginInfo downloadPluginInfo : downloadPluginInfoList) {
            LOGGER.debug("Plugin name : " + downloadPluginInfo.getName());
            String[] matchingPatterns = downloadPluginInfo.getMatchingPatterns();
            
            for (String matchingPattern : matchingPatterns) {
                LOGGER.debug("Matching pattern : " + matchingPattern);
                Pattern p = Pattern.compile(matchingPattern);
                Matcher m = p.matcher(url);
                if (m.matches()) {
                    LOGGER.debug("Matched plugin: " + downloadPluginInfo.getName());
                    return mapOfPluginNamesToPlugins.get(downloadPluginInfo.getName());
                }
            }
        }

        LOGGER.debug(String.format("No available plugin for url %s", url));
        return null;
    }

    /**
     * Proceed plugin cleaning when proceeding shutdown of the server (TODO
     * implement shutdown detection)
     *
     * @throws DMPluginException
     */
    public void cleanPlugins() throws DMPluginException {
        LOGGER.debug("Clean plugins");
        for (IDownloadPlugin downloadPlugin : mapOfPluginNamesToPlugins.values()) {
            downloadPlugin.terminate();
        }
    }

    public PluginConfiguration[] getDownloadPluginInfo(String protocol, String[] servers) {
        LOGGER.debug("Get download plugin configurations( protocol = " + protocol + ", servers = " + StringUtils.join(servers, ","));
        if (StringUtils.isNotEmpty(protocol)) {
            for (IDownloadPluginInfo downloadPluginInfo : downloadPluginInfoList) {
                if (downloadPluginInfo.getProtocol().toString().equalsIgnoreCase(protocol)) {
                    return downloadPluginInfo.getConfigurations(servers);
                }
            }
        } else {
            List<PluginConfiguration> pluginConfigList = null;
            for (IDownloadPluginInfo downloadPluginInfo : downloadPluginInfoList) {
                PluginConfiguration[] pluginConfigs = downloadPluginInfo.getConfigurations(servers);
                if (pluginConfigs != null) {
                    if (pluginConfigList == null) {
                        pluginConfigList = new ArrayList<PluginConfiguration>();
                    }
                    pluginConfigList.addAll(Arrays.asList(pluginConfigs));
                }

            }
            if (pluginConfigList != null) {
                return pluginConfigList.toArray(new PluginConfiguration[pluginConfigList.size()]);
            }
        }
        return null;

    }

    /**
     * Return the list of plugin classes
     *
     * @return list of plugings classes
     */
    private static List<Class<? extends IDownloadPlugin>> getPluginsClasses() {
        LOGGER.debug("Get plugin classes.");
        // Build list of parsers.
        final List<Class<? extends IDownloadPlugin>> pluginsList = new ArrayList<Class<? extends IDownloadPlugin>>();
        // USGS plugin should be added before HTTP (else HTTP plugin matches URLs before USGS)
        pluginsList.add(USGSDownloadPlugin.class);
        pluginsList.add(HTTPDownloadPlugin.class);
        pluginsList.add(FTPDownloadPlugin.class);
        
        return pluginsList;
    }

    /**
     * Return the instance of a given (plugin) class
     *
     * @param pluginClass the plugin class
     * @return the plugin instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private IDownloadPlugin getPluginInstance(Class<? extends IDownloadPlugin> pluginClass) throws InstantiationException, IllegalAccessException {
        return pluginClass.newInstance();
    }

}
