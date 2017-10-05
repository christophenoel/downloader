package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.plugin.IProductDownloadListener;
import java.io.File;
import java.net.URI;

public class DownloadProcessInfo {

    private URI productURI;
    private File downloadDir;
    private FTPDownloadAccount account;
    private IProductDownloadListener downloadListener;
    private String proxyLocation;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    public DownloadProcessInfo() {
    }

    public DownloadProcessInfo(FTPDownloadAccount newAccount, URI productURI, File downloadDir,
            IProductDownloadListener downloadListener, String proxyLocation, int proxyPort,
            String proxyUser, String proxyPassword) {
        this.account = newAccount;
        this.productURI = productURI;
        this.downloadDir = downloadDir;
        this.downloadListener = downloadListener;
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

    public IProductDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(IProductDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
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

    public FTPDownloadAccount getAccount() {
        return account;
    }

    public void setAccount(FTPDownloadAccount account) {
        this.account = account;
    }

    public String debug() {
        return "DownloadProcessInfo[productURI = " + productURI + ", downloadDir = "
                + downloadDir.getAbsolutePath() + ", account = " + account.debug()
                + ", proxyLocation = " + proxyLocation + ", proxyPort = " + proxyPort
                + ", proxyUser = " + proxyUser + ", proxyPassword = " + proxyPassword + "]";
    }
}
