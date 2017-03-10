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

import esa.mep.downloader.plugin.PluginFactory;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProductType;
import esa.mep.downloader.config.DownloaderConfig;
import esa.mep.downloader.logic.DownloadTask;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * ProductDownloadManager handles individual product downloads.
 *
 * @author cnl
 */
@Singleton
public class ProductDownloadManager {

    private PluginFactory pluginFactory = new PluginFactory();
  
    @EJB
    private DownloaderConfig config;
    
    public IDownloadPlugin getPlugin(ProductType product) {
        IDownloadPlugin downloadPlugin = pluginFactory.getPlugin(product.getURL());
        return downloadPlugin;
        /**
         * ProductDownloadListener productDownloadListener = new
         * ProductDownloadListener(product.getUuid());
         * productDownloadListener.registerObserver(this);
         *
         */
    }

    public void downloadProducts(DownloadTask task) throws URISyntaxException, DMPluginException {

        Map<String, ProductDownload> productDownloads = new HashMap<String, ProductDownload>();
        for (ProductType downloadEntry : task.getRequest().getProducts()) {
            String url = downloadEntry.getURL();
            IDownloadPlugin plugin = getPlugin(downloadEntry);
            ProductDownload download = new ProductDownload(downloadEntry, plugin, task);
            ProductDownloadListener productListener = new ProductDownloadListener(task, download);
            productDownloads.put(url, download);
            download.start(productListener, config.getDownloaderRootDirectory());
        }
    }

}
