/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.service;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloader.DownloadStatus;
import _int.esa.proba_v_mep.schemas.downloader.ExpandURLRequest;
import _int.esa.proba_v_mep.schemas.downloader.ExpandURLResponse;
import _int.esa.proba_v_mep.schemas.downloader.GetConfigurationRequest;
import _int.esa.proba_v_mep.schemas.downloader.GetConfigurationResponse;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import _int.esa.proba_v_mep.schemas.downloader.ServerConfigurationType;
import _int.esa.proba_v_mep.schemas.downloader.ServersConfigurationType;
import esa.mep.downloader.logic.DownloaderException;
import esa.mep.downloader.logic.DownloaderLogic;
import esa.mep.downloader.plugin.PluginConfiguration;
import java.util.Arrays;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
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

    @Context
    private UriInfo context;
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
        return of.createDownloadResponse().withIdentifier(downloader.download(request));
    }

    @DELETE
    @Path("/download/{identifier}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DownloadStatus deleteDownload(@PathParam("identifier") String identifier) throws DownloaderException {
        return downloader.cancel(identifier);
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

    @POST
    @Path("/configuration")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GetConfigurationResponse postGetConfiguration(GetConfigurationRequest request) throws DownloaderException {
        String protocol = null;
        String[] servers = null;
        if (request != null && request.getServers() != null) {
            protocol = request.getServers().getProtocol();
            if (request.getServers().getNames() != null) {
                servers = request.getServers().getNames().toArray(new String[request.getServers().getNames().size()]);
            }
        }
        GetConfigurationResponse response = of.createGetConfigurationResponse();

        PluginConfiguration[] pluginConfigs = downloader.getConfiguration(protocol, servers);
        if (pluginConfigs != null) {
            ServersConfigurationType serversType = of.createServersConfigurationType();
            for (PluginConfiguration pConfig : pluginConfigs) {
                ServerConfigurationType serverType = of.createServerConfigurationType();
                serverType.setName(pConfig.getName());
                serverType.setProtocol(pConfig.getProtocol().toString());
                serverType.setUser(pConfig.getUser());
                serverType.setMaxConnections(pConfig.getMaxConnections());
                serversType.getServers().add(serverType);
            }
            response.setServers(serversType);
        }
        return response;
    }
}
