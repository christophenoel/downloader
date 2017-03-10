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

import _int.esa.proba_v_mep.schemas.downloadmanager.ObjectFactory;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProductStatusType;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProductType;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProgressType;
import _int.esa.proba_v_mep.schemas.downloadmanager.StatusType;
import esa.mep.downloader.logic.DownloadTask;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadPlugin;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;
import javax.ws.rs.core.Response;

/**
 *
 * @author cnl
 */
public class ProductDownload {

    private String id;
    private ProductType product;
    private IDownloadPlugin plugin;
    private IDownloadProcess process;

   
    
    ProductDownload(ProductType downloadEntry, IDownloadPlugin plugin, DownloadTask task) {
        id = UUID.randomUUID().toString();
        this.product = downloadEntry;
        this.plugin = plugin;

    }

    public ProductType getProduct() {
        return product;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public void start() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @param product the product to set
     */
    public void setProduct(ProductType product) {
        this.product = product;
    }

    /**
     * @return the plugin
     */
    public IDownloadPlugin getPlugin() {
        return plugin;
    }

    /**
     * @param plugin the plugin to set
     */
    public void setPlugin(IDownloadPlugin plugin) {
        this.plugin = plugin;
    }
public IDownloadProcess getProcess() {
        return process;
    }

    public void setProcess(IDownloadProcess process) {
        this.process = process;
    }

    public IDownloadProcess start(ProductDownloadListener productListener, String downloaderRootDirectory) throws URISyntaxException, DMPluginException {
       // in case of async call @ejb ProductDownloadManager in async mode instead of this
     this.process = plugin.createDownloadProcess(new URI(this.getProduct().getURL()), Paths.get(downloaderRootDirectory,this.getProduct().getDownloadDirectory()).toFile(), null, null, productListener, null, 0, null, null);
     return this.process;
    }

    public EDownloadStatus cancel() throws DMPluginException {
        return this.getProcess().cancelDownload();
    }


}
