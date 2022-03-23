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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.webservices.WsdlResolver;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.xml.sax.InputSource;

import jakarta.jws.HandlerChain;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import jakarta.xml.ws.soap.MTOM;
import jakarta.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class WsDeployer implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, WsDeployer.class.getPackage().getName());

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        // process all webservice port
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            processPorts(ejbModule);
        }
        for (final WebModule webModule : appModule.getWebModules()) {
            processPorts(webModule);
        }

        // Resolve service-refs
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            for (final EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                resolveServiceRefs(ejbModule, enterpriseBean);
            }
        }
        for (final WebModule webModule : appModule.getWebModules()) {
            resolveServiceRefs(webModule, webModule.getWebApp());
        }
        for (final ClientModule clientModule : appModule.getClientModules()) {
            resolveServiceRefs(clientModule, clientModule.getApplicationClient());
        }

        return appModule;
    }

    private void resolveServiceRefs(final DeploymentModule module, final JndiConsumer jndiConsumer) {
        for (final ServiceRef serviceRef : jndiConsumer.getServiceRef()) {
            if (serviceRef.getServiceQname() == null && serviceRef.getWsdlFile() != null) {
                // parse the wsdl and get the serviceQname
                try {
                    final String wsdlFile = serviceRef.getWsdlFile();
                    final Definition definition = getWsdl(module, wsdlFile);

                    final Set serviceQNames = definition.getServices().keySet();
                    if (serviceQNames.size() == 1) {
                        final QName serviceQName = (QName) serviceQNames.iterator().next();
                        serviceRef.setServiceQname(serviceQName);
                    } else if (serviceQNames.isEmpty()) {
                        logger.error("The service-ref " + serviceRef.getName() + " must define service-qname because the wsdl-file " + serviceRef.getWsdlFile() + " does not constain any service definitions ");
                    } else {
                        logger.error("The service-ref " + serviceRef.getName() + " must define service-qname because the wsdl-file " + serviceRef.getWsdlFile() + " constain more then one service definitions " + serviceQNames);
                    }
                } catch (final Exception e) {
                    logger.error("Unable to read wsdl file " + serviceRef.getWsdlFile());
                }
            }
        }
    }

    private void processPorts(final WebModule webModule) throws OpenEJBException {
        // map existing webservice port declarations by servlet link
        Webservices webservices = webModule.getWebservices();
        final Map<String, PortComponent> portMap = new TreeMap<>();
        if (webservices != null) {
            for (final WebserviceDescription webserviceDescription : webservices.getWebserviceDescription()) {
                for (final PortComponent portComponent : webserviceDescription.getPortComponent()) {
                    final ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                    if (serviceImplBean != null && serviceImplBean.getServletLink() != null) {
                        portMap.put(serviceImplBean.getServletLink(), portComponent);
                    }
                }
            }
        }


        // map existing servlet-mapping declarations
        final WebApp webApp = webModule.getWebApp();
        final Map<String, ServletMapping> servletMappings = new TreeMap<>();
        for (final ServletMapping servletMapping : webApp.getServletMapping()) {
            servletMappings.put(servletMapping.getServletName(), servletMapping);
        }

        // add port declarations for Pojo webservices
        WebserviceDescription webserviceDescription;
        for (final Servlet servlet : webApp.getServlet()) {
            // the implementation class will be replaced by the WsServlet in the WsRegistry
            final String className = servlet.getServletClass();

            // Skip JSPs
            if (className == null) {
                continue;
            }

            try {
                final Class<?> clazz = webModule.getClassLoader().loadClass(className);
                if (JaxWsUtils.isWebService(clazz)) {
                    // add servlet mapping if not already declared
                    ServletMapping servletMapping = servletMappings.get(servlet.getServletName());
                    final String serviceName = JaxWsUtils.getServiceName(clazz);
                    if (servletMapping == null) {
                        servletMapping = new ServletMapping();
                        servletMapping.setServletName(servlet.getServletName());

                        final String location = "/" + serviceName;
                        servletMapping.getUrlPattern().add(location);
                        webApp.getServletMapping().add(servletMapping);
                    }

                    // if we don't have a webservices document yet, we're gonna need one now
                    if (webservices == null) {
                        webservices = new Webservices();
                        webModule.setWebservices(webservices);
                    }

                    // add web service description element (maps to service)
                    webserviceDescription = webservices.getWebserviceDescriptionMap().get(serviceName);
                    if (webserviceDescription == null) {
                        webserviceDescription = new WebserviceDescription();
                        webserviceDescription.setWebserviceDescriptionName(serviceName);
                        webservices.getWebserviceDescription().add(webserviceDescription);
                    }

                    // define port if not already declared
                    PortComponent portComponent = portMap.get(servlet.getServletName());
                    if (portComponent == null) {
                        portComponent = new PortComponent();
                        portComponent.setPortComponentName(clazz.getSimpleName());
                        final ServiceImplBean serviceImplBean = new ServiceImplBean();
                        serviceImplBean.setServletLink(servlet.getServletName());
                        portComponent.setServiceImplBean(serviceImplBean);

                        webserviceDescription.getPortComponent().add(portComponent);
                    }

                    // default portId == host.moduleId.servletName
                    if (portComponent.getId() == null) {
                        portComponent.setId(webModule.getHost() + "." + webModule.getModuleId() + "." + servlet.getServletName());
                    }
                    if (webserviceDescription.getId() == null) {
                        webserviceDescription.setId(webModule.getHost() + "." + webModule.getModuleId() + "." + servlet.getServletName());
                    }

                    // set port values from annotations if not already set
                    if (portComponent.getServiceEndpointInterface() == null) {
                        portComponent.setServiceEndpointInterface(JaxWsUtils.getServiceInterface(clazz));
                    }
                    if (portComponent.getWsdlPort() == null) {
                        portComponent.setWsdlPort(JaxWsUtils.getPortQName(clazz));
                    }
                    if (webserviceDescription.getWsdlFile() == null) {
                        webserviceDescription.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(clazz, webModule.getClassLoader()));
                    }
                    if (portComponent.getWsdlService() == null) {
                        final Definition definition = getWsdl(webModule, webserviceDescription.getWsdlFile());
                        if (definition != null && definition.getServices().size() == 1) {
                            final QName serviceQName = (QName) definition.getServices().keySet().iterator().next();
                            portComponent.setWsdlService(serviceQName);
                        } else {
                            portComponent.setWsdlService(JaxWsUtils.getServiceQName(clazz));
                        }
                    }
                    if (portComponent.getProtocolBinding() == null) {
                        portComponent.setProtocolBinding(JaxWsUtils.getBindingUriFromAnn(clazz));
                    }
                    configMtomAnnotation(clazz, portComponent);
                    if (SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(portComponent.getProtocolBinding()) ||
                        SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(portComponent.getProtocolBinding())) {
                        portComponent.setEnableMtom(true);
                    }

                    // handlers
                    if (portComponent.getHandlerChains() == null) {
                        final HandlerChains handlerChains = getHandlerChains(clazz, portComponent.getServiceEndpointInterface(), webModule.getClassLoader());
                        portComponent.setHandlerChains(handlerChains);

                    }
                }
            } catch (final Exception e) {
                throw new OpenEJBException("Unable to load servlet class: " + className, e);
            }
        }
    }

    private void configMtomAnnotation(final Class<?> clazz, final PortComponent portComponent) {
        final MTOM mtom = clazz.getAnnotation(MTOM.class);
        if (mtom != null) {
            if (portComponent.getEnableMtom() == null) {
                portComponent.setEnableMtom(mtom.enabled());
            }
            if (portComponent.getMtomThreshold() == null) {
                portComponent.setMtomThreshold(mtom.threshold());
            }
        }
    }

    private void processPorts(final EjbModule ejbModule) throws OpenEJBException {
        // map existing webservice port declarations by servlet link
        Webservices webservices = ejbModule.getWebservices();
        final Map<String, PortComponent> portMap = new TreeMap<>();
        if (webservices != null) {
            for (final WebserviceDescription webserviceDescription : webservices.getWebserviceDescription()) {
                for (final PortComponent portComponent : webserviceDescription.getPortComponent()) {
                    final ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                    if (serviceImplBean != null && serviceImplBean.getEjbLink() != null) {
                        portMap.put(serviceImplBean.getEjbLink(), portComponent);
                    }
                }
            }
        }

        final Map<String, EjbDeployment> deploymentsByEjbName = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

        WebserviceDescription webserviceDescription;
        for (final EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            // skip if this is not a webservices endpoint
            if (!(enterpriseBean instanceof SessionBean)) {
                continue;
            }
            final SessionBean sessionBean = (SessionBean) enterpriseBean;
            if (sessionBean.getSessionType() == SessionType.STATEFUL) {
                continue;
            }
            if (sessionBean.getSessionType() == SessionType.MANAGED) {
                continue;
            }
            if (sessionBean.getServiceEndpoint() == null) {
                continue;
            }


            final EjbDeployment deployment = deploymentsByEjbName.get(sessionBean.getEjbName());
            if (deployment == null) {
                continue;
            }

            final Class<?> ejbClass;
            try {
                ejbClass = ejbModule.getClassLoader().loadClass(sessionBean.getEjbClass());
            } catch (final ClassNotFoundException e) {
                throw new OpenEJBException("Unable to load ejb class: " + sessionBean.getEjbClass(), e);
            }

            // for now, skip all non jaxws beans
            if (!JaxWsUtils.isWebService(ejbClass)) {
                continue;
            }

            // create webservices dd if not defined
            if (webservices == null) {
                webservices = new Webservices();
                ejbModule.setWebservices(webservices);
            }

            webserviceDescription = webservices.getWebserviceDescriptionMap().get(JaxWsUtils.getServiceName(ejbClass));
            if (webserviceDescription == null) {
                webserviceDescription = new WebserviceDescription();
                if (JaxWsUtils.isWebService(ejbClass)) {
                    webserviceDescription.setWebserviceDescriptionName(JaxWsUtils.getServiceName(ejbClass));
                }
                // TODO else { /* create webserviceDescription name using some sort of jaxrpc data */ }
                webservices.getWebserviceDescription().add(webserviceDescription);
            }

            // add a port component if we don't alrady have one
            PortComponent portComponent = portMap.get(sessionBean.getEjbName());
            if (portComponent == null) {
                portComponent = new PortComponent();
                if (webserviceDescription.getPortComponentMap().containsKey(JaxWsUtils.getPortQName(ejbClass).getLocalPart())) {
                    // when to webservices.xml is defined and when we want to
                    // publish more than one port for the same implementation by configuration
                    portComponent.setPortComponentName(sessionBean.getEjbName());

                } else { // JAX-WS Metadata specification default
                    portComponent.setPortComponentName(JaxWsUtils.getPortQName(ejbClass).getLocalPart());
                }
                webserviceDescription.getPortComponent().add(portComponent);

                final ServiceImplBean serviceImplBean = new ServiceImplBean();
                serviceImplBean.setEjbLink(sessionBean.getEjbName());
                portComponent.setServiceImplBean(serviceImplBean);

                // Checking if MTOM must be enabled
                if (portComponent.getProtocolBinding() == null) {
                    portComponent.setProtocolBinding(JaxWsUtils.getBindingUriFromAnn(ejbClass));
                }
                configMtomAnnotation(ejbClass, portComponent);
                if (SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(portComponent.getProtocolBinding()) ||
                    SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(portComponent.getProtocolBinding())) {
                    portComponent.setEnableMtom(true);
                }

            }

            // default portId == deploymentId
            if (portComponent.getId() == null) {
                portComponent.setId(deployment.getDeploymentId());
            }
            if (webserviceDescription.getId() == null) {
                webserviceDescription.setId(deployment.getDeploymentId());
            }

            // set service endpoint interface
            if (portComponent.getServiceEndpointInterface() == null) {
                portComponent.setServiceEndpointInterface(sessionBean.getServiceEndpoint());
            }

            // default location is /@WebService.serviceName/@WebService.name
            if (JaxWsUtils.isWebService(ejbClass)) {
                if (portComponent.getWsdlPort() == null) {
                    portComponent.setWsdlPort(JaxWsUtils.getPortQName(ejbClass));
                }
                if (webserviceDescription.getWsdlFile() == null) {
                    webserviceDescription.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(ejbClass, ejbModule.getClassLoader()));
                }
                if (portComponent.getWsdlService() == null) {
                    final Definition definition = getWsdl(ejbModule, webserviceDescription.getWsdlFile());
                    if (definition != null && definition.getServices().size() == 1) {
                        final QName serviceQName = (QName) definition.getServices().keySet().iterator().next();
                        portComponent.setWsdlService(serviceQName);
                    } else {
                        portComponent.setWsdlService(JaxWsUtils.getServiceQName(ejbClass));
                    }
                }
                if (portComponent.getLocation() == null && webserviceDescription.getWsdlFile() != null) {
                    // set location based on wsdl port
                    final Definition definition = getWsdl(ejbModule, webserviceDescription.getWsdlFile());
                    final String locationURI = getLocationFromWsdl(definition, portComponent);
                    portComponent.setLocation(locationURI);
                }
                if (portComponent.getProtocolBinding() == null) {
                    portComponent.setProtocolBinding(JaxWsUtils.getBindingUriFromAnn(ejbClass));
                }

                // handlers
                if (portComponent.getHandlerChains() == null) {
                    final HandlerChains handlerChains = getHandlerChains(ejbClass, sessionBean.getServiceEndpoint(), ejbModule.getClassLoader());
                    portComponent.setHandlerChains(handlerChains);

                }
            }
            // TODO else { /* location JAX-RPC services comes from wsdl file */ }
        }
    }

    private Definition getWsdl(final DeploymentModule module, final String wsdlFile) {
        if (wsdlFile == null) {
            return null;
        }

        final Object object = module.getAltDDs().get(wsdlFile);
        if (object instanceof Definition) {
            return (Definition) object;
        }

        try {
            final URL wsdlUrl;
            if (object instanceof URL) {
                wsdlUrl = (URL) object;
            } else {
                final URL baseUrl = getBaseUrl(module);
                wsdlUrl = new URL(baseUrl, wsdlFile);
            }

            final Definition definition = readWsdl(wsdlUrl);
            module.getAltDDs().put(wsdlFile, definition);
            return definition;
        } catch (final Exception e) {
            if (module.getClassLoader() != null) {
                final URL wsdlUrl = module.getClassLoader().getResource(wsdlFile.startsWith("classpath:") ? wsdlFile.substring("classpath:".length()) : wsdlFile);
                try {
                    final Definition definition = readWsdl(wsdlUrl);
                    module.getAltDDs().put(wsdlFile, definition);
                    return definition;
                } catch (final OpenEJBException e1) {
                    // no-op
                }
            }
            logger.error("Unable to read wsdl file " + wsdlFile);
        }

        return null;
    }

    // don't put it in ReadDescriptors to respect classloader dependencies (wsdl4j is optional)
    public static Definition readWsdl(final URL url) throws OpenEJBException {
        final Definition definition;
        try {
            final WSDLFactory factory = WSDLFactory.newInstance();
            final WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", true);
            reader.setFeature("javax.wsdl.importDocuments", true);
            final WsdlResolver wsdlResolver = new WsdlResolver(new URL(url, ".").toExternalForm(), new InputSource(IO.read(url)));
            definition = reader.readWSDL(wsdlResolver);
        } catch (final IOException e) {
            throw new OpenEJBException("Cannot read the wsdl file: " + url.toExternalForm(), e);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the wsdl file: " + url.toExternalForm(), e);
        }
        return definition;
    }

    private URL getBaseUrl(final DeploymentModule module) throws MalformedURLException {
        final File file = new File(module.getJarLocation());
        if (!file.exists()) {
            return new URL(module.getJarLocation());
        }

        URL baseUrl = file.toURI().toURL();
        if (file.isFile()) {
            baseUrl = new URL("jar", null, baseUrl.toExternalForm() + "!/");
        }
        return baseUrl;
    }

    private String getLocationFromWsdl(final Definition definition, final PortComponent portComponent) {
        if (definition == null) {
            return null;
        }

        try {
            final Service service = definition.getService(portComponent.getWsdlService());
            if (service == null) {
                return null;
            }

            final Port port = service.getPort(portComponent.getWsdlPort().getLocalPart());
            if (port == null) {
                return null;
            }

            for (final Object element : port.getExtensibilityElements()) {
                if (element instanceof SOAPAddress) {
                    final SOAPAddress soapAddress = (SOAPAddress) element;
                    final URI uri = URLs.uri(soapAddress.getLocationURI());
                    return uri.getPath();
                } else if (element instanceof HTTPAddress) {
                    final HTTPAddress httpAddress = (HTTPAddress) element;
                    final URI uri = URLs.uri(httpAddress.getLocationURI());
                    return uri.getPath();
                }
            }
        } catch (final Exception e) {
            // no-op
        }
        return null;
    }

    public static HandlerChains getHandlerChains(Class<?> declaringClass, final String serviceEndpoint, final ClassLoader classLoader) throws OpenEJBException {
        HandlerChain handlerChain = declaringClass.getAnnotation(HandlerChain.class);
        if (handlerChain == null && serviceEndpoint != null) {
            try {
                declaringClass = classLoader.loadClass(serviceEndpoint);
                handlerChain = declaringClass.getAnnotation(HandlerChain.class);
            } catch (final ClassNotFoundException ignored) {
                // no-op
            }
        }
        HandlerChains handlerChains = null;
        if (handlerChain != null) {
            try {
                final URL handlerFileURL = declaringClass.getResource(handlerChain.file());
                handlerChains = ReadDescriptors.readHandlerChains(handlerFileURL);
            } catch (final Throwable e) {
                throw new OpenEJBException("Unable to load handler chain file: " + handlerChain.file(), e);
            }
        }
        return handlerChains;
    }
}
