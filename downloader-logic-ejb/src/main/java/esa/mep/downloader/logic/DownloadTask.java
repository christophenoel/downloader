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
package esa.mep.downloader.logic;

import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadStatus;
import _int.esa.proba_v_mep.schemas.downloadmanager.ObjectFactory;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProductStatusType;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProductType;
import _int.esa.proba_v_mep.schemas.downloadmanager.ProgressType;
import esa.mep.downloader.products.ProductDownload;
import esa.mep.downloader.util.DownloaderBindings;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Lock;
import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;

/**
 * Represent a Download Task related to a DownloadRequest. It manage locking
 * around the status document which is modified by threads in concurrency
 *
 * @author cnl
 */
public class DownloadTask {

    private DownloadRequest request;
    private String identifier;
    private DownloadStatus status;
    private Map<String, ProductDownload> productDownloads;

    public Map<String, ProductDownload> getProductDownloads() {
        return productDownloads;
    }

    public DownloadTask(DownloadRequest request, String identifier) {
        this.request = request;
        this.identifier = identifier;
        this.status = DownloaderBindings.createDownloadStatus(request, identifier);
    }

    /**
     * Create an initial status document @TODO
     *
     * @return
     */
    @Lock(WRITE)
    private DownloadStatus newDownloadStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the request
     */
    public DownloadRequest getRequest() {
        return request;
    }

    /**
     * @param request the request to set
     */
    public void setRequest(DownloadRequest request) {
        this.request = request;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Create all the Product Download instances and put them in a map
     */
    /**
     * @return the status
     */
    @Lock(READ)
    public DownloadStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    @Lock(WRITE)
    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    @Lock(READ)
    /**
     * Given a task id and product url return the status document
     *
     * @param taskId
     * @param url
     * @return
     */
    public ProductStatusType getProductStatus(String url) {
        DownloadStatus statusDoc = this.getStatus();
        for (ProductStatusType prodStatus : statusDoc.getProductStatuses()) {
            if (prodStatus.getProductURL().equalsIgnoreCase(url)) {
                return prodStatus;
            }
        }
        return null;
    }

    @Lock(WRITE)
    public void updateDownloadProgress(ProductDownload product, ProgressType progressType) {
        ProductStatusType productStatus = this.getProductStatus(product.getProduct().getURL());
        productStatus.setProductProgress(progressType);
    }

     @Lock(WRITE)
    public void updateDownloadProgressStatus(ProductDownload product, EDownloadStatus progressStatus) {
        ProductStatusType productStatus = this.getProductStatus(product.getProduct().getURL());
        productStatus.getProductProgress().setStatus(DownloaderBindings.getStatus(progressStatus));
    }

    @Lock(WRITE)
    public void updateDownloadDetails(ProductDownload product, String productName, Long overallSize) {
        ProductStatusType productStatus = this.getProductStatus(product.getProduct().getURL());
        productStatus.setTotalFileSize(overallSize);
        productStatus.setProductName(productName);
    }

    public DownloadStatus cancel() {
        for (ProductDownload download : this.getProductDownloads().values()) {
            try {
                EDownloadStatus productStatus = download.cancel();
                this.updateDownloadProgressStatus(download, productStatus);
            } catch (DMPluginException ex) {
                // @TODO cancel exception occured
                Logger.getLogger(DownloadTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.getStatus();
    }
@Lock(WRITE)
    public void updateCompletedDownloadPath(ProductDownload product,File downloadedFile) {
         ProductStatusType productStatus = this.getProductStatus(product.getProduct().getURL());
         productStatus.setCompletedDownloadPath(downloadedFile.getAbsolutePath());
    }

}
