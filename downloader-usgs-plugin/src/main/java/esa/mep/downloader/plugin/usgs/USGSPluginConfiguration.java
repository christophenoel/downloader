/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esa.mep.downloader.plugin.usgs;

import be.spacebel.ese.downloadmanager.plugin.http.HTTPDownloadConfiguration;
import be.spacebel.ese.downloadmanager.plugin.http.HTTPHostInfo;
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
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author cnl
 */
public class USGSPluginConfiguration {

    private static final Logger LOG = LogManager.getLogger(
            HTTPDownloadConfiguration.class);

    // consts
    private static final String HTTP_ACCOUNTS_FILE = "usgs-accounts.xml";

    // Configuration parameters
    UsgsHostInfo hostInfo;

    /**
     * Constructs the configuration for HTTPDownload plugin. It reads the
     * "http-accounts.xml" file in given directory. If the file cannot be found
     * or parsed, then an exception is thrown.
     *
     * @param pluginCfgRootDir The directory that contains "http-accounts.xml".
     * @throws DMPluginException in case the file is not found or is malformed.
     */
    public USGSPluginConfiguration(File pluginCfgRootDir) throws DMPluginException {
        // retrieve http-accounts.xml file
        String usgsAccountsFilePath = FilenameUtils.concat(
                pluginCfgRootDir.getPath(), HTTP_ACCOUNTS_FILE);
        File usgsAccountsFile = new File(usgsAccountsFilePath);
        if (usgsAccountsFile.isFile() == false) {
            throw new DMPluginException("File http-accounts.xml not found.",
                    new FileNotFoundException(usgsAccountsFilePath));
        }
        LOG.debug("File usgs-accounts.xml found: " + usgsAccountsFilePath);

        // parse config file 
        try {
            UsgsAccountsXmlReader xmlReader = new UsgsAccountsXmlReader();
            xmlReader.parseXmlFile(usgsAccountsFile);
            hostInfo = xmlReader.getHostInfo();
            LOG.debug("USGS Plugin host "+hostInfo.getServer());
            LOG.debug("USGS Plugin user "+hostInfo.getUsername());
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            // impossible to continue without configuration
            String message = "Failed to read usgs-accounts.xml.";
            LOG.error(message, ex);
            throw new DMPluginException(message, ex);
        }

    }



    /**
     * Helper to parse "http-accounts.xml" file and create list of HTTPHostInfo.
     */
    private static class UsgsAccountsXmlReader extends DefaultHandler {

        // object to create from xml elements
        private UsgsHostInfo hostInfo;
private static final org.slf4j.Logger log = LoggerFactory.getLogger(UsgsAccountsXmlReader.class);
        // events marker to detect xml elements
        private boolean server_element = false;
        private boolean username_element = false;
        private boolean password_element = false;
        private boolean maxConnections_element = false;

        /**
         * @return List of host info parsed from xml file.
         */
        UsgsHostInfo getHostInfo() {
            return hostInfo;
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
            log.debug("parsing usgs account xml");
            hostInfo = new UsgsHostInfo();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            
            sp.parse(xmlFile, this);
            
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {

            

            if (qName.equalsIgnoreCase("server")) {
                log.debug("Found server element");
                server_element = true;
            } else if (qName.equalsIgnoreCase("username")) {
                username_element = true;
            } else if (qName.equalsIgnoreCase("password")) {
                password_element = true;
            } else if (qName.equalsIgnoreCase("maxConnections")) {
                maxConnections_element = true;
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
                log.debug("Setting server with value "+hostInfo.getServer());
                server_element = false;
            } else if (username_element) {
                hostInfo.setUsername(new String(ch, start, length));
                username_element = false;
            } else if (password_element) {
                hostInfo.setPassword(new String(ch, start, length));
                password_element = false;
            } else if (maxConnections_element) {
                hostInfo.setMaxConnections(Integer.parseInt(
                        new String(ch, start, length)));
                maxConnections_element = false;

            }
        }

    }

}
