/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.rest.client;

import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadStatus;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cnl
 */
public class DownloaderRestClientAPI {

    private final DownloaderRestClient client;

 
    public DownloaderRestClientAPI(String url) {
        client = new DownloaderRestClient(url);
    }

    public DownloadResponse download(DownloadRequest request) {
        return client.postDownload_XML(request, DownloadResponse.class);
    }

    public DownloadStatus cancel(String identifier) {
        return client.deleteDownload_XML(DownloadStatus.class, identifier);
    }

    public DownloadStatus getStatus(String identifier) {
        return client.getStatus_XML(DownloadStatus.class, identifier);
    }

}
