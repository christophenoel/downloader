/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.rest.client;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloader.ExpandURLRequest;
import _int.esa.proba_v_mep.schemas.downloader.ExpandURLResponse;
import _int.esa.proba_v_mep.schemas.downloader.GetConfigurationRequest;
import _int.esa.proba_v_mep.schemas.downloader.GetConfigurationResponse;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import _int.esa.proba_v_mep.schemas.downloader.ProductStatusType;
import _int.esa.proba_v_mep.schemas.downloader.ProductType;
import _int.esa.proba_v_mep.schemas.downloader.ServerConfigurationType;
import _int.esa.proba_v_mep.schemas.downloader.ServerSelectionType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cnl
 */
public class DownloaderRestClientAPI {
    
    private final DownloaderRestClient client;
    private final ObjectFactory objectFactory = new ObjectFactory();
    
    public DownloaderRestClientAPI(String url) {
        client = new DownloaderRestClient(url);
    }
    
    public String download(String name, List<ProductUrl> productUrls) {
        DownloadRequest request = objectFactory.createDownloadRequest();
        request.setName(name);
        for (ProductUrl productUrl : productUrls) {
            ProductType productType = objectFactory.createProductType();
            productType.setURL(productUrl.getUrl());
            productType.setDownloadDirectory(productUrl.getDownloadDirectory());
            productType.setSize(productUrl.getSize());
            if (productUrl.getHashType() != null && isNotEmpty(productUrl.getHashValue())) {
                ProductType.Verification verification = objectFactory.createProductTypeVerification();
                ProductType.Verification.Hash hashValue = objectFactory.createProductTypeVerificationHash();
                hashValue.setType(productUrl.getHashType().toString());
                verification.getHashes().add(hashValue);
            }
            request.getProducts().add(productType);
        }
        
        return client.postDownload_XML(request, DownloadResponse.class).getIdentifier();
    }
    
    public DownloadStatus cancel(String identifier) {
        return toDownloadStatus(client.deleteDownload_XML(_int.esa.proba_v_mep.schemas.downloader.DownloadStatus.class, identifier));
    }
    
    public DownloadStatus getStatus(String identifier) {
        return toDownloadStatus(client.getStatus_XML(_int.esa.proba_v_mep.schemas.downloader.DownloadStatus.class, identifier));
    }
    
    public List<String> expandUrl(String url) {
        ExpandURLRequest request = objectFactory.createExpandURLRequest();
        request.setUrl(url);
        return client.postExpandUrl_XML(request, ExpandURLResponse.class).getUrls();
    }
    
    public List<ServerConfiguration> getConfiguration(ServerConfiguration.Protocol protocol, List<String> names) {
        GetConfigurationRequest request = objectFactory.createGetConfigurationRequest();
        ServerSelectionType serverType = objectFactory.createServerSelectionType();
        if (protocol != null) {
            serverType.setProtocol(protocol.toString());
        }
        serverType.setNames(names);
        request.setServers(serverType);
        GetConfigurationResponse response = client.postExpandUrl_XML(request, GetConfigurationResponse.class);
        if (response != null && response.getServers() != null && response.getServers().getServers() != null) {
            List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>();
            for (ServerConfigurationType serverConfigType : response.getServers().getServers()) {
                servers.add(new ServerConfiguration(serverConfigType.getName(), ServerConfiguration.Protocol.valueOf(serverConfigType.getProtocol()), serverConfigType.getUser(), serverConfigType.getMaxConnections()));
            }
            return servers;
        }
        
        return null;
    }
    
    private DownloadStatus toDownloadStatus(_int.esa.proba_v_mep.schemas.downloader.DownloadStatus downloadStatusResp) {
        DownloadStatus downloadStatus = new DownloadStatus();
        downloadStatus.setIdentifier(downloadStatusResp.getIdentifier());
        downloadStatus.setName(downloadStatusResp.getName());
        if (downloadStatusResp.getProductStatuses() != null) {
            for (ProductStatusType psType : downloadStatusResp.getProductStatuses()) {
                ProductStatus pStatus = new ProductStatus();
                
                pStatus.setProductURL(psType.getProductURL());
                pStatus.setProductName(psType.getProductName());
                
                pStatus.setDownloadDirectory(psType.getDownloadDirectory());
                pStatus.setCompletedDownloadPath(psType.getCompletedDownloadPath());
                pStatus.setTotalFileSize(psType.getTotalFileSize());
                
                if (psType.getProductProgress() != null) {
                    ProductProgress progress = new ProductProgress();
                    
                    progress.setDownloadedSize(psType.getProductProgress().getDownloadedSize());
                    progress.setMessage(psType.getProductProgress().getMessage());
                    progress.setProgressPercentage(psType.getProductProgress().getProgressPercentage());
                    progress.setStatusCode(psType.getProductProgress().getStatusCode());
                    progress.setStatus(ProductProgress.Status.valueOf(psType.getProductProgress().getStatus().enumValue()));
                    
                    pStatus.setProgress(progress);
                }
                
                if (downloadStatus.getProductStatus() == null) {
                    downloadStatus.setProductStatus(new ArrayList<ProductStatus>());
                }
                
                downloadStatus.getProductStatus().add(pStatus);
            }
        }
        return downloadStatus;
    }
    
    private boolean isNotEmpty(String str) {
        if (str != null && !str.isEmpty()) {
            return true;
        }
        return false;
    }
}
