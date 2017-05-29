/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.rest.client;

import java.io.Serializable;

/**
 *
 * @author mng
 */
public class ProductUrl implements Serializable {

    public enum HashType {

        MD2, MD5, SHA1, SHA256, SHA384, SHA512, NOT_SUPPORTED;

        @Override
        public String toString() {
            switch (this) {
                case MD2:
                    return "MD2";
                case MD5:
                    return "MD5";
                case SHA1:
                    return "SHA-1";
                case SHA256:
                    return "SHA-256";
                case SHA384:
                    return "SHA-384";
                case SHA512:
                    return "SHA-512";
                default:
                    return null;
            }
        }

        public static HashType toEnum(String value) {
            if (value == null || value.isEmpty()) {
                return NOT_SUPPORTED;
            }

            if ("MD5".equalsIgnoreCase(value)) {
                return MD5;
            }
            if ("SHA1".equalsIgnoreCase(value) || "SHA-1".equalsIgnoreCase(value)) {
                return SHA1;
            }
            if ("SHA256".equalsIgnoreCase(value) || "SHA-256".equalsIgnoreCase(value)) {
                return SHA256;
            }
            if ("SHA384".equalsIgnoreCase(value) || "SHA-384".equalsIgnoreCase(value)) {
                return SHA384;
            }
            if ("SHA512".equalsIgnoreCase(value) || "SHA-512".equalsIgnoreCase(value)) {
                return SHA512;
            }

            return NOT_SUPPORTED;
        }

    }

    public ProductUrl() {
    }

    public ProductUrl(String url, String downloadDirectory) {
        this.url = url;
        this.downloadDirectory = downloadDirectory;
    }

    public ProductUrl(String url, String downloadDirectory, long size, HashType hashType, String hashValue) {
        this.url = url;
        this.downloadDirectory = downloadDirectory;
        this.size = size;
        this.hashType = hashType;
        this.hashValue = hashValue;
    }

    private String url;
    private String downloadDirectory;
    private long size;
    private HashType hashType;
    private String hashValue;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public HashType getHashType() {
        return hashType;
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

}
