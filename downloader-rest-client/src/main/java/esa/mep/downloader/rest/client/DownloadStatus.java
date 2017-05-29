/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.rest.client;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mng
 */
public class DownloadStatus implements Serializable {

    private String identifier;
    private String name;
    private List<ProductStatus> productStatus;

    public DownloadStatus() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductStatus> getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(List<ProductStatus> productStatus) {
        this.productStatus = productStatus;
    }

}
