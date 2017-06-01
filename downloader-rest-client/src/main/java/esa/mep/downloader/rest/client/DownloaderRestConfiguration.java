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
 * Jersey REST client generated for REST resource:Configuration
 * [configuration]<br>
 * USAGE:
 * <pre>
 *        DownloaderRestConfiguration client = new DownloaderRestConfiguration();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author mng
 */
public class DownloaderRestConfiguration {

    private WebTarget webTarget;
    private Client client;
    private static final String BASE_URI = "http://localhost:8080/downloader/webapi";

    public DownloaderRestConfiguration() {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(BASE_URI).path("configuration");
    }

    public DownloaderRestConfiguration(String url) {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(url).path("configuration");
    }

    public <T> T postGetConfiguration_XML(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_XML).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_XML), responseType);
    }

    public <T> T postGetConfiguration_JSON(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), responseType);
    }

    public void close() {
        client.close();
    }

}
