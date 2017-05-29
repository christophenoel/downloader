/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.rest.client;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Jersey REST client generated for REST resource:Download [data]<br>
 * USAGE:
 * <pre>
 *        DownloaderRestClient client = new DownloaderRestClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author cnl
 */
public class DownloaderRestClient {

    private WebTarget webTarget;
    private Client client;
    private static final String BASE_URI = "http://localhost:8080/downloader-service/webapi";

    
      public DownloaderRestClient(String url) {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(url).path("data");
    }

    
    public DownloaderRestClient() {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(BASE_URI).path("data");
    }

    public <T> T getStatus_XML(Class<T> responseType, String identifier) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path(java.text.MessageFormat.format("download/{0}", new Object[]{identifier}));
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T getStatus_JSON(Class<T> responseType, String identifier) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path(java.text.MessageFormat.format("download/{0}", new Object[]{identifier}));
        return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T postDownload_XML(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("download").request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML), responseType);
    }

    public <T> T postDownload_JSON(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("download").request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), responseType);
    }

    public <T> T deleteDownload_XML(Class<T> responseType, String identifier) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("download/{0}", new Object[]{identifier})).request(javax.ws.rs.core.MediaType.APPLICATION_XML).delete(responseType);
    }

    public <T> T deleteDownload_JSON(Class<T> responseType, String identifier) throws ClientErrorException {
        return webTarget.path(java.text.MessageFormat.format("download/{0}", new Object[]{identifier})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).delete(responseType);
    }
    
    public <T> T postExpandUrl_XML(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("expandurl").request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML), responseType);
    }

    public <T> T postExpandUrl_JSON(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("expandurl").request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), responseType);
    }

    public <T> T postGetConfiguration_XML(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("configuration").request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML), responseType);
    }

    public <T> T postGetConfiguration_JSON(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("configuration").request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), responseType);
    }

    public void close() {
        client.close();
    }
    
    
    
}
