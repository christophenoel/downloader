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

import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadPlugin;
import esa.mep.downloader.config.DownloaderConfig;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPluginInfo;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;

/**
 *
 * @author cnl
 */
public class PluginFactory {

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    public static void main(String[] args) {
        Path tmpRootDir = Paths.get(System.getProperty(JAVA_IO_TMPDIR));
        Path pluginCfgRootDir = Paths.get("conf", "plugins");

    }

    @EJB
            private DownloaderConfig config;
    
    /**
     * List of plugins classes: IDownloadPluginInfo, IDownloadPlugin, and a map
     * between names and IDownloadPlugin
     */
    private List<IDownloadPluginInfo> downloadPluginInfos;
    private List<IDownloadPlugin> downloadPlugins;
    private Map<String, IDownloadPlugin> mapOfPluginNamesToPlugins;

    public void initPlugins() throws IllegalAccessException, InstantiationException, DMPluginException {
        Path tmpRootDir = Paths.get(System.getProperty(JAVA_IO_TMPDIR));
        Path pluginCfgRootDir = Paths.get(config.getPluginConfigurationDirectory());
        downloadPluginInfos = new ArrayList<>();
        downloadPlugins = new ArrayList<>();
        mapOfPluginNamesToPlugins = new HashMap<>();
        for (Class<? extends IDownloadPlugin> c : getPluginsClasses()) {
            IDownloadPlugin plugin = getPluginInstance(c);
            IDownloadPluginInfo pluginInfo = plugin.initialize(tmpRootDir.toFile(), pluginCfgRootDir.toFile());
            downloadPlugins.add(plugin);
            downloadPluginInfos.add(pluginInfo);
        }
    }

    /** 
     * Get the relevant plugin for a specific url (check url pattern)
     * @param url the product url
     * @return the relevant plugin
     */
    public IDownloadPlugin getPlugin(String url) {
        for (IDownloadPluginInfo downloadPluginInfo : downloadPluginInfos) {
            String[] matchingPatterns = downloadPluginInfo.getMatchingPatterns();
            for (int i = 0; i < matchingPatterns.length; i++) {
                String matchingPattern = matchingPatterns[i];
                Pattern p = Pattern.compile(matchingPattern);
                Matcher m = p.matcher(url);
                if (m.matches()) {
                    return mapOfPluginNamesToPlugins.get(downloadPluginInfo.getName());
                }
            }
        }
        return null;
    }

    /**
     * Proceed plugin cleaning when proceeding shutdown of the server (TODO implement shutdown detection)
     * @throws DMPluginException 
     */
    
    public void cleanPlugins() throws DMPluginException {
        for (IDownloadPlugin downloadPlugin : mapOfPluginNamesToPlugins.values()) {
            downloadPlugin.terminate();
        }
    }

    /**
     * Return the list of plugin classes
     * @return list of plugings classes
     */
    private static List<Class<? extends IDownloadPlugin>> getPluginsClasses() {
        // Build list of parsers.
        final List<Class<? extends IDownloadPlugin>> pluginsList = new ArrayList<Class<? extends IDownloadPlugin>>();
        pluginsList.add(HTTPDownloadPlugin.class);
        return pluginsList;
    }
/**
 * Return the instance of a given (plugin) class
 * @param pluginClass the plugin class
 * @return the plugin instance
 * @throws InstantiationException
 * @throws IllegalAccessException 
 */
    public IDownloadPlugin getPluginInstance(Class<? extends IDownloadPlugin> pluginClass) throws InstantiationException, IllegalAccessException {
        IDownloadPlugin pluginInstance = pluginClass.newInstance();
        return pluginInstance;
    }
}
