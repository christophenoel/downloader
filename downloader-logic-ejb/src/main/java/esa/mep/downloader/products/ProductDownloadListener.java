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

import _int.esa.proba_v_mep.schemas.downloader.ProductType.Verification.Hash;
import _int.esa.proba_v_mep.schemas.downloader.ProgressType;
import _int.esa.proba_v_mep.schemas.downloader.StatusType;
import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.logic.DownloadManager;
import esa.mep.downloader.logic.DownloadTask;
import esa.mep.downloader.plugin.EDownloadStatus;
import esa.mep.downloader.plugin.IProductDownloadListener;
import esa.mep.downloader.util.DownloaderBindings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.ejb.EJB;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cnl
 */
public class ProductDownloadListener implements IProductDownloadListener {

    private final DownloadTask downloadTask;
    private final ProductDownload product;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ProductDownloadListener.class);

    @EJB
    private DownloadManager downloadManager;

    public ProductDownloadListener(DownloadTask downloadTask, ProductDownload product) {
        this.downloadTask = downloadTask;
        this.product = product;
    }

    @Override
    public void productDetails(String productName, Integer numberOfFiles, Long overallSize) {
        this.downloadTask.updateDownloadDetails(this.product, productName, overallSize);
    }

    @Override
    public void progress(Integer progressPercentage, Long downloadedSize, EDownloadStatus status, String message) {
        ProgressType progressType = DownloaderBindings.getProgressType(progressPercentage, downloadedSize, status, message);
        this.downloadTask.updateDownloadProgress(this.product, progressType);
        // in case completed
        if (status.equals(EDownloadStatus.COMPLETED)) {
            File downloadedFile = this.getProduct().getProcess().getDownloadedFiles()[0];
            this.downloadTask.updateCompletedDownloadPath(this.product, downloadedFile);

            String hashValue = null;

            if (this.product.getProduct().getVerification() != null && this.product.getProduct().getVerification().getHashes() != null) {
                for (Hash hash : this.product.getProduct().getVerification().getHashes()) {
                    if (hash.getType() != null && "MD5".equals(hash.getType())) {
                        hashValue = hash.getValue();
                        if (hashValue != null) {
                            hashValue = hashValue.replaceAll("\\s+", "");
                        }
                    }
                }
            }

            if (hashValue != null && hashValue.trim().length() > 0) {
                LOGGER.debug("Request hash: " + hashValue);
                try {
                    String downloadedFileHash = getDigest(downloadedFile);
                    if (downloadedFile != null && downloadedFileHash.trim().length() > 0) {
                        downloadedFileHash = downloadedFileHash.replaceAll("\\s+", "");
                        LOGGER.debug("Hash of downloaded file: " + downloadedFileHash);
                        if (downloadedFileHash.equals(hashValue)) {
                            LOGGER.debug("The downloaded file is OK");
                        } else {
                            LOGGER.debug("The downloaded file is NOK");
                            progressType.setStatus(StatusType.IN_ERROR);

                            if (this.product.getProduct().getURL().startsWith("http://") || this.product.getProduct().getURL().startsWith("https://")) {
                                progressType.setMessage("HTTP response code 500 : The file isn't downloaded properly. Its hash value " + downloadedFileHash + " is different from hash value of the request " + hashValue);
                                progressType.setStatusCode("500");
                            } else {
                                if (this.product.getProduct().getURL().startsWith("ftp://") || this.product.getProduct().getURL().startsWith("ftps://")) {
                                    progressType.setMessage("FTP response code 400 : The file isn't downloaded properly. Its hash value " + downloadedFileHash + " is different from hash value of the request " + hashValue);
                                    progressType.setStatusCode("400");
                                } else {
                                    progressType.setMessage("The file isn't downloaded properly. Its hash value " + downloadedFileHash + " is different from hash value of the request " + hashValue);
                                }
                            }

                            this.downloadTask.updateCompletedDownloadPath(this.product, downloadedFile);
                        }
                    }

                } catch (IOException e) {

                }
            }

        }

    }

    @Override
    public void progress(Integer progressPercentage, Long downloadedSize, EDownloadStatus status, String message, DMPluginException exception) {
        ProgressType progressType = DownloaderBindings.getProgressType(progressPercentage, downloadedSize, status, message);
        this.downloadTask.updateDownloadProgress(this.product, progressType);

    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public ProductDownload getProduct() {
        return product;
    }

    private String getDigest(File file) throws IOException {
        String md5;
        try (FileInputStream fis = new FileInputStream(file)) {
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        }
        return md5;
    }
}
