package be.spacebel.ese.downloadmanager.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {

    private static Logger log = Logger.getLogger(XMLUtils.class);

    public Document stringToDOM(String xmlSource, boolean isNamespaceAware) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(isNamespaceAware);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlSource)));
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    public void metalinkValidator(InputStream metalinkInput, String metalinkSchemaFile)
            throws IOException {
        try {
            File schemaFile = new File(metalinkSchemaFile);
            Source xmlFile = new StreamSource(metalinkInput);
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            log.debug("The Metalink IS valid.");
        } catch (SAXException e) {
            log.error("The Metalink file is NOT valid.");
            throw new IOException(e);
        }
    }
}
