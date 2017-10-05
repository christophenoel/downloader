package be.spacebel.ese.downloadmanager.plugin.http;


import esa.mep.downloader.exception.DMPluginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author ane
 */
public class HTTPDownloadConfiguration {

    private static final Logger LOG = LogManager.getLogger(HTTPDownloadConfiguration.class);

    // consts
    private static final String HTTP_ACCOUNTS_FILE = "http-accounts.xml";

    // the map that associates the hostname to its info
    private final Map<String, HTTPHostInfo> hostInfoMap = new HashMap<>();


    /**
     * Constructs the configuration for HTTPDownload plugin.
     * It reads the "http-accounts.xml" file in given directory.
     * If the file cannot be found or parsed, then an exception is thrown.
     *
     * @param pluginCfgRootDir The directory that contains "http-accounts.xml".
     * @throws DMPluginException in case the file is not found or is malformed.
     */
    public HTTPDownloadConfiguration(File pluginCfgRootDir) throws DMPluginException {
        // retrieve http-accounts.xml file
        String httpAccountsFilePath = FilenameUtils.concat(pluginCfgRootDir.getPath(), HTTP_ACCOUNTS_FILE);
        File httpAccountsFile = new File(httpAccountsFilePath);
        if (httpAccountsFile.isFile() == false) {
            throw new DMPluginException("File http-accounts.xml not found.", new FileNotFoundException(httpAccountsFilePath));
        }
        LOG.debug("File http-accounts.xml found: " + httpAccountsFilePath);

        // parse config file to get list of host info
        List<HTTPHostInfo> hostInfoList = null;
        try {
            HttpAccountsXmlReader xmlReader = new HttpAccountsXmlReader();
            xmlReader.parseXmlFile(httpAccountsFile);
            hostInfoList = xmlReader.getHostInfoList();
            LOG.debug("Retrieved " + hostInfoList.size() + " http accounts.");
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            // impossible to continue without configuration
            String message = "Failed to read http-accounts.xml.";
            LOG.error(message, ex);
            throw new DMPluginException(message, ex);
        }

        // transform host info list to map
        for (HTTPHostInfo hostInfo : hostInfoList) {
            // keep only the hostname, the port number will be used latter when configuring "maxPerRoute" in connection manager
            hostInfoMap.put(hostInfo.getServer(), hostInfo);
        }
    }

    /**
     * Constructs the configuration for HTTPDownload plugin with a single entry (used by USGS plugin)
     * @param info
     * @throws DMPluginException 
     */
      public HTTPDownloadConfiguration(HTTPHostInfo info ) throws DMPluginException {
          hostInfoMap.put(info.getServer(),info);
      }
    

    /**
     * Retrieves the info about the given hostname.
     *
     * @param hostname The name of the host.
     * @return The info about the host.
     */
    public HTTPHostInfo getHostInfo(String hostname) {
        return hostInfoMap.get(hostname);
    }


    /**
     * Retrieves the hostnames listed by the configuration.
     *
     * @return List of hostnames.
     */
    public List<HTTPHostInfo> getListOfHostInfos() {
        return new ArrayList<>(hostInfoMap.values());
    }


    /**
     * Helper to parse "http-accounts.xml" file and create list of HTTPHostInfo.
     */
    private static class HttpAccountsXmlReader extends DefaultHandler {

        // list to build
        private final List<HTTPHostInfo> hostInfoList = new ArrayList<>();

        // object to create from xml elements
        private HTTPHostInfo hostInfo;

        // events marker to detect xml elements
        private boolean server_element = false;
        private boolean username_element = false;
        private boolean password_element = false;
        private boolean maxConnections_element = false;
        private boolean protocol_element = false;
        private boolean port_element = false;


        /**
         * @return List of host info parsed from xml file.
         */
        List<HTTPHostInfo> getHostInfoList() {
            return hostInfoList;
        }


        /**
         * Starts the SAX parsing of given xml file.
         *
         * @param xmlFile The XML file to parse.
         * @throws SAXException
         * @throws IOException
         * @throws ParserConfigurationException
         */
        void parseXmlFile(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse(xmlFile, this);
        }


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("account")) {
                hostInfo = new HTTPHostInfo();
                hostInfoList.add(hostInfo);
            } else if (qName.equalsIgnoreCase("server")) {
                server_element = true;
            } else if (qName.equalsIgnoreCase("username")) {
                username_element = true;
            } else if (qName.equalsIgnoreCase("password")) {
                password_element = true;
            } else if (qName.equalsIgnoreCase("maxConnections")) {
                maxConnections_element = true;
            } else if (qName.equalsIgnoreCase("protocol")) {
                protocol_element = true;
            } else if (qName.equalsIgnoreCase("port")) {
                port_element = true;
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // nothing to do
        }


        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (server_element) {
                hostInfo.setServer(new String(ch, start, length));
                server_element = false;
            } else if (username_element) {
                hostInfo.setUsername(new String(ch, start, length));
                username_element = false;
            } else if (password_element) {
                hostInfo.setPassword(new String(ch, start, length));
                password_element = false;
            } else if (maxConnections_element) {
                hostInfo.setMaxConnections(Integer.parseInt(new String(ch, start, length)));
                maxConnections_element = false;
            } else if (protocol_element) {
                hostInfo.setProtocol(new String(ch, start, length));
                protocol_element = false;
            } else if (port_element) {
                hostInfo.setPort(new String(ch, start, length));
                port_element = false;
            }
        }

    }

}
