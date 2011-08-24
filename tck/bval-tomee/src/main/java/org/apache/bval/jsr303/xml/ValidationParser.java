package org.apache.bval.jsr303.xml;

import org.apache.bval.jsr303.ConfigurationImpl;
import org.apache.bval.jsr303.util.IOUtils;
import org.apache.bval.jsr303.util.SecureActions;
import org.apache.bval.util.PrivilegedActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.spi.ValidationProvider;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * fast override behavior cause of classloading issue.
 *
 * see https://issues.apache.org/jira/browse/BVAL-97
 *
 * @author rmannibucau
 */
public class ValidationParser {
    private static final String DEFAULT_VALIDATION_XML_FILE = "META-INF/validation.xml";
    private static final String VALIDATION_CONFIGURATION_XSD =
          "META-INF/validation-configuration-1.0.xsd";
    private static final Logger log = LoggerFactory.getLogger(ValidationParser.class);
    private final String validationXmlFile;

    /**
     * Create a new ValidationParser instance.
     * @param file
     */
    public ValidationParser(String file) {
        if(file == null) {
            validationXmlFile = DEFAULT_VALIDATION_XML_FILE;
        } else {
            validationXmlFile = file;
        }
    }

    /**
     * Process the validation configuration into <code>targetConfig</code>.
     * @param targetConfig
     */
    public void processValidationConfig(ConfigurationImpl targetConfig) {
        ValidationConfigType xmlConfig = parseXmlConfig();
        if (xmlConfig != null) {
            applyConfig(xmlConfig, targetConfig);
        }
    }

