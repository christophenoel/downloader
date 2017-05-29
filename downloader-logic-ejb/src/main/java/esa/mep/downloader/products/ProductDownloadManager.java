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
package esa.mep.downloader.products;

import _int.esa.proba_v_mep.schemas.downloader.ProductType;
import esa.mep.downloader.config.DownloaderConfig;
import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.logic.DownloadTask;
import esa.mep.downloader.plugin.IDownloadPlugin;
import esa.mep.downloader.plugin.PluginConfiguration;
import esa.mep.downloader.plugin.PluginFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * ProductDownloadManager handles individual product downloads.
 *
 * @author cnl
 */
@Singleton
public class ProductDownloadManager {

    @EJB
    private PluginFactory pluginFactory;

    @EJB
    private DownloaderConfig config;

    public IDownloadPlugin getPlugin(ProductType product) {
        return pluginFactory.getPlugin(product.getURL());
    }

    public String[] expandUrl(URI uri) throws DMPluginException {
        IDownloadPlugin plugin = pluginFactory.getPlugin(uri.toString());
        if (plugin != null) {
            return plugin.expandUrl(uri);
        } else {
            throw new DMPluginException(String.format("No available plugin for url %s", uri.toString()));
        }
    }

    public PluginConfiguration[] getPluginConfigs(String protocol, String[] servers) {
        return pluginFactory.getDownloadPluginInfo(protocol, servers);
    }

    public void downloadProducts(DownloadTask task) throws URISyntaxException, DMPluginException {

        Map<String, ProductDownload> productDownloads = new HashMap<String, ProductDownload>();
        for (ProductType downloadEntry : task.getRequest().getProducts()) {
            String url = downloadEntry.getURL();
            IDownloadPlugin plugin = getPlugin(downloadEntry);
            if (plugin != null) {
                ProductDownload download = new ProductDownload(downloadEntry, plugin, task);
                ProductDownloadListener productListener = new ProductDownloadListener(task, download);
                productDownloads.put(url, download);
                download.start(productListener, config.getBaseDownloadDirectory());
            } else {
                throw new DMPluginException(String.format("No available plugin for url %s", url));
            }
        }
    }

}
