package be.spacebel.ese.downloadmanager.plugin;

import java.util.UUID;

public class FTPDownloadAccount {

    private String base;
    private String user;
    private String password;
    private boolean isActiveMode;
    private int maxConnections;
    private int numOfRetrials;
    private int delayTime;
    private int delayFactor;
    private int dataReadingTimeout;
    private String uuid;
    private String logsDir;

    public FTPDownloadAccount() {
        this.uuid = UUID.randomUUID().toString();
    }

    public FTPDownloadAccount(String base, String user, String password, boolean isActiveMode) {
        this.base = base;
        this.user = user;
        this.password = password;
        this.isActiveMode = isActiveMode;
        this.uuid = UUID.randomUUID().toString();
        this.maxConnections = -1;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
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

    public boolean isActiveMode() {
        return isActiveMode;
    }

    public void setActiveMode(boolean isActiveMode) {
        this.isActiveMode = isActiveMode;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getNumOfRetrials() {
        return numOfRetrials;
    }

    public void setNumOfRetrials(int numOfRetrials) {
        this.numOfRetrials = numOfRetrials;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public int getDelayFactor() {
        return delayFactor;
    }

    public void setDelayFactor(int delayFactor) {
        this.delayFactor = delayFactor;
    }

    public String getLogsDir() {
        return logsDir;
    }

    public void setLogsDir(String logsDir) {
        this.logsDir = logsDir;
    }

    public int getDataReadingTimeout() {
        return dataReadingTimeout;
    }

    public void setDataReadingTimeout(int dataReadingTimeout) {
        this.dataReadingTimeout = dataReadingTimeout;
    }

    public String debug() {
        return "FTPDownloadAccount[base = " + base + ", user = " + user + ", password = "
                + password + ", isActiveMode = " + isActiveMode + ", maxConnections = "
                + maxConnections + ", numOfRetrials = " + numOfRetrials + ", delayTime = "
                + delayTime + ", delayFactor = " + delayFactor + ", logsDir = " + logsDir
                + ", dataReadingTimeout = " + dataReadingTimeout + "]";
    }
}