    private ValidationConfigType parseXmlConfig() {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream(validationXmlFile);
            if (inputStream == null) {
                log.debug("No {} found. Using annotation based configuration only.", validationXmlFile);
                return null;
            }

            log.debug("{} found.", validationXmlFile);

            Schema schema = getSchema();
            JAXBContext jc = JAXBContext.newInstance(ValidationConfigType.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setSchema(schema);
            StreamSource stream = new StreamSource(inputStream);
            JAXBElement<ValidationConfigType> root =
                  unmarshaller.unmarshal(stream, ValidationConfigType.class);
            return root.getValue();
        } catch (JAXBException e) {
            throw new ValidationException("Unable to parse " + validationXmlFile, e);
        } catch (IOException e) {
            throw new ValidationException("Unable to parse " + validationXmlFile, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private InputStream getInputStream(String path) throws IOException {
        ClassLoader loader = PrivilegedActions.getClassLoader(getClass());
        InputStream inputStream = loader.getResourceAsStream( path );

        if ( inputStream != null ) {
            // spec says: If more than one META-INF/validation.xml file
            // is found in the classpath, a ValidationException is raised.
            if ( path.equals("META-INF/validation.xml") ) {
                Enumeration<URL> urls = loader.getResources(path);
                Set<String> uniqueUrls = new HashSet<String>();
                while (urls.hasMoreElements()) {
                    uniqueUrls.add(urls.nextElement().toString());
                }
                if (uniqueUrls.size() > 1) {
                    throw new ValidationException("More than one " + path + " is found in the classpath"
                            + uniqueUrls);
                }
            }
        }

        return inputStream;
    }

    private Schema getSchema() {
        return getSchema(VALIDATION_CONFIGURATION_XSD);
    }

    /**
     * Get a Schema object from the specified resource name.
     * @param xsd
     * @return {@link Schema}
     */
    static Schema getSchema(String xsd) {
        ClassLoader loader = PrivilegedActions.getClassLoader(ValidationParser.class);
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL schemaUrl = loader.getResource(xsd);
        try {
            return sf.newSchema(schemaUrl);
        } catch (SAXException e) {
            log.warn("Unable to parse schema: " + xsd, e);
            return null;
        }
    }

    private void applyConfig(ValidationConfigType xmlConfig, ConfigurationImpl targetConfig) {
        applyProviderClass(xmlConfig, targetConfig);
        applyMessageInterpolator(xmlConfig, targetConfig);
        applyTraversableResolver(xmlConfig, targetConfig);
        applyConstraintFactory(xmlConfig, targetConfig);
        applyMappingStreams(xmlConfig, targetConfig);
        applyProperties(xmlConfig, targetConfig);
    }

    private void applyProperties(ValidationConfigType xmlConfig, ConfigurationImpl target) {
        for (PropertyType property : xmlConfig.getProperty()) {
            if (log.isDebugEnabled()) {
                log.debug("Found property '" + property.getName() + "' with value '" +
                      property.getValue() + "' in " + validationXmlFile);
            }
            target.addProperty(property.getName(), property.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void applyProviderClass(ValidationConfigType xmlConfig, ConfigurationImpl target) {
        String providerClassName = xmlConfig.getDefaultProvider();
        if (providerClassName != null) {
            Class<? extends ValidationProvider<?>> clazz =
                  (Class<? extends ValidationProvider<?>>) SecureActions
                        .loadClass(providerClassName, this.getClass());
            target.setProviderClass(clazz);
            log.info("Using {} as validation provider.", providerClassName);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyMessageInterpolator(ValidationConfigType xmlConfig,
                                          ConfigurationImpl target) {
        String messageInterpolatorClass = xmlConfig.getMessageInterpolator();
        if ( target.getMessageInterpolator() == null ) {
            if (messageInterpolatorClass != null) {
                Class<MessageInterpolator> clazz = (Class<MessageInterpolator>) SecureActions
                      .loadClass(messageInterpolatorClass, this.getClass());
                target.messageInterpolator(SecureActions.newInstance(clazz));
                log.info("Using {} as message interpolator.", messageInterpolatorClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void applyTraversableResolver(ValidationConfigType xmlConfig,
                                          ConfigurationImpl target) {
        String traversableResolverClass = xmlConfig.getTraversableResolver();
        if ( target.getTraversableResolver() == null ) {
            if (traversableResolverClass != null) {
                Class<TraversableResolver> clazz = (Class<TraversableResolver>) SecureActions
                      .loadClass(traversableResolverClass, this.getClass());
                target.traversableResolver(SecureActions.newInstance(clazz));
                log.info("Using {} as traversable resolver.", traversableResolverClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void applyConstraintFactory(ValidationConfigType xmlConfig,
                                        ConfigurationImpl target) {
        String constraintFactoryClass = xmlConfig.getConstraintValidatorFactory();
        if ( target.getConstraintValidatorFactory() == null ) {
            if (constraintFactoryClass != null) {
                Class<ConstraintValidatorFactory> clazz =
                      (Class<ConstraintValidatorFactory>) SecureActions
                            .loadClass(constraintFactoryClass, this.getClass());
                target.constraintValidatorFactory(SecureActions.newInstance(clazz));
                log.info("Using {} as constraint factory.", constraintFactoryClass);
            }
        }
    }

    private void applyMappingStreams(ValidationConfigType xmlConfig,
                                     ConfigurationImpl target) {
        for (JAXBElement<String> mappingFileNameElement : xmlConfig.getConstraintMapping()) {
            String mappingFileName = mappingFileNameElement.getValue();
            if ( mappingFileName.startsWith("/") ) {
                // Classloader needs a path without a starting /
                mappingFileName = mappingFileName.substring(1);
            }
            log.debug("Trying to open input stream for {}", mappingFileName);
            InputStream in = null;
            try {
                in = getInputStream(mappingFileName);
                if (in == null) {
                    throw new ValidationException(
                          "Unable to open input stream for mapping file " +
                                mappingFileName);
                }
            } catch (IOException e) {
                throw new ValidationException("Unable to open input stream for mapping file " +
                      mappingFileName, e);
            }
            target.addMapping(in);
        }
    }
}
