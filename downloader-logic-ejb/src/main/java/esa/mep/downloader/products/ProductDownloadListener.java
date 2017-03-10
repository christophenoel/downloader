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

import _int.esa.proba_v_mep.schemas.downloadmanager.ProgressType;
import esa.mep.downloader.logic.DownloadManager;
import esa.mep.downloader.logic.DownloadTask;
import esa.mep.downloader.util.DownloaderBindings;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IProductDownloadListener;
import java.io.File;
import javax.ejb.EJB;

/**
 *
 * @author cnl
 */
public class ProductDownloadListener implements IProductDownloadListener {

    private final DownloadTask downloadTask;
    private final ProductDownload product;

    @EJB
    private DownloadManager downloadManager;
    
    public ProductDownloadListener(DownloadTask downloadTask, ProductDownload product) {
        this.downloadTask = downloadTask;
        this.product=product;
    }
    @Override
    public void productDetails(String productName, Integer numberOfFiles, Long overallSize) {
        this.downloadTask.updateDownloadDetails(this.product,productName,overallSize);
    }

    @Override
    public void progress(Integer progressPercentage, Long downloadedSize, EDownloadStatus status, String message) {
        ProgressType progressType = DownloaderBindings.getProgressType( progressPercentage,  downloadedSize,  status,  message);
        this.downloadTask.updateDownloadProgress(this.product,progressType);
        // in case completed
        if(status.equals(EDownloadStatus.COMPLETED)) {
            File downloadedFile = this.getProduct().getProcess().getDownloadedFiles()[0];
            this.downloadTask.updateCompletedDownloadPath(this.product,downloadedFile);
        }
        
    }

    @Override
    public void progress(Integer progressPercentage, Long downloadedSize, EDownloadStatus status, String message, DMPluginException exception) {
         ProgressType progressType = DownloaderBindings.getProgressType( progressPercentage,  downloadedSize,  status,  message);
        this.downloadTask.updateDownloadProgress(this.product,progressType);
        
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public ProductDownload getProduct() {
        return product;
    }
    
}
