package be.spacebel.ese.downloadmanager.plugin.http;


import java.io.File;
import java.net.URI;
import java.util.Objects;
import org.apache.http.client.methods.HttpUriRequest;


/**
 *
 * @author ane
 */
public class DownloadProcessInfo {

    private URI productURI;
    private File downloadDir;
    private String user;
    private String password;
    private String proxyLocation;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;
    private HttpUriRequest httpUriRequest=null;

  

    public DownloadProcessInfo(URI productURI, File downloadDir, String user,
            String password, String proxyLocation, int proxyPort, String proxyUser, String proxyPassword) {
        this.productURI = productURI;
        this.downloadDir = downloadDir;
        this.user = user;
        this.password = password;
        this.proxyLocation = proxyLocation;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
    }


    public URI getProductURI() {
        return productURI;
    }


    public void setProductURI(URI productURI) {
        this.productURI = productURI;
    }


    public File getDownloadDir() {
        return downloadDir;
    }


    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }


    public String getUser() {
        return user;
    }


    public void setUser(String user) {
        this.user = user;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getProxyLocation() {
        return proxyLocation;
    }


    public void setProxyLocation(String proxyLocation) {
        this.proxyLocation = proxyLocation;
    }


    public int getProxyPort() {
        return proxyPort;
    }


    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }


    public String getProxyUser() {
        return proxyUser;
    }


    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }


    public String getProxyPassword() {
        return proxyPassword;
    }


    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.productURI);
        hash = 67 * hash + Objects.hashCode(this.downloadDir);
        hash = 67 * hash + Objects.hashCode(this.user);
        hash = 67 * hash + Objects.hashCode(this.password);
        hash = 67 * hash + Objects.hashCode(this.proxyLocation);
        hash = 67 * hash + this.proxyPort;
        hash = 67 * hash + Objects.hashCode(this.proxyUser);
        hash = 67 * hash + Objects.hashCode(this.proxyPassword);
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
        final DownloadProcessInfo other = (DownloadProcessInfo) obj;
        if (this.proxyPort != other.proxyPort) {
            return false;
        }
        if (!Objects.equals(this.user, other.user)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.proxyLocation, other.proxyLocation)) {
            return false;
        }
        if (!Objects.equals(this.proxyUser, other.proxyUser)) {
            return false;
        }
        if (!Objects.equals(this.proxyPassword, other.proxyPassword)) {
            return false;
        }
        if (!Objects.equals(this.productURI, other.productURI)) {
            return false;
        }
        if (!Objects.equals(this.downloadDir, other.downloadDir)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "DownloadProcessInfo{" + "productURI=" + productURI + ", downloadDir=" + downloadDir + ", user="
                + user + ", password=" + password + ", proxyLocation=" + proxyLocation + ", proxyPort=" + proxyPort
                + ", proxyUser=" + proxyUser + ", proxyPassword=" + proxyPassword + '}';
    }

    
  public HttpUriRequest getHttpUriRequest() {
        return httpUriRequest;
    }

    public void setHttpUriRequest(HttpUriRequest httpUriRequest) {
        this.httpUriRequest = httpUriRequest;
    }
    

}
