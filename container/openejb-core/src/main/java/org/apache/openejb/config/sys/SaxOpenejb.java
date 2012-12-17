/*
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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Saxs;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain Java code for parsing a tomee.xml or openejb.xml file
 * as JAXB loading is so slow.
 *
 * @version $Rev$ $Date$
 */
class SaxOpenejb extends StackHandler {

    public static final String HOME_VAR = "$home";
    private final Openejb openejb = new Openejb();

    public static Openejb parse(final InputSource source) throws SAXException, ParserConfigurationException, IOException {
        SAXParser parser = Saxs.factory().newSAXParser();
        final SaxOpenejb sax = new SaxOpenejb();
        parser.parse(source, sax);
        return sax.openejb;
    }

    @Override
    public void startDocument() throws SAXException {
        push(new Document());
    }

    private class Root extends DefaultHandler {

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals("Container")) push(new ContainerElement());
            else if (localName.equals("JndiProvider")) push(new JndiProviderElement());
            else if (localName.equals("SecurityService")) push(new SecurityServiceElement());
            else if (localName.equals("TransactionManager")) push(new TransactionManagerElement());
            else if (localName.equals("ConnectionManager")) push(new ConnectionManagerElement());
            else if (localName.equals("ProxyFactory")) push(new ProxyFactoryElement());
            else if (localName.equals("Resource")) push(new ResourceElement(openejb.getResource()));
            else if (localName.equals("Connector")) push(new ResourceElement(openejb.getResource()));
            else if (localName.equals("Deployments")) push(new DeploymentsElement());
            else if (localName.equals("Import")) push(new ImportElement());
            else if (localName.equals("Service")) push(new DeclaredServiceElement(openejb.getServices()));
            else throw new IllegalStateException("Unsupported Element: " + localName);
            get().startElement(uri, localName, qName, attributes);
        }
    }


    private class Document extends DefaultHandler {

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals("openejb")) push(new Root());
            else if (localName.equals("tomee")) push(new Root());
            else throw new SAXException("Unsupported Element: " + localName);
            checkAttributes(attributes);
        }
    }

    private class DeploymentsElement extends DefaultHandler {

        private final Deployments deployments = new Deployments();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            deployments.setDir(attributes.getValue("dir"));
            deployments.setFile(attributes.getValue("jar"));
            deployments.setFile(attributes.getValue("file"));
            deployments.setAutoDeploy("true".equals(attributes.getValue("autoDeploy")));
            checkAttributes(attributes, "dir", "jar", "file", "autoDeploy");
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            openejb.getDeployments().add(deployments);
        }
    }

    public class ContainerElement extends ServiceElement<Container> {

        public ContainerElement() {
            super(new Container());
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            final String ctype = attributes.getValue("ctype");
            if (ctype != null) service.setType(ctype);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            openejb.getContainer().add(service);
            super.endElement(uri, localName, qName);
        }

        @Override
        protected List<String> getAttributes() {
            final List<String> attributes = new ArrayList<String>(super.getAttributes());
            attributes.add("ctype");
            return attributes;
        }
    }

    public class SecurityServiceElement extends ServiceElement<SecurityService> {

        public SecurityServiceElement() {
            super(new SecurityService());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            openejb.setSecurityService(service);
            super.endElement(uri, localName, qName);
        }
    }

    public class ConnectionManagerElement extends ServiceElement<ConnectionManager> {

        public ConnectionManagerElement() {
            super(new ConnectionManager());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            openejb.setConnectionManager(service);
            super.endElement(uri, localName, qName);
        }
    }

    public class ProxyFactoryElement extends ServiceElement<ProxyFactory> {

        public ProxyFactoryElement() {
            super(new ProxyFactory());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            openejb.setProxyFactory(service);
            super.endElement(uri, localName, qName);
        }
    }

    public class TransactionManagerElement extends ServiceElement<TransactionManager> {

        public TransactionManagerElement() {
            super(new TransactionManager());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            openejb.setTransactionManager(service);
            super.endElement(uri, localName, qName);
        }
    }

    public class JndiProviderElement extends ServiceElement<JndiProvider> {

        public JndiProviderElement() {
            super(new JndiProvider());
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            openejb.getJndiProvider().add(service);
            super.endElement(uri, localName, qName);
        }
    }

    private class ImportElement extends DefaultHandler {
        private String path = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            path = attributes.getValue("path");
            checkAttributes(attributes, "path");
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (path != null) {
                updatePath();

                InputStream is;
                try {
                    final URL url = new URL(path);
                    is = url.openStream();
                } catch (MalformedURLException e) {
                    final File file = new File(path);
                    try {
                        is = new FileInputStream(file);
                    } catch (FileNotFoundException e1) {
                        throw new SAXException("specified path '" + path + "' is neither an url nor a file path", e);
                    }
                } catch (IOException e) {
                    throw new SAXException(e);
                }

                try {
                    final Openejb importedOpenEJB = parse(new InputSource(is));
                    merge(openejb, importedOpenEJB);
                } catch (ParserConfigurationException e) {
                    throw new SAXException(e);
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            }
        }

        private void updatePath() {
            if (path != null && path.startsWith(HOME_VAR)) {
                path = SystemInstance.get().getHome().getDirectory().getAbsolutePath() + path.substring(HOME_VAR.length());
            }
        }

        private void merge(final Openejb openejb, final Openejb importedOpenEJB) {
            if (importedOpenEJB.container != null) {
                for (Container container : importedOpenEJB.container) {
                    openejb.add(container);
                }
            }
            if (importedOpenEJB.jndiProvider != null) {
                for (JndiProvider jndiProvider : importedOpenEJB.jndiProvider) {
                    openejb.add(jndiProvider);
                }
            }
            if (importedOpenEJB.securityService != null
                    || importedOpenEJB.transactionManager != null
                    || importedOpenEJB.connectionManager != null
                    || importedOpenEJB.proxyFactory != null) {
                // do nothing, these are unique so i don't think importing it is a good idea
            }
            if (importedOpenEJB.connector != null) {
                for (Connector connector : importedOpenEJB.connector) {
                    openejb.add(connector);
                }
            }
            if (importedOpenEJB.resource != null) {
                for (Resource resource : importedOpenEJB.resource) {
                    openejb.add(resource);
                }
            }
            if (importedOpenEJB.deployments != null) {
                for (Deployments deployment : importedOpenEJB.deployments) {
                    openejb.add(deployment);
                }
            }
        }
    }
}
