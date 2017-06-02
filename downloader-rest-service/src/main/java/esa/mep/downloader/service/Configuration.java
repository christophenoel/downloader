/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.service;

import _int.esa.proba_v_mep.schemas.downloader.GetConfigurationRequest;
import _int.esa.proba_v_mep.schemas.downloader.GetConfigurationResponse;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import _int.esa.proba_v_mep.schemas.downloader.ServerConfigurationType;
import _int.esa.proba_v_mep.schemas.downloader.ServersConfigurationType;
import esa.mep.downloader.logic.DownloaderException;
import esa.mep.downloader.logic.DownloaderLogic;
import esa.mep.downloader.plugin.PluginConfiguration;
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
 * @author mng
 */
@Path("configuration")
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    @EJB
    private DownloaderLogic downloader;
    
    private final static ObjectFactory of = new ObjectFactory();

    /**
     * Creates a new instance of Download
     */
    public Configuration() {
        LOGGER.debug("Initialize Configuration");
    }

    @POST
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
