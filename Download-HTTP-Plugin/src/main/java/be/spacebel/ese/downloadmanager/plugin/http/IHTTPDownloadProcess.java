/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.ese.downloadmanager.plugin.http;

import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.IDownloadProcess;

/**
 *
 * @author cnl
 */
public interface IHTTPDownloadProcess extends IDownloadProcess{

    public void progress(String message, DMPluginException ex);
    
}
