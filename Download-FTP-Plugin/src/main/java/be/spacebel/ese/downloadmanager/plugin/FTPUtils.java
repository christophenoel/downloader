/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.ese.downloadmanager.plugin;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 *
 * @author mng
 */
public class FTPUtils {

    private FTPClient ftpClient = null;

    private static final Logger log = Logger.getLogger(FTPUtils.class);

    public List<String> listFiles(FTPDownloadAccount ftpAccount, URI ftpURI) throws IOException {
        log.debug("listFiles(ftpAccount = " + ftpAccount.debug() + ", ftpURI = " + ftpURI);
        try {
            initFTPClient(ftpAccount, ftpURI);
            String serverPart = ftpURI.getScheme() + "://" + ftpURI.getAuthority();
            log.debug("Server part: " + serverPart);
            List<String> fileUrls = new ArrayList<String>();
            List<Boolean> areFiles = new ArrayList<Boolean>();
            listFiles(ftpURI.getPath(), serverPart, fileUrls, areFiles);
            boolean isFile = (areFiles.size() > 0) ? areFiles.get(0) : false;
            log.debug("isFile = " + isFile);
            if (isFile) {
                return new ArrayList<String>();
            }
            return fileUrls;
        } catch (IOException e) {
            disconnectFTP();
            throw e;
        } finally {
            disconnectFTP();
        }
    }

    private void initFTPClient(FTPDownloadAccount ftpAccount, URI productURI) throws IOException {
        this.ftpClient = new FTPClient();

        this.ftpClient.setControlEncoding("UTF-8");

        log.debug("host: " + productURI.getHost());
        log.debug("port: " + productURI.getPort());

        if (productURI.getPort() == -1) {
            this.ftpClient.connect(productURI.getHost());
        } else {
            this.ftpClient.connect(productURI.getHost(), productURI.getPort());
        }

        if (ftpAccount.isActiveMode()) {
            log.debug("Connect to FTP server in ACTIVE mode.");
        } else {
            this.ftpClient.enterLocalPassiveMode();
            log.debug("Connect to FTP server in PASSIVE mode.");
        }

        if (StringUtils.isNotEmpty(ftpAccount.getUser()) && StringUtils.isNotEmpty(ftpAccount.getPassword())) {
            log.debug("Login to FTP server under account: " + ftpAccount.getUser() + "/xxxxxx");
            this.ftpClient.login(ftpAccount.getUser(), ftpAccount.getPassword());
        }

        // check the reply code to verify success.
        int reply = this.ftpClient.getReplyCode();
        log.debug("Reply code: " + reply);

        if (!FTPReply.isPositiveCompletion(reply)) {
            String errorMsg = this.ftpClient.getReplyString();
            if (FTPReply.isNegativeTransient(reply)) {
                log.debug("Transient error: " + errorMsg);
                this.ftpClient.disconnect();
            } else {
                log.debug("Not transient error: " + errorMsg);
                this.ftpClient.disconnect();
                throw new IOException("Unexpected response when connecting to the server " + productURI.getHost() + ", FTP response code " + errorMsg);
            }
        }

        this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        this.ftpClient.setControlKeepAliveTimeout(60);
        this.ftpClient.setDataTimeout(60 * 60 * 1000); // 60 minutes
        if (log.isDebugEnabled()) {
            this.ftpClient.addProtocolCommandListener(new PrintCommandListener(System.out));
        }
    }

    private void listFiles(String path, String serverPart, List<String> fileUrls, List<Boolean> areFiles) throws IOException {
        log.debug("listFiles(path = " + path + ", serverPart = " + serverPart + ", fileUrls = " + StringUtils.join(fileUrls, ";") + ", areFiles = " + StringUtils.join(areFiles, ";"));
        boolean isFile = false;

        FTPFile[] subFiles = this.ftpClient.listFiles(path);
        if (subFiles != null && subFiles.length > 0) {
            if (subFiles.length > 1) {
                listFiles(path, fileUrls, serverPart);
            } else {
                if (subFiles.length == 1) {
                    String fileName = subFiles[0].getName();
                    log.debug("fileName = " + fileName);
                    String onlyFileName = StringUtils.substringAfterLast(fileName, "/");
                    log.debug("onlyFileName = " + onlyFileName);
                    if (StringUtils.isNotEmpty(onlyFileName)) {
                        fileName = onlyFileName;
                    }
                    fileName = removeFirstSlashs(fileName);

                    if (path.endsWith("/" + fileName)) {
                        log.debug("endsWith case");
                        boolean changeToDir = ftpClient.changeWorkingDirectory(path);
                        log.debug("changeToDir : " + changeToDir);
                        int replyCode = this.ftpClient.getReplyCode();
                        log.debug("replyCode : " + replyCode);
                        if (!changeToDir && replyCode == 550) {
                            isFile = true;
                        } else {
                            listFiles(path, fileUrls, serverPart);
                        }
                    } else {
                        log.debug("not endsWith case");
                        listFiles(path, fileUrls, serverPart);
                    }
                }

            }
        } else {
            log.debug("The dir/file doesn't exist: " + path);
            throw new IOException("The file/directory " + serverPart + "/" + removeFirstSlashs(path) + " does not exist.");
        }

        log.debug("isFile : " + isFile);
        areFiles.add(isFile);
    }

    private void listFiles(String ftpUrl, List<String> fileUrls, String serverPart) throws IOException {
        log.debug("listFiles(ftpUrl = " + ftpUrl + ", fileUrls = " + StringUtils.join(fileUrls, ";") + ", serverPart = " + serverPart);

        FTPFile[] subFiles = this.ftpClient.listFiles(ftpUrl);

        if (subFiles != null) {
            log.debug("Num of subFiles : " + subFiles.length);
        }

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile sFile : subFiles) {
                String fileName = sFile.getName();
                log.debug("Full file path = " + fileName);
                if (StringUtils.contains(fileName, "/")) {
                    fileName = StringUtils.substringAfterLast(fileName, "/");
                }
                log.debug("Only file name = " + fileName);
                fileName = ftpUrl + "/" + fileName;
                if (sFile.isFile()) {
                    log.debug("This is file: " + fileName);
                    log.debug("URL to File: " + serverPart + fileName);
                    fileUrls.add(serverPart + fileName);
                }
                if (sFile.isDirectory()) {
                    log.debug("This is directory: " + fileName + ", continue to look for the leaf file.");
                    listFiles(fileName, fileUrls, serverPart);
                }
            }
        }
    }

    private void disconnectFTP() throws IOException {
        if (this.ftpClient != null && this.ftpClient.isConnected()) {
            this.ftpClient.disconnect();
        }
    }

    private String removeFirstSlashs(String path) {
        if (StringUtils.isNotEmpty(path)) {
            while (true) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                } else {
                    break;
                }
            }
        }
        return path;
    }
}
