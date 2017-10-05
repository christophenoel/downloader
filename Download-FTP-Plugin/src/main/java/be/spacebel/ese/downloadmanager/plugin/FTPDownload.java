package be.spacebel.ese.downloadmanager.plugin;

import esa.mep.downloader.plugin.EDownloadStatus;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FTPDownload {

    private static final Logger log = Logger.getLogger(FTPDownload.class);

    private final DownloadProcessInfo downloadProcessInfo;
    private boolean isCancel;
    private FTPClient ftpClient = null;
    private DownloadedInfo downloadInfo = null;

    public FTPDownload(DownloadProcessInfo newDownloadProcessInfo) {
        log.debug("new instance of FTPDownload......");
        this.downloadProcessInfo = newDownloadProcessInfo;
    }

    public DownloadedInfo download() throws IOException {
        getFTPClient();

        downloadInfo = new DownloadedInfo();
        downloadInfo.setDownloadedFiles(new ArrayList<File>());
        downloadInfo.setStatus(EDownloadStatus.RUNNING);
        log.debug("download info: " + downloadInfo.debug());

        if (isCancel) {
            downloadInfo.setStatus(EDownloadStatus.CANCELLED);
            return downloadInfo;
        }

        try {
            /*
             * if the URL points to a file, download it.
             */
            boolean isFile = isFile(downloadProcessInfo.getProductURI().getPath(), downloadProcessInfo.getProductURI());
            if (isFile) {
                log.debug("THIS IS URL TO FILE.");
                try {
                    String dirHierachy = getDirHierachy(downloadProcessInfo.getProductURI());
                    log.debug("dirHierachy : " + dirHierachy);
                    String downloadPath = (dirHierachy != null && !dirHierachy.isEmpty())
                            ? (downloadProcessInfo.getDownloadDir().getAbsolutePath() + "/" + dirHierachy)
                            : downloadProcessInfo.getDownloadDir().getAbsolutePath();
                    log.debug("downloadDir = " + downloadPath);
                    File downloadDir = new File(downloadPath);
                    if (!downloadDir.exists()) {
                        if (downloadDir.mkdirs()) {
                            log.debug("created downloaded dir: " + downloadDir.getAbsolutePath());
                        } else {
                            log.debug("Could not create downloaded dir: " + downloadDir.getAbsolutePath());
                        }
                    }
                    isFile = downloadSingleFile(downloadProcessInfo.getProductURI().getPath(), downloadDir, downloadInfo, downloadProcessInfo.getProductURI());
                } catch (IOException e) {
                    log.debug("IO: Error causes by method downloadSingleFile(): " + e.getMessage());
                    throw e;
                } catch (Exception e) {
                    log.debug("E: Error causes by method downloadSingleFile(): " + e.getMessage());
                }
            } else {
                /*
                 * otherwise download whole directory.
                 */
                log.debug("THIS IS URL TO DIRECTORY.");
                log.debug("product URI path : " + downloadProcessInfo.getProductURI().getPath());
                String productLocalPath = getProductLocalPath(downloadProcessInfo.getProductURI());
                log.debug("product local path : " + productLocalPath);

                String localDir = downloadProcessInfo.getDownloadDir().getAbsolutePath() + productLocalPath;
                log.debug("localDir : " + localDir);

                try {
                    downloadDirectory(downloadProcessInfo.getProductURI().getPath(), localDir, downloadInfo, true, downloadProcessInfo.getProductURI());
                    String productName = getProductName(downloadProcessInfo.getProductURI());
                    log.debug("productName : " + productName);

                    downloadInfo.setProductName(productName);
                    downloadInfo.getDownloadedFiles().add(new File(localDir));
                    if (downloadInfo.getStatus() == EDownloadStatus.RUNNING) {
                        log.debug("set COMPLETED status for downloadDirectory.");
                        downloadInfo.setStatus(EDownloadStatus.COMPLETED);
                    }
                } catch (IOException e) {
                    log.debug("IO: Error causes by method downloadDirectory(): " + e.getMessage());
                    throw e;
                } catch (Exception e) {
                    log.debug("E: Error causes by method downloadDirectory(): " + e.getMessage());
                }
            }
            stopFTP();
        } catch (IOException e) {
            log.debug("IO: Error causes in download() method: ");
            e.printStackTrace();
            throw e;
        } finally {
            if (ftpClient.isConnected()) {
                log.debug("disconnect the FTP.");
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                    log.debug("IO: Error causes by disconnecting the FTP connection in download ()method: " + ioe.getMessage());
                }
            }
        }
        return downloadInfo;
    }

    private void getFTPClient() throws IOException {
        try {
            createFTPClient();
        } catch (IOException e) {
            /*
             * retry the connection.
             */
            if (downloadProcessInfo.getAccount().getNumOfRetrials() > 0) {
                boolean connected = false;
                int delay = downloadProcessInfo.getAccount().getDelayTime();

                for (int i = 0; i < downloadProcessInfo.getAccount().getNumOfRetrials(); i++) {
                    log.debug(retrialTimes(i + 1) + "Re-connect to the FTP server.");

                    /* sleeping */
                    if (i > 0) {
                        delay = delay * intPower(i, downloadProcessInfo.getAccount().getDelayFactor());
                    }
                    log.debug("Sleep in " + (delay) + "s.");
                    try {
                        Thread.sleep(delay * 1000);
                    } catch (InterruptedException ie) {
                        log.debug("Error when waiting for the next retrial: " + ie.getLocalizedMessage());
                    }

                    try {
                        createFTPClient();
                        connected = true;
                        log.debug("The FTP connection is OK now.");
                        break;
                    } catch (IOException ioe) {
                        log.debug(retrialTimes(i + 1) + " Error when reconnecting to the server: " + ioe.getMessage());
                    }
                }

                if (!connected) {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    private void createFTPClient() throws IOException {
        ftpClient = new FTPClient();

        if (log.isDebugEnabled() && isNotNullOrEmpty(downloadProcessInfo.getAccount().getLogsDir())) {
            try {
                String logFileName = validateFileName(downloadProcessInfo.getProductURI().toString());

                File logFile = new File(downloadProcessInfo.getAccount().getLogsDir() + File.separator + logFileName + ".log");
                if (!logFile.getParentFile().exists()) {
                    logFile.getParentFile().mkdirs();
                }

                int count = 1;
                while (true) {
                    if (logFile.exists()) {
                        logFile = new File(downloadProcessInfo.getAccount().getLogsDir() + File.separator + logFileName + "_" + count + ".log");
                        count++;
                        log.debug("Log file exists, continue....");
                    } else {
                        log.debug("Log file does not exist, stop.");
                        break;
                    }
                }

                log.debug("Log file: " + logFile.getAbsolutePath());
                ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(logFile)));
            } catch (IOException e) {
                log.debug("Error when creating log file: " + e.getMessage());
            }
        }

        ftpClient.setControlEncoding("UTF-8");

        if (downloadProcessInfo.getProductURI().getPort() == -1) {
            ftpClient.connect(downloadProcessInfo.getProductURI().getHost());
        } else {
            ftpClient.connect(downloadProcessInfo.getProductURI().getHost(), downloadProcessInfo.getProductURI().getPort());
        }

        if (downloadProcessInfo.getAccount().isActiveMode()) {
            log.debug("Connect to FTP server in ACTIVE mode.");
        } else {
            ftpClient.enterLocalPassiveMode();
            log.debug("Connect to FTP server in PASSIVE mode.");
        }

        if (isNotNullOrEmpty(downloadProcessInfo.getAccount().getUser()) && isNotNullOrEmpty(downloadProcessInfo.getAccount().getPassword())) {
            ftpClient.login(downloadProcessInfo.getAccount().getUser(), downloadProcessInfo.getAccount().getPassword());
        }

        // check the reply code to verify success.
        int reply = ftpClient.getReplyCode();
        log.debug("Reply code: " + reply);

        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("Unexpected response when connecting to the server " + downloadProcessInfo.getProductURI().getHost() + ", FTP response code " + ftpClient.getReplyString());
        }

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        this.ftpClient.setControlKeepAliveTimeout(60);
        int dataTimeout = 10 * 60 * 1000;
        if (this.downloadProcessInfo.getAccount().getDataReadingTimeout() > 0) {
            dataTimeout = this.downloadProcessInfo.getAccount().getDataReadingTimeout();
        }
        this.ftpClient.setDataTimeout(dataTimeout);
    }

    private boolean isFile(String remotePath, URI productURI) throws IOException {
        log.debug("Enter isFile(remotePath = " + remotePath + ", productURI = " + productURI + ")...........");
        FTPFile[] subFiles = ftpClient.listFiles(remotePath);

        if (subFiles != null && subFiles.length > 0) {
            log.debug("Num of subFiles = " + subFiles.length);
            String fileName = getFileName(subFiles[0].getName());
            if (subFiles.length == 1 && remotePath.endsWith("/" + fileName)) {
                log.debug("This is an URL to File.");
                return true;
            }
            log.debug("This is NOT an URL to File.");
            return false;
        } else {
            log.debug("There is no file/dir in the path: " + remotePath);
            throw new IOException("The file/directory: " + getFTPServerInfo(productURI) + remotePath + " does not exist.");
        }
    }

    private boolean downloadSingleFile(String remotePath, File downloadDir, DownloadedInfo downloadInfo, URI productURI) throws IOException {
        log.debug("Enter downloadSingleFile............");
        FTPFile[] subFiles = ftpClient.listFiles(remotePath);

        if (subFiles != null) {
            log.debug("num of subFiles : " + subFiles.length);
        }

        if (subFiles != null && subFiles.length == 1) {
            String fileName = getFileName(subFiles[0].getName());
            if (remotePath.endsWith("/" + fileName)) {
                File localFile = new File(downloadDir.getAbsolutePath() + File.separator + fileName);
                if (!downloadDir.exists()) {
                    if (localFile.getParentFile().mkdirs()) {
                        log.debug("created downloaded dir: " + downloadDir.getAbsolutePath());
                    } else {
                        log.debug("could not create downloaded dir: " + downloadDir.getAbsolutePath());
                    }
                }
                try {
                    downloadOneFileWithAttemps(ftpClient, remotePath, subFiles[0].getSize(), localFile, true);
                    downloadInfo.setProductName(fileName);
                    downloadInfo.getDownloadedFiles().add(localFile);
                } catch (IOException e) {
                    log.debug("IO:Error happen when invoking method downloadOneFileWithAttemps() in downloadSingleFile(): " + e.getMessage());
                    throw e;
                } catch (Exception e) {
                    log.debug("E: Error happen when invoking method downloadOneFileWithAttemps() in downloadSingleFile(): " + e.getMessage());
                }
                return true;
            }
        } else {
            log.debug("There is no file/dir in the path: " + remotePath);
            throw new IOException("The file/directory: " + getFTPServerInfo(productURI) + remotePath + " does not exist.");
        }
        return false;
    }

    private void downloadDirectory(String remotePath, String localPath, DownloadedInfo downloadInfo, boolean firstCall, URI productURI) throws IOException {
        log.debug("Download a directory (remote path=" + remotePath + ", localPath=" + localPath + ", downloadInfo = " + downloadInfo.debug() + ")");

        if (isCancel) {
            log.debug("Cancel at starting.");
            downloadInfo.setStatus(EDownloadStatus.CANCELLED);
        } else {
            File downloadDir = new File(localPath);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }

            FTPFile[] subFiles = ftpClient.listFiles(remotePath);

            if (subFiles != null) {
                log.debug("num of subFiles : " + subFiles.length);
            }

            if (subFiles != null && subFiles.length > 0) {
                for (FTPFile sFile : subFiles) {
                    String fileName = getFileName(sFile.getName());
                    if (!fileName.startsWith("/")) {
                        fileName = "/" + fileName;
                    }

                    String remoteSubPath = remotePath + fileName;
                    log.debug("Remote sub path: " + remoteSubPath);
                    String localSubPath = localPath + fileName;
                    log.debug("Local sub path: " + localSubPath);

                    if (isCancel) {
                        log.debug("Cancel in directories loop.");
                        downloadInfo.setStatus(EDownloadStatus.CANCELLED);
                        break;
                    }

                    if (sFile.isFile()) {
                        try {
                            log.debug("This is a file, download it to local: " + remoteSubPath);
                            downloadOneFileWithAttemps(ftpClient, remoteSubPath, sFile.getSize(), new File(localSubPath), false);
                        } catch (IOException e) {
                            log.debug("IO: Error happen when invoking method downloadOneFileWithAttemps() in downloadDirectory(): " + e.getMessage());
                            throw e;
                        } catch (Exception e) {
                            log.debug("E: Error happen when invoking method downloadOneFileWithAttemps() in downloadDirectory(): " + e.getMessage());
                        }
                    } else if (sFile.isDirectory()) {
                        try {
                            log.debug("This is a directory, look inside it: " + remoteSubPath);
                            downloadDirectory(remoteSubPath, localSubPath, downloadInfo, false, productURI);
                        } catch (IOException e) {
                            log.debug("IO: Error happen when invoking method downloadDirectory() in downloadDirectory(): " + e.getMessage());
                            throw e;
                        } catch (Exception e) {
                            log.debug("E: Error happen when invoking method downloadDirectory() in downloadDirectory(): " + e.getMessage());
                        }
                    }
                }
            } else {
                log.debug("There is no file/dir in the path: " + remotePath);
                if (firstCall) {
                    throw new IOException("The file/directory: " + getFTPServerInfo(productURI) + remotePath + " does not exist.");
                }
            }
        }
    }

    private void downloadOneFileWithAttemps(FTPClient ftpClient, String remotePath, long remoteSize, File localFile, boolean isFile) throws IOException {
        log.debug("Enter downloadOneFileWithAttemps (remotePath = " + remotePath + ", remoteSize = " + remoteSize + ").");
        downloadInfo.setOverallSize(downloadInfo.getOverallSize() + remoteSize);

        DownloadStatus dlStatus = DownloadStatus.FAILED;
        try {
            dlStatus = saveDownloadedFile(remotePath, localFile, remoteSize);
        } catch (IOException e) {
            log.debug("IO: Error when saving downloaded file to local: " + e.getMessage());
            e.printStackTrace();
            if (downloadProcessInfo.getAccount().getNumOfRetrials() < 1) {
                throw e;
            }
        } catch (Exception e) {
            log.debug("E: Error when saving downloaded file to local: " + e.getMessage());
            e.printStackTrace();
            if (downloadProcessInfo.getAccount().getNumOfRetrials() < 1) {
                throw new IOException(e);
            }
        }
        if (dlStatus == DownloadStatus.CANCELLED) {
            downloadInfo.setStatus(EDownloadStatus.CANCELLED);
            log.debug("Download CANCELLED: " + remotePath);
        } else if (dlStatus == DownloadStatus.SUCCESS) {
            log.debug("Download SUCCESS: " + remotePath);
            if (isFile) {
                downloadInfo.setStatus(EDownloadStatus.COMPLETED);
            }
            downloadInfo.setDownloadedSize(downloadInfo.getDownloadedSize() + remoteSize);
            log.debug("downloaded size: " + downloadInfo.getDownloadedSize() + remoteSize);

            downloadInfo.setNumberOfFiles(downloadInfo.getNumberOfFiles() + 1);
            log.debug("number of files: " + downloadInfo.getNumberOfFiles());

            log.debug("downloaded successfully product: " + remotePath + " ,size = " + remoteSize);
        } else {
            if (isCancel) {
                log.debug("Do not retry due to the download is cancelled.");
                downloadInfo.setStatus(EDownloadStatus.CANCELLED);
            } else {
                log.debug("Download FAILS: " + remotePath);
                /*
                 * retry the download
                 */
                if (downloadProcessInfo.getAccount().getNumOfRetrials() > 0) {
                    log.debug("Redownload file: " + remotePath);
                    int delay = downloadProcessInfo.getAccount().getDelayTime();

                    for (int i = 0; i < downloadProcessInfo.getAccount().getNumOfRetrials(); i++) {
                        log.debug(retrialTimes(i + 1));

                        if (isCancel) {
                            log.debug("Stop retry due to the download is cancelled.");
                            downloadInfo.setStatus(EDownloadStatus.CANCELLED);
                            break;
                        }

                        /* sleeping */
                        if (i > 0) {
                            delay = delay * downloadProcessInfo.getAccount().getDelayFactor();
                        }

                        stopFTP();

                        log.debug("Sleep in " + (delay) + "s.");
                        try {
                            Thread.sleep(delay * 1000);
                        } catch (InterruptedException e) {
                            log.debug("Error when waiting for the next retrial: " + e.getMessage());
                        }

                        try {
                            createFTPClient();
                            dlStatus = saveDownloadedFile(remotePath, localFile, remoteSize);
                            if (dlStatus == DownloadStatus.SUCCESS) {
                                log.debug("Redownload SUCCESS: " + remotePath);
                                downloadInfo.setDownloadedSize(downloadInfo.getDownloadedSize() + remoteSize);
                                downloadInfo.setNumberOfFiles(downloadInfo.getNumberOfFiles() + 1);
                                log.debug("downloaded successfully product: " + remotePath + " ,size = " + remoteSize);
                                break;
                            }
                            if (dlStatus == DownloadStatus.CANCELLED) {
                                log.debug("Stop redownload due to the download is cancelled.");
                                downloadInfo.setStatus(EDownloadStatus.CANCELLED);
                                break;
                            }
                        } catch (IOException ioe) {
                            log.debug("IO: retry download error: " + ioe.getMessage());
                            ioe.printStackTrace();
                            stopFTP();
                        } catch (Exception e) {
                            log.debug("E: retry download error: " + e.getMessage());
                            e.printStackTrace();
                            stopFTP();
                        }
                    }
                } else {
                    log.debug("Do not retry the download.");
                }
            }
        }
        if (dlStatus == DownloadStatus.FAILED) {
            throw new IOException("Could not download product: " + remotePath);
        }
    }

    private DownloadStatus saveDownloadedFile(String remoteFile, File localFile, long remoteSize) throws IOException {
        log.debug("Saving remote file " + remoteFile + " to local: " + localFile.getAbsolutePath());

        FileOutputStream fos = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        DownloadStatus status = DownloadStatus.FAILED;
        long startReadingTime = 0;
        try {
            long startTime = System.currentTimeMillis();
            log.debug("read input stream from server.");
            fos = new FileOutputStream(localFile);
            outputStream = new BufferedOutputStream(fos);
            inputStream = ftpClient.retrieveFileStream(remoteFile);

            if (inputStream != null) {
                log.debug("write stream to local.");
                int numOfbytes = 10240;
                byte[] bytesArray = new byte[numOfbytes];

                while (true) {
                    startReadingTime = System.currentTimeMillis();
                    int bytesRead = inputStream.read(bytesArray);
                    if (bytesRead == -1) {
                        log.debug("End of the file. Stop !");
                        break;
                    } else {
                        if (bytesRead > 0) {
                            outputStream.write(bytesArray, 0, bytesRead);
                        } else {
                            log.debug("NO BYTES ARE READ.......................... BREAK! ");
                            break;
                        }
                    }
                }
                log.debug("Finish read/write stream.");

                long endTime = System.currentTimeMillis();
                log.debug("Saved successful remote file " + remoteFile + " to local: " + localFile.getAbsolutePath() + " in " + (endTime - startTime) + " miliseconds.");
            } else {
                log.debug("COULD NOT GET DATA FROM THE FTP SERVER.");
                throw new IOException("The data connection cannot be opened.");
            }
        } catch (IOException e) {
            log.debug("IO: Error when saving file to local in saveDownloadedFile(): " + e.getMessage());
            log.debug("START: " + startReadingTime + ", END: " + System.currentTimeMillis());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            log.debug("E: Error when saving file to local in saveDownloadedFile(): " + e.getMessage());
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            try {
                log.debug("Close the streams.");
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    log.debug("Error when closing OutputStream: " + e.getMessage());
                }

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.debug("Error when closing InputStream: " + e.getMessage());
                }

                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    log.debug("Error when closing FileOutputStream: " + e.getMessage());
                }
            } catch (Exception e) {
            }
        }

        if (ftpClient.completePendingCommand()) {
            status = DownloadStatus.SUCCESS;
        } else {
            log.debug("FAILED STATUS.");
        }
        return status;
    }

    private static String getProductName(URI productURI) {
        String productName = null;
        if (productURI != null && productURI.getPath() != null) {
            File file = new File(productURI.getPath());
            productName = file.getName();
        }
        if (productName == null || productName.isEmpty()) {
            productName = productURI.getHost();
        }
        return productName;
    }

    private static String getProductLocalPath(URI productURI) {
        String productPath = File.separator;
        if (productURI != null && productURI.getPath() != null) {
            if (productURI.getPath().startsWith("/")) {
                productPath = productURI.getPath();
            } else {
                productPath += productURI.getPath();
            }
        }
        return productPath;
    }

    private static String getFTPServerInfo(URI productURI) {
        String scheme = productURI.getScheme();
        String port = productURI.getPort() == -1 ? "" : ":" + productURI.getPort();
        return scheme + "://" + productURI.getHost() + port;
    }

    private boolean testConnection(FTPClient ftpClient) {
        log.debug("Check the connection.");
        boolean connected = false;
        try {
            int status = ftpClient.noop();
            if (status >= 200 && status < 300) {
                connected = true;
                log.debug("The connection is still OK.");
            }
        } catch (IOException e) {
            log.debug("Error when checking the FTP connection: " + e.getMessage());
        }
        return connected;
    }

    private String retrialTimes(int index) {
        String result = "The " + index + "th retrial: ";
        if (index == 1) {
            result = "The 1st retrial: ";
        }
        if (index == 2) {
            result = "The 2nd retrial: ";
        }
        if (index == 3) {
            result = "The 3rd retrial: ";
        }
        return result;
    }

    private void stopFTP() {
        log.debug("Stop FTP client.");
        if (ftpClient != null) {
            try {
                boolean connected = testConnection(ftpClient);
                if (connected) {
                    ftpClient.logout();
                }
            } catch (IOException ioe) {
                log.debug("IO: Error causes by logout the FTP connection:");
                ioe.printStackTrace();
            } finally {
                if (ftpClient.isConnected()) {
                    try {
                        log.debug("Disconnect.");
                        ftpClient.disconnect();
                    } catch (IOException ioe) {
                        log.debug("IO: Error causes by disconnecting the FTP connection:");
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }

    private String validateFileName(String fileName) {
        String newName = fileName.replaceAll("/", "slash");
        newName = newName.replaceAll(":", "colon");
        return newName;
    }

    private String getDirHierachy(URI productUri) {
        String path = productUri.getPath();
        path = trimSlashs(path);
        int index = path.lastIndexOf("/");
        log.debug("getDirHierachy.index = " + index);
        if (index == -1) {
            return "";
        } else {
            return path.substring(0, index);
        }
    }

    private String trimSlashs(String path) {
        if (path != null && !path.isEmpty()) {
            while (true) {
                if (path.endsWith("/")) {
                    path = path.substring(0, (path.length() - 1));
                } else {
                    break;
                }
            }
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

    private int intPower(int base, int exp) {
        log.debug(base + "^" + exp);
        int result = 1;
        while (exp != 0) {
            if ((exp & 1) != 0) {
                result *= base;
            }
            exp >>= 1;
            base *= base;
        }
        log.debug("Result: " + result);
        return result;
    }

    private String getFileName(String filePath) {
        String fileName = filePath;
        int lastIndex = filePath.lastIndexOf("/");
        log.debug("lastIndex = " + lastIndex);
        if (lastIndex > -1) {
            fileName = filePath.substring(lastIndex + 1);
        }
        log.debug("file name = " + fileName);
        return fileName;
    }

    private boolean isNotNullOrEmpty(String str) {
        if (str != null && !str.trim().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void setCancel(boolean cancel) {
        this.isCancel = cancel;
    }

    public enum DownloadStatus {

        SUCCESS, FAILED, CANCELLED
    }

}
