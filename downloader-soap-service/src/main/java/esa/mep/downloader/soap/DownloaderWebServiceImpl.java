/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.soap;

import _int.esa.proba_v_mep.schemas.downloader.DownloadRequest;
import _int.esa.proba_v_mep.schemas.downloader.DownloadResponse;
import _int.esa.proba_v_mep.schemas.downloader.ObjectFactory;
import esa.mep.downloader.logic.DownloaderLogic;
import javax.ejb.EJB;
import javax.jws.WebService;


@WebService
public class DownloaderWebServiceImpl implements DownloaderWebServicePortType {

@EJB
    private DownloaderLogic downloader;
    private final static ObjectFactory of=new ObjectFactory();
    public DownloadResponse download( DownloadRequest request) {
        return of.createDownloadResponse().withIdentifier(downloader.download(request));
    }
}
