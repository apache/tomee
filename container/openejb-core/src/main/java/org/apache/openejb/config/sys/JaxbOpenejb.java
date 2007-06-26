/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.sys;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ConfigUtils;
import org.apache.xbean.finder.ResourceFinder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class JaxbOpenejb {
    @SuppressWarnings({"unchecked"})
    public static <T> T create(Class<T> type) {
        if (type == null) throw new NullPointerException("type is null");

        if (type == ConnectionManager.class) {
            return (T) createConnectionManager();
        } else if (type == Connector.class) {
            return (T) createConnector();
        } else if (type == Container.class) {
            return (T) createContainer();
        } else if (type == Deployments.class) {
            return (T) createDeployments();
        } else if (type == JndiProvider.class) {
            return (T) createJndiProvider();
        } else if (type == Openejb.class) {
            return (T) createOpenejb();
        } else if (type == ProxyFactory.class) {
            return (T) createProxyFactory();
        } else if (type == Resource.class) {
            return (T) createResource();
        } else if (type == SecurityService.class) {
            return (T) createSecurityService();
        } else if (type == ServiceProvider.class) {
            return (T) createServiceProvider();
        } else if (type == ServicesJar.class) {
            return (T) createServicesJar();
        } else if (type == TransactionManager.class) {
            return (T) createTransactionManager();
        }
        throw new IllegalArgumentException("Unknown type " + type.getName());
    }

    public static ServicesJar readServicesJar(String providerName) throws OpenEJBException {
        InputStream in = null;
        URL url = null;
        try {
            ResourceFinder finder = new ResourceFinder("META-INF/", Thread.currentThread().getContextClassLoader());
            url = finder.find(providerName + "/service-jar.xml");
            in = url.openStream();

            ServicesJar servicesJar = unmarshal(ServicesJar.class, in);
            return servicesJar;
        } catch (MalformedURLException e) {
            throw new OpenEJBException("Unable to resolve service provider " + providerName, e);
        } catch (Exception e) {
            throw new OpenEJBException("Unable to read OpenEJB service-jar file for provider " + providerName + " at " + url, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static Openejb readConfig(String configFile) throws OpenEJBException {
        InputStream in = null;
        try {
            if (configFile.startsWith("jar:")) {
                URL url = new URL(configFile);
                in = url.openStream();
            } else if (configFile.startsWith("file:")) {
                URL url = new URL(configFile);
                in = url.openStream();
            } else {
                in = new FileInputStream(configFile);
            }
            Openejb openejb = unmarshal(Openejb.class, in);
            return openejb;
        } catch (MalformedURLException e) {
            throw new OpenEJBException("Unable to resolve location " + configFile, e);
        } catch (Exception e) {
            throw new OpenEJBException("Unable to read OpenEJB configuration file at " + configFile, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void writeConfig(String configFile, Openejb openejb) throws OpenEJBException {
        OutputStream out = null;
        try {
            File file = new File(configFile);
            out = new FileOutputStream(file);
            marshal(Openejb.class, openejb, out);
        } catch (IOException e) {
            throw new OpenEJBException(ConfigUtils.messages.format("conf.1040", configFile, e.getLocalizedMessage()), e);
        } catch (MarshalException e) {
            if (e.getCause() instanceof IOException) {
                throw new OpenEJBException(ConfigUtils.messages.format("conf.1040", configFile, e.getLocalizedMessage()), e);
            } else {
                throw new OpenEJBException(ConfigUtils.messages.format("conf.1050", configFile, e.getLocalizedMessage()), e);
            }
        } catch (ValidationException e) {
            /* TODO: Implement informative error handling here.
               The exception will say "X doesn't match the regular
               expression Y"
               This should be checked and more relevant information
               should be given -- not everyone understands regular
               expressions.
             */
            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */
            throw new OpenEJBException(ConfigUtils.messages.format("conf.1060", configFile, e.getLocalizedMessage()), e);
        } catch (JAXBException e) {
            throw new OpenEJBException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static final ThreadLocal<Set<String>> currentPublicId = new ThreadLocal<Set<String>>();

    private static Map<Class, JAXBContext> jaxbContexts = new HashMap<Class, JAXBContext>();

    public static <T> String marshal(Class<T> type, Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T> void marshal(Class<T> type, Object object, OutputStream out) throws JAXBException {
        JAXBContext jaxbContext = getContext(type);
        Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    private static <T> JAXBContext getContext(Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(type);
            jaxbContexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T unmarshal(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });


        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return (T) unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    public static class NamespaceFilter extends XMLFilterImpl {

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return super.resolveEntity(publicId, systemId);
        }

        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://www.openejb.org/System/Configuration", localName, qname, atts);
        }
    }


    public static ConnectionManager createConnectionManager() {
        return new ConnectionManager();
    }

    public static Connector createConnector() {
        return new Connector();
    }

    public static Container createContainer() {
        return new Container();
    }

    public static Deployments createDeployments() {
        return new Deployments();
    }

    public static JndiProvider createJndiProvider() {
        return new JndiProvider();
    }

    public static Openejb createOpenejb() {
        return new Openejb();
    }

    public static ProxyFactory createProxyFactory() {
        return new ProxyFactory();
    }

    public static Resource createResource() {
        return new Resource();
    }

    public static SecurityService createSecurityService() {
        return new SecurityService();
    }

    public static ServiceProvider createServiceProvider() {
        return new ServiceProvider();
    }

    public static ServicesJar createServicesJar() {
        return new ServicesJar();
    }

    public static TransactionManager createTransactionManager() {
        return new TransactionManager();
    }
}
