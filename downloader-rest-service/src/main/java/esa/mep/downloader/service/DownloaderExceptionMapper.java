/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.service;

import esa.mep.downloader.logic.DownloaderException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author mng
 */
@Provider
public class DownloaderExceptionMapper implements ExceptionMapper<DownloaderException> {

    /*
    @Context
    private javax.inject.Provider<Request> request;
    */

    @Override
    public Response toResponse(DownloaderException exception) {
        _int.esa.proba_v_mep.schemas.downloader.DownloaderException ex = new _int.esa.proba_v_mep.schemas.downloader.DownloaderException();
        
        ex.setCode(exception.getCode());
        ex.setMessage(exception.getMessage());
        ex.setDeveloperMessage(exception.getDeveloperMessage());
        ex.setStatus(exception.getCode());
        System.out.println("Handle exception");
        return Response.status(exception.getCode()).entity(ex).type(MediaType.APPLICATION_XML).build();
        
        /*
        ResponseBuilder rb = Response.status(exception.getCode()).entity(ex);
        // Entity
        final List<Variant> variants = Variant.mediaTypes(
                MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_XML_TYPE
        ).build();

        final Variant variant = request.get().selectVariant(variants);
        if (variant != null) {
            //System.out.println("Variant is not null " + variant.getMediaType());
            //rb = rb.type(variant.getMediaType());
        } else {
            //System.out.println("Variant is null ");
            //rb = rb.type(MediaType.APPLICATION_XML);
        }

        rb = rb.type(MediaType.APPLICATION_XML);

        return rb.build();
                */
    }

}
