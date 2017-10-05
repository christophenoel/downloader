package be.spacebel.ese.downloadmanager.plugin.http;


import esa.mep.downloader.plugin.EDownloadStatus;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;


/**
 *
 * @author ane
 */
public class HTTPDownloadInfo {

    private final URI uri;
    private final HttpClientContext httpContext; // maybe not used
    private File file;
    private EDownloadStatus status;
    private long contentLength;


    public HTTPDownloadInfo(EDownloadStatus status, File file, URI uri, HttpClientContext httpContext) {
        this.status = status;
        this.file = file;
        this.contentLength = -1;
        this.uri = uri;
        this.httpContext = httpContext;
    }


    public URI getUri() {
        return uri;
    }


    public HttpClientContext getHttpContext() {
        return httpContext;
    }


    public synchronized final File getFile() {
        return file;
    }


    public synchronized void setFile(File file) {
        this.file = file;
    }


    public EDownloadStatus getStatus() {
        return status;
    }


    public void setStatus(EDownloadStatus status) {
        this.status = status;
    }


    public long getContentLength() {
        return contentLength;
    }


    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }


    @Override
    public String toString() {
        String uriWithoutUserInfo;
        try {
            uriWithoutUserInfo = URIUtils.rewriteURI(uri).toString();
        } catch (URISyntaxException ex) {
            uriWithoutUserInfo = uri.getHost();
        }
        return "HTTPDownloadInfo{" + "uri=" + uriWithoutUserInfo + ", file=" + file + ", status=" + status + ", contentLength=" + contentLength + '}';
    }

}
