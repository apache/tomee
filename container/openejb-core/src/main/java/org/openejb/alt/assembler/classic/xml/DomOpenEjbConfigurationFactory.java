package org.openejb.alt.assembler.classic.xml;

import java.io.IOException;
import java.util.Properties;

import org.apache.xerces.parsers.DOMParser;
import org.openejb.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.OpenEjbConfiguration;
import org.openejb.alt.assembler.classic.OpenEjbConfigurationFactory;
import org.openejb.util.OpenEJBErrorHandler;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

public class DomOpenEjbConfigurationFactory implements OpenEjbConfigurationFactory{

    private SafeToolkit toolkit = SafeToolkit.getToolkit("DomConfig");
    private XmlOpenEJBConfiguration config;
    private String configXml;

    public void init(Properties props) throws OpenEJBException {
        SafeProperties safeProps = toolkit.getSafeProperties(props);
        configXml = safeProps.getProperty(EnvProps.CONFIGURATION);
 new java.io.File(configXml);
    }

    public OpenEjbConfiguration getOpenEjbConfiguration() throws OpenEJBException {
        try {
            if (config == null) {
                config = new XmlOpenEJBConfiguration();
                DOMParser parser = new DOMParser();

                parser.setErrorHandler(new XMLErrorHandler());
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);
                parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
                parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);
                parser.parse(configXml);

                Document document = parser.getDocument();
                config.initializeFromDOM(document);
            }

            return config;
        } catch (IOException e) { 
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        } catch (SAXNotSupportedException e) {
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        } catch (SAXNotRecognizedException e) {
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        } catch (SAXException e) {
            throw new OpenEJBException("Problem creating the OpenEjbConfiguration structure from file "+configXml, e);
        }
    }

}

class XMLErrorHandler implements ErrorHandler{

    public void warning (SAXParseException exception) throws SAXException{
        handleError("warning",exception);
    }

    public void error (SAXParseException exception) throws SAXException{
        handleError("error",exception);
    }

    public void fatalError (SAXParseException exception) throws SAXException{
        handleError("fatal error",exception);
    }

    private void handleError(String errorType, SAXParseException e) {
        OpenEJBErrorHandler.configurationParsingError(errorType, e.getLocalizedMessage(), e.getLineNumber()+"", e.getColumnNumber()+"");
    }
}

