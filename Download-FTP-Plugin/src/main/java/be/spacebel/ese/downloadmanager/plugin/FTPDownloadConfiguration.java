package be.spacebel.ese.downloadmanager.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FTPDownloadConfiguration {

    // private static final String CONFIGURATION_DIR = "ese-ftp-plugin";
    private static final String CONFIGURATION_DIR = "mep-ftp-plugin";
    // private static final String RESOURCES_FILE = "resources.properties";
    private static final String FTP_ACCOUNTS_FILE = "ftp-accounts.xml";
    private static final String ACCOUNT_ELEM = "account";
    private static final String BASE_ELEM = "ftpBase";
    private static final String USER_ELEM = "ftpUser";
    private static final String PASS_ELEM = "ftpPassword";
    private static final String ACTIVE_ELEM = "ftpActiveMode";
    private static final String MAX_CONNECTIONS_ELEM = "ftpMaxConnections";
    private static final String RETRIAL_ELEM = "ftpRetrial";
    private static final String NUM_OF_ATTEMPT_ATTR = "numOfAttempts";
    private static final String DELAY_ATTR = "delay";
    private static final String FACTOR_ATTR = "factor";
    private static final String DATA_TIMEOUT_ELEM = "dataReadingTimeout";

    private List<FTPDownloadAccount> ftpAccounts;
    private String logsDir;
    private Logger log = Logger.getLogger(this.getClass());

    public FTPDownloadConfiguration(File pluginCfgRootDir, Map<String, Integer> connectionsPerAccountMap) throws Exception {
        String configDir = pluginCfgRootDir.getAbsolutePath(); //+ File.separator + CONFIGURATION_DIR;

        File logDir = new File(configDir + File.separator + "logs");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        this.logsDir = logDir.getAbsolutePath();

        // TODO: uncomment the code line if you want to read the configuration
        // from resources.properties
        // resources.load(new FileInputStream(configDir + File.separator +
        // RESOURCES_FILE));
        String ftpAccountsFilePath = configDir + File.separator + FTP_ACCOUNTS_FILE;
        loadFTPAccounts(ftpAccountsFilePath, connectionsPerAccountMap);
    }

    private void loadFTPAccounts(String ftpAccountsFilePath, Map<String, Integer> connectionsPerAccountMap) throws Exception {
        log.debug("FTP accounts file location:" + ftpAccountsFilePath);

        File ftpAccountsFile = new File(ftpAccountsFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(ftpAccountsFile);
        doc.getDocumentElement().normalize();

        NodeList accountNodes = doc.getElementsByTagName(ACCOUNT_ELEM);

        ftpAccounts = new ArrayList<FTPDownloadAccount>();
        for (int i = 0; i < accountNodes.getLength(); i++) {
            Node accountNode = accountNodes.item(i);

            if (accountNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) accountNode;
                String base = getNodeValue(eElement.getElementsByTagName(BASE_ELEM).item(0));
                String user = getNodeValue(eElement.getElementsByTagName(USER_ELEM).item(0));
                String password = getNodeValue(eElement.getElementsByTagName(PASS_ELEM).item(0));
                boolean isActive = false;
                try {
                    String activeMode = getNodeValue(eElement.getElementsByTagName(ACTIVE_ELEM).item(0));
                    if (activeMode != null && activeMode.equalsIgnoreCase("true")) {
                        isActive = true;
                    }
                } catch (Exception e) {
                    log.debug("Error while getting FTP Active mode configuration:" + e.getLocalizedMessage());
                }

                FTPDownloadAccount ftpAccount = new FTPDownloadAccount(base, user, password, isActive);
                ftpAccount.setLogsDir(this.logsDir);
                try {
                    String maxConnections = getNodeValue(eElement.getElementsByTagName(MAX_CONNECTIONS_ELEM).item(0));
                    ftpAccount.setMaxConnections(Integer.parseInt(maxConnections));
                } catch (Exception e) {
                    ftpAccount.setMaxConnections(-1);
                    log.debug("Error while getting FTP max connection configuration:" + e.getLocalizedMessage());
                }

                int dataTimeout = 10 * 60 * 1000; // convert 10 minutes into milliseconds
                try {
                    String dataReadingTimeout = getNodeValue(eElement.getElementsByTagName(DATA_TIMEOUT_ELEM).item(0));
                    dataTimeout = Integer.parseInt(dataReadingTimeout);
                    dataTimeout = dataTimeout * 60 * 1000; // convert minute into milliseconds
                } catch (Exception e) {
                    log.debug("Error while getting data timeout value in the configuration:" + e.getLocalizedMessage());
                }
                ftpAccount.setDataReadingTimeout(dataTimeout);

                int numOfRetrials = 0;
                int delay = 10;
                int factor = 1;

                try {
                    /*
                     * MNG: Now the retrial will be taken by Data Manager
                     */
                    /*
                     * Node retrialNode = eElement.getElementsByTagName(RETRIAL_ELEM).item(0); String numOfAttempt = getAttValueOfNode(retrialNode, NUM_OF_ATTEMPT_ATTR); if
                     * (numOfAttempt != null) { numOfRetrials = Integer.parseInt(numOfAttempt); }
                     * 
                     * String delayTime = getAttValueOfNode(retrialNode, DELAY_ATTR); if (delayTime != null) { delay = Integer.parseInt(delayTime); }
                     * 
                     * String delayFactor = getAttValueOfNode(retrialNode, FACTOR_ATTR); if (delayFactor != null) { factor = Integer.parseInt(delayFactor); }
                     */

                } catch (Exception e) {
                    ftpAccount.setNumOfRetrials(0);
                    log.debug("Error while getting FTP number of retrials configuration:" + e.getLocalizedMessage());
                }

                ftpAccount.setNumOfRetrials(numOfRetrials);
                ftpAccount.setDelayTime(delay);
                ftpAccount.setDelayFactor(factor);

                ftpAccounts.add(ftpAccount);
                connectionsPerAccountMap.put(ftpAccount.getUuid(), ftpAccount.getMaxConnections());
            }
        }
    }

    private String getNodeValue(Node node) {
        String value = null;
        try {
            value = node.getFirstChild().getNodeValue();
        } catch (Exception e) {

        }
        return value;
    }

    private String getAttValueOfNode(Node node, String attName) {
        String value = null;
        if (node.getAttributes() != null && node.getAttributes().getNamedItem(attName) != null) {
            value = node.getAttributes().getNamedItem(attName).getNodeValue();
        }
        return value;
    }

    public FTPDownloadAccount getFTPAccount(String ftpBase) {
        for (FTPDownloadAccount ftpAccount : ftpAccounts) {
            if (ftpBase.startsWith(ftpAccount.getBase())) {
                return ftpAccount;
            }
        }
        log.debug("There is no login account for this FTP server: " + ftpBase + ". Therefore the anonymous user will be used.");
        return new FTPDownloadAccount(ftpBase, "anonymous", "guest", false);
    }

    public List<FTPDownloadAccount> getFTPAccounts() {
        return ftpAccounts;
    }

    public int getMaxConnectionByUUID(String uuid) {
        for (FTPDownloadAccount ftpAccount : ftpAccounts) {
            if (uuid.equals(ftpAccount.getUuid())) {
                return ftpAccount.getMaxConnections();
            }
        }
        return -1;
    }

}
