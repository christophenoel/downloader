/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.service;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloader.DownloadStatus;
import _int.esa.proba_v_mep.schemas.downloader.GetContentRequest;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import _int.esa.proba_v_mep.schemas.downloader.ProductType;
import esa.mep.downloader.logic.DownloaderException;
import esa.mep.downloader.logic.DownloaderLogic;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
        LOGGER.info("Get Status " + identifier);
        return downloader.getStatus(identifier);
    }

    @POST
    @Path("/download")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DownloadResponse postDownload(DownloadRequest request) throws DownloaderException {
        LOGGER.debug("postDownload" + request.toString());
        if (request == null) {
            throw new DownloaderException("Download request should not be empty.");
        }

        if (request.getName() == null) {
            throw new DownloaderException("Name of download request should be provided.");
        }

        if (request.getProducts() == null || request.getProducts().size() < 1) {
            throw new DownloaderException("There is no product to download.");
        }

        for (ProductType pType : request.getProducts()) {
            if (pType.getURL() == null || pType.getURL().trim().isEmpty()) {
                throw new DownloaderException("URL of product to download request should be provided.");
            }

            if (pType.getDownloadDirectory() == null || pType.getDownloadDirectory().trim().isEmpty()) {
                throw new DownloaderException("Download directory should be provided.");
            }
        }

        return of.createDownloadResponse().withIdentifier(downloader.download(request));
    }

    @DELETE
    @Path("/download/{identifier}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DownloadStatus deleteDownload(@PathParam("identifier") String identifier) throws DownloaderException {
        LOGGER.info("Delete Download " + identifier);
        return downloader.cancel(identifier);
    }

    @POST
    @Path("/content")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getContent(GetContentRequest request) throws DownloaderException {
        LOGGER.info("Get content of file " + request.getFilePath() + " with hash value " + request.getHashValue());
        return getTextFileContent(request.getFilePath(), request.getHashValue());
    }

    public String getTextFileContent(String filePath, String hashValue) {
        LOGGER.debug("Get contents of file " + filePath + " with hash value " + hashValue);

        String content = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean ok = true;
                if (StringUtils.isNotEmpty(hashValue)) {
                    String fileHash = getDigest(file);
                    LOGGER.debug("MD5 hex of downloaded file: " + fileHash);
                    if (fileHash != null && fileHash.equals(hashValue)) {
                        LOGGER.debug("Check hash is OK.");
                    } else {
                        ok = false;
                        content = "FILE_NOT_FOUND";
                    }
                }

                if (ok) {
                    //System.out.println("MD5 Hex: " + DigestUtils.md5Hex(new FileInputStream(file)));
                    if (file.isFile()) {
                        if (filePath.endsWith(".txt") || filePath.endsWith(".xml")) {
                            content = FileUtils.readFileToString(file);
                        } else {
                            content = "" + FileUtils.sizeOf(file);
                        }
                    } else {
                        if (file.isDirectory()) {
                            content = "" + FileUtils.sizeOfDirectory(file);
                        } else {
                            content = "NOR_FILE_NOR_DIRECTORY";
                        }
                    }
                }

            } else {
                content = "FILE_NOT_FOUND";
            }
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
        }
        LOGGER.debug("content = " + content);
        return content;
    }

    private String getDigest(File file) throws IOException {
        String md5;
        try (FileInputStream fis = new FileInputStream(file)) {
            md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        }
        return md5;
    }

}
