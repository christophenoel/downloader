package esa.mep.downloader.plugin.usgs;

import be.spacebel.ese.downloadmanager.plugin.http.DownloadProcessInfo;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadConfiguration;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadInfo;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadProcess;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPHostInfo;
import be.spacebel.ese.downloadmanager.plugin.http.HttpClientController;
import be.spacebel.ese.downloadmanager.plugin.http.IHTTPDownloadProcess;
import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import esa.mep.downloader.exception.DMPluginException;
import esa.mep.downloader.plugin.EDownloadStatus;
import esa.mep.downloader.plugin.IDownloadProcess;
import esa.mep.downloader.plugin.IProductDownloadListener;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cnl
 */
public class USGSDownloadProcess implements IHTTPDownloadProcess {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(USGSDownloadProcess.class);
    
    private final IProductDownloadListener downloadListener;
    private WebDriver driver;
    private final HttpClientController httpClientController;
    private final DownloadProcessInfo processInfo;
    private HTTPDownloadInfo dlInfo;
    private HTTPDownloadProcess downloadProcess;
    private final String usgsUser;
    private final String usgsPassword;

    public USGSDownloadProcess(WebDriver driver, IProductDownloadListener downloadListener,
                        HttpClientController httpClientController,
            DownloadProcessInfo processInfo, String usgsUser, String usgsPassword) {
        this.downloadListener = downloadListener;
        this.driver = driver;
        this.processInfo = processInfo;
        this.httpClientController = httpClientController;
        this.usgsUser = usgsUser;
        this.usgsPassword=usgsPassword;
    }

 
    
    public  static void main(String[] args) throws URISyntaxException, DMPluginException, Exception {
        long time = 10;
        Preconditions.checkArgument(true);
        Preconditions.checkArgument(time >= 0, "Duration < 0: %d, time");
        Duration duration = new Duration(10, TimeUnit.SECONDS);
       Duration duration2 = new Duration(500, MILLISECONDS);
        HTTPHostInfo hostInfo = new HTTPHostInfo();
        hostInfo.setServer("earthexplorer.usgs.gov");
        hostInfo.setProtocol("http");
        hostInfo.setMaxConnections(3);
        HTTPDownloadConfiguration config = new HTTPDownloadConfiguration(
                hostInfo);
        HttpClientController controller = new HttpClientController(config);
        DownloadProcessInfo processInfo = new DownloadProcessInfo(new URI(
                "http://earthexplorer.usgs.gov/order/process?node=EC&dataset_name=LANDSAT_8&ordered=LC80980112013101LGN01"),
                new File("C:\\test.dat"), "cnoel", "Spacebel3#", null, 0,
                null, null);
       // USGSDownloadProcess process = new USGSDownloadProcess(drivernull,
         //        controller, processInfo,"cnoel","Spacebel3#");
        
        //process.startDownload();
    }

