/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.service;

import _int.esa.proba_v_mep.schemas.downloader.ExpandURLRequest;
import _int.esa.proba_v_mep.schemas.downloader.ExpandURLResponse;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import esa.mep.downloader.logic.DownloaderException;
import esa.mep.downloader.logic.DownloaderLogic;
import java.util.Arrays;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author cnl
 */
@Path("urllist")
public class ExpandUrl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpandUrl.class);

    @EJB
    private DownloaderLogic downloader;

    private final static ObjectFactory of = new ObjectFactory();

    /**
     * Creates a new instance of Download
     */
    public ExpandUrl() {
        LOGGER.debug("Initialize ExpandUrl");
    }   

    @POST
    @Path("/expandurl")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ExpandURLResponse postExpandUrl(ExpandURLRequest request) throws DownloaderException {
        ExpandURLResponse response = of.createExpandURLResponse();
        String[] urls = downloader.expandUrl(request.getUrl());
        if (urls != null) {
            response.getUrls().addAll(Arrays.asList(urls));
        }
        return response;
    }   
}
