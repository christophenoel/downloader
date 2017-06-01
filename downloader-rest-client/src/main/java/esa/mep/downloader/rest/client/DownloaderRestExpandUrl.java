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
 * Jersey REST client generated for REST resource:ExpandUrl [urllist]<br>
 * USAGE:
 * <pre>
 *        DownloaderRestExpandUrl client = new DownloaderRestExpandUrl();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author mng
 */
public class DownloaderRestExpandUrl {

    private WebTarget webTarget;
    private Client client;
    private static final String BASE_URI = "http://localhost:8080/downloader/webapi";

    public DownloaderRestExpandUrl() {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(BASE_URI).path("urllist");
    }

    public DownloaderRestExpandUrl(String url) {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(url).path("urllist");
    }

    public <T> T postExpandUrl_XML(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("expandurl").request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML), responseType);
    }

    public <T> T postExpandUrl_JSON(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.path("expandurl").request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), responseType);
    }

    public void close() {
        client.close();
    }

}
