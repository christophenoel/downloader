/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.soap;

import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloadmanager.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloadmanager.ObjectFactory;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author cnl
 */


    @WebService(targetNamespace = "http://esa.int/ese/services/downloader", name = "DownloaderWebServiceInterface")
    @XmlSeeAlso({ObjectFactory.class})
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public interface DownloaderWebServicePortType {

        @WebMethod(action = "urn:#download")
        @WebResult(name = "DownloadResponse", targetNamespace = "http://proba-v-mep.esa.int/schemas/downloadmanager", partName = "parameter")
        public DownloadResponse download(
                @WebParam(partName = "request", name = "DownloadRequest", targetNamespace = "http://proba-v-mep.esa.int/schemas/downloadmanager") DownloadRequest request
        );

}