    public EDownloadStatus startDownload() throws DMPluginException {
        log.debug("Start USGS download");
        downloadListener.progress(0, new Long(0),
                EDownloadStatus.RUNNING, null);
        // Access the URL (using Selenium Driver)
        driver.get(this.processInfo.getProductURI().toString());
        // Fill username and password
        boolean signedin=false;
        WebElement userElement=null;
        try {
        userElement = driver.findElement(By.id("username"));
        signedin=false;
        }
        catch(Exception e) {
            log.debug("Browser is still signed in - skipping authentication");
            signedin=true;
        }
        if(!signedin) {
        userElement.sendKeys(this.usgsUser);
        WebElement passwordElement = driver.findElement(By.id("password"));
        passwordElement.sendKeys(this.usgsPassword);
        // Submit
        passwordElement.submit();
        }
        // Wait for download button and click
        WebDriverWait wait = new WebDriverWait(driver,10);
        WebElement downloadButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("div.ee-icon.ee-icon-download")));
        downloadButton.click();
        // After clicking close old tab and select the new tab
        driver.close();
        ArrayList<String> tabs2 = new ArrayList<String>(
                driver.getWindowHandles());
        driver.switchTo().window(tabs2.get(0));
        // Wait for the new download button
        WebElement realDL = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(
                        "//div[@id='optionsPage']/div/div/div[contains(.,'Level 1')]/parent::div/input")));
        
        Cookie cookie = driver.manage().getCookieNamed("EROS_SSO_production");
        // 2 cookies are used (update july 2017 !)
        Cookie cookie2 = driver.manage().getCookieNamed("EROS_SSO_production_secure");
        log.debug("Found cookie:"+cookie.getValue());
        log.debug("Found cookie2:"+cookie2.getValue());
        // Retrieve real url
        String onclick = realDL.getAttribute("onclick").replaceFirst(
                "window.location='", "");
        URI prodURI = null;
        try {
            prodURI = new URI(onclick.substring(0, onclick.length() - 1));
        } catch (URISyntaxException ex) {
            Logger.getLogger(USGSDownloadProcess.class.getName()).log(
                    Level.SEVERE,
                    null, ex);
        }
        processInfo.setProductURI(prodURI);
        log.debug("Found URL to download : " + prodURI);
        HttpUriRequest request = RequestBuilder.get().setUri(
                processInfo.getProductURI())
                .setHeader(HttpHeaders.COOKIE,
                        cookie.getName() + "=" + cookie.getValue()).
                setHeader(HttpHeaders.COOKIE,
                        cookie2.getName() + "=" + cookie2.getValue()).build();
        this.processInfo.setHttpUriRequest(request);
        log.debug("Creating for USGS the HTTP download");
        
        log.debug("For USGS adding HTTP task download");
        this.dlInfo = httpClientController.addDownloadTask(
                this, processInfo);
         dlInfo.setStatus(EDownloadStatus.RUNNING);
         log.debug("USGS status was changed to running !");
        return dlInfo.getStatus();
        
    }

    public EDownloadStatus pauseDownload() throws DMPluginException {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public EDownloadStatus resumeDownload() throws DMPluginException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public EDownloadStatus cancelDownload() throws DMPluginException {
        if (dlInfo != null) {
            httpClientController.removeDownloadTask(processInfo, true);

        }
        return dlInfo.getStatus();
    }

    public EDownloadStatus getStatus() {
        if (dlInfo == null) {
            log.debug("dl info is null");
            return EDownloadStatus.RUNNING;
        }
        
        return dlInfo.getStatus();
    }

    public File[] getDownloadedFiles() {
        if(dlInfo==null) {
            return null;
        }
       final File theFile = dlInfo.getFile();
        return new File[]{theFile};
    }

    public void disconnect() throws DMPluginException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void progress(String message, DMPluginException ex) {
           log.debug("progress ............................ " );
        // remove the task if download process is stopped (the "CANCELED" case is already handled int method "cancelDownload")
        if (dlInfo.getStatus() == EDownloadStatus.COMPLETED
                || dlInfo.getStatus() == EDownloadStatus.IN_ERROR) {
            log.debug("removing download task");
            httpClientController.removeDownloadTask(processInfo, false);
        }

        // call the Product Download Listener
        if (downloadListener == null) { // should be never null, condition exists only here for debug
            log.warn("No Download Listener instance available.");
            log.debug("Progress status for" + processInfo.getProductURI().getPath() + "@" + processInfo.getProductURI().getHost()
                    + " : [percentage=" + getProgress() + "%, downloadedSize=" + dlInfo.getFile().length() + "B, status=" + dlInfo.getStatus()
                    + ", message=" + message + (ex != null ? ", exception=" + ex.getMessage() : "") + "]");
        } else {
            Integer percentage = Math.round(getProgress());
            if (ex == null) {
                log.debug("downloaded file name: " + dlInfo.getFile().getName());
                log.debug("downloaded file size: " + dlInfo.getFile().length());
                /*
                    inform to DM the downloaded file name and size
                */
                downloadListener.productDetails(dlInfo.getFile().getName(), 1, dlInfo.getFile().length());
                /*
                    inform to DM the progress
                */                
                downloadListener.progress(percentage, dlInfo.getFile().length(), dlInfo.getStatus(), message);                
            } else {
                downloadListener.progress(percentage, dlInfo.getFile().length(), dlInfo.getStatus(), message, ex);
            }
        }
    }

     /**
     * For debug purpose. Calculate the percentage of progress.
     *
     * @deprecated Progress must be sent to the Product Download Listener (see
     * HTTPDownloadProcess#progress(String, DMPluginException))
     * @return percentage.
     */
    public float getProgress() {
        if (dlInfo.getFile() != null && dlInfo.getFile().isFile()) {
            if (dlInfo.getContentLength() != -1) {
                return ((float) dlInfo.getFile().length() / (float) dlInfo.getContentLength()) * 100;
            }
        }
        return -1;
    }
}
