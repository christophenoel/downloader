package be.spacebel.ese.downloadmanager.plugin.http;


import java.util.Objects;


public class HTTPHostInfo {

    private String server;
    private String username;
    private String password;
    private int maxConnections;
    private String protocol;
    private String port;


    public HTTPHostInfo() {
    }


    public String getServer() {
        return server;
    }


    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }


    public int getMaxConnections() {
        return maxConnections;
    }


    public String getProtocol() {
        return protocol;
    }


    public String getPort() {
        return port;
    }


    public void setServer(String server) {
        this.server = server;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }


    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public void setPort(String port) {
        this.port = port;
    }


    @Override
    public String toString() {
        return "HTTPHostInfo{" + "server=" + server + ", username=" + "*********" + ", password=" + "*********" + ", maxConnections=" + maxConnections + ", protocol=" + protocol + ", port=" + port + '}';
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.server);
        hash = 53 * hash + Objects.hashCode(this.username);
        hash = 53 * hash + Objects.hashCode(this.password);
        hash = 53 * hash + this.maxConnections;
        hash = 53 * hash + Objects.hashCode(this.protocol);
        hash = 53 * hash + Objects.hashCode(this.port);
        return hash;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HTTPHostInfo other = (HTTPHostInfo) obj;
        if (this.maxConnections != other.maxConnections) {
            return false;
        }
        if (!Objects.equals(this.server, other.server)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.protocol, other.protocol)) {
            return false;
        }
        if (!Objects.equals(this.port, other.port)) {
            return false;
        }
        return true;
    }

}
