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
public class ServerConfiguration implements Serializable {

    public enum Protocol {

        HTTP, FTP;

        @Override
        public String toString() {
            switch (this) {
                case HTTP:
                    return "HTTP";
                case FTP:
                    return "FTP";
                default:
                    return null;
            }
        }

        public static Protocol toEnum(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }

            if ("HTTP".equalsIgnoreCase(value) || "HTTPS".equalsIgnoreCase(value)) {
                return HTTP;
            }

            if ("FTP".equalsIgnoreCase(value) || "FTPS".equalsIgnoreCase(value)) {
                return FTP;
            }
            return null;
        }
    }
    private String name;
    private Protocol protocol;
    private String user;
    private int maxConnections;

    public ServerConfiguration() {
    }

    public ServerConfiguration(String name, Protocol protocol, String user, int maxConnections) {
        this.name = name;
        this.protocol = protocol;
        this.user = user;
        this.maxConnections = maxConnections;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

}
