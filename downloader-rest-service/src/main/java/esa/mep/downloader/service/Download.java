/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.service;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloader.DownloadStatus;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import esa.mep.downloader.logic.DownloaderException;
import esa.mep.downloader.logic.DownloaderLogic;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author cnl
 */
@Path("data")
public class Download {

    private static final Logger LOGGER = LoggerFactory.getLogger(Download.class);

    @EJB
    private DownloaderLogic downloader;
    
    private final static ObjectFactory of = new ObjectFactory();

    /**
     * Creates a new instance of Download
     */
    public Download() {
        LOGGER.debug("Initialize Download");
    }

    /**
     * Retrieves representation of an instance of
     * esa.mep.downloader.service.Download
     *
     * @param identifier
     * @return an instance of java.lang.String
     */
    @GET
    @Path("/download/{identifier}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DownloadStatus getStatus(@PathParam("identifier") String identifier) throws DownloaderException {
        return downloader.getStatus(identifier);
    }

    @POST
    @Path("/download")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DownloadResponse postDownload(DownloadRequest request) throws DownloaderException {
        LOGGER.debug("postDownload" + request.toString());
        return of.createDownloadResponse().withIdentifier(downloader.download(request));
    }

    @DELETE
    @Path("/download/{identifier}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DownloadStatus deleteDownload(@PathParam("identifier") String identifier) throws DownloaderException {
        return downloader.cancel(identifier);
    }    
}
