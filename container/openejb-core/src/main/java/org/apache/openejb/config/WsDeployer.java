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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.jee.EnterpriseBean;
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
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;
import javax.jws.HandlerChain;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class WsDeployer implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, WsDeployer.class.getPackage().getName());

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        // process all webservice port
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            processPorts(ejbModule);
        }
        for (WebModule webModule : appModule.getWebModules()) {
            processPorts(webModule);
        }

        // Resolve service-refs
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                resolveServiceRefs(ejbModule, enterpriseBean);
            }
        }
        for (WebModule webModule : appModule.getWebModules()) {
            resolveServiceRefs(webModule, webModule.getWebApp());
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            resolveServiceRefs(clientModule, clientModule.getApplicationClient());
        }

        return appModule;
    }

    private void resolveServiceRefs(DeploymentModule module, JndiConsumer jndiConsumer) {
        for (ServiceRef serviceRef : jndiConsumer.getServiceRef()) {
            if (serviceRef.getServiceQname() == null && serviceRef.getWsdlFile() != null) {
                // parse the wsdl and get the serviceQname
                try {
                    String wsdlFile = serviceRef.getWsdlFile();
                    Definition definition = getWsdl(module, wsdlFile);

                    Set serviceQNames = definition.getServices().keySet();
                    if (serviceQNames.size() == 1) {
                        QName serviceQName = (QName) serviceQNames.iterator().next();
                        serviceRef.setServiceQname(serviceQName);
                    } else if (serviceQNames.isEmpty()) {
                        logger.error("The service-ref " + serviceRef.getName() + " must define service-qname because the wsdl-file " + serviceRef.getWsdlFile() + " does not constain any service definitions ");
                    } else {
                        logger.error("The service-ref " + serviceRef.getName() + " must define service-qname because the wsdl-file " + serviceRef.getWsdlFile() + " constain more then one service definitions " + serviceQNames);
                    }
                } catch(Exception e) {
                    logger.error("Unable to read wsdl file " + serviceRef.getWsdlFile());
                }
            }
        }
    }

    private void processPorts(WebModule webModule) throws OpenEJBException {
        // map existing webservice port declarations by servlet link
        Webservices webservices = webModule.getWebservices();
        Map<String, PortComponent> portMap = new TreeMap<String, PortComponent>();
        if (webservices != null) {
            for (WebserviceDescription webserviceDescription : webservices.getWebserviceDescription()) {
                for (PortComponent portComponent : webserviceDescription.getPortComponent()) {
                    ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                    if (serviceImplBean != null && serviceImplBean.getServletLink() != null) {
                        portMap.put(serviceImplBean.getServletLink(), portComponent);
                    }
                }
            }
        }


        // map existing servlet-mapping declarations
        WebApp webApp = webModule.getWebApp();
        Map<String, ServletMapping> servletMappings = new TreeMap<String, ServletMapping>();
        for (ServletMapping servletMapping : webApp.getServletMapping()) {
            servletMappings.put(servletMapping.getServletName(), servletMapping);
        }

        // add port declarations for webservices
        WebserviceDescription webserviceDescription;
        for (Servlet servlet : webApp.getServlet()) {
            String className = servlet.getServletClass();

            try {
                Class<?> clazz = webModule.getClassLoader().loadClass(className);
                if (JaxWsUtils.isWebService(clazz)) {
                    // add servlet mapping if not already declared
                    ServletMapping servletMapping = servletMappings.get(servlet.getServletName());
                    String serviceName = JaxWsUtils.getServiceName(clazz);
                    if (servletMapping == null) {
                        servletMapping = new ServletMapping();
                        servletMapping.setServletName(servlet.getServletName());

                        String location = "/" + serviceName;
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
                        ServiceImplBean serviceImplBean = new ServiceImplBean();
                        serviceImplBean.setServletLink(servlet.getServletName());
                        portComponent.setServiceImplBean(serviceImplBean);

                        webserviceDescription.getPortComponent().add(portComponent);
                    }

                    // default portId == moduleId.servletName
                    if (portComponent.getId() == null) {
                        portComponent.setId(webModule.getModuleId() + "." + servlet.getServletName());
                    }
                    if (webserviceDescription.getId() == null) {
                        webserviceDescription.setId(webModule.getModuleId() + "." + servlet.getServletName());
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
                        Definition definition = getWsdl(webModule, webserviceDescription.getWsdlFile());
                        if (definition != null && definition.getServices().size() ==  1) {
                            QName serviceQName = (QName) definition.getServices().keySet().iterator().next();
                            portComponent.setWsdlService(serviceQName);
                        } else {
                            portComponent.setWsdlService(JaxWsUtils.getServiceQName(clazz));
                        }
                    }
                    if (portComponent.getProtocolBinding() == null) {
                        portComponent.setProtocolBinding(JaxWsUtils.getBindingUriFromAnn(clazz));
                    }

                    // handlers
                    if (portComponent.getHandlerChains() == null) {
                        HandlerChains handlerChains = getHandlerChains(clazz, portComponent.getServiceEndpointInterface(), webModule.getClassLoader());
                        portComponent.setHandlerChains(handlerChains);

                    }
                }
            } catch (Exception e) {
                throw new OpenEJBException("Unable to load servlet class: " + className, e);
            }
        }
    }

    private void processPorts(EjbModule ejbModule) throws OpenEJBException {
        // map existing webservice port declarations by servlet link
        Webservices webservices = ejbModule.getWebservices();
        Map<String, PortComponent> portMap = new TreeMap<String, PortComponent>();
        if (webservices != null) {
            for (WebserviceDescription webserviceDescription : webservices.getWebserviceDescription()) {
                for (PortComponent portComponent : webserviceDescription.getPortComponent()) {
                    ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
                    if (serviceImplBean != null && serviceImplBean.getEjbLink() != null) {
                        portMap.put(serviceImplBean.getEjbLink(), portComponent);
                    }
                }
            }
        }

        Map<String, EjbDeployment> deploymentsByEjbName = ejbModule.getOpenejbJar().getDeploymentsByEjbName();

        WebserviceDescription webserviceDescription = null;
        for (EnterpriseBean enterpriseBean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            // skip if this is not a webservices endpoint
            if (!(enterpriseBean instanceof SessionBean)) continue;
            SessionBean sessionBean = (SessionBean) enterpriseBean;
            if (sessionBean.getSessionType() != SessionType.STATELESS) continue;
            if (sessionBean.getServiceEndpoint() == null) continue;


            EjbDeployment deployment = deploymentsByEjbName.get(sessionBean.getEjbName());
            if (deployment == null) continue;

            Class<?> ejbClass;
            try {
                ejbClass = ejbModule.getClassLoader().loadClass(sessionBean.getEjbClass());
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Unable to load ejb class: " + sessionBean.getEjbClass(), e);
            }

            // for now, skip all non jaxws beans
            if (!JaxWsUtils.isWebService(ejbClass)) continue;

            // create webservices dd if not defined
            if (webservices == null) {
                webservices = new Webservices();
                ejbModule.setWebservices(webservices);
            }
            if (webserviceDescription == null) {
                webserviceDescription = new WebserviceDescription();
                if (JaxWsUtils.isWebService(ejbClass)) {
                    webserviceDescription.setWebserviceDescriptionName(JaxWsUtils.getServiceName(ejbClass));
                } else {
                    // todo create webserviceDescription name using some sort of jaxrpc data 
                }
                webservices.getWebserviceDescription().add(webserviceDescription);
            }

            // add a port component if we don't alrady have one
            PortComponent portComponent = portMap.get(sessionBean.getEjbName());
            if (portComponent == null) {
                portComponent = new PortComponent();
                if (ejbClass.isAnnotationPresent(WebServiceProvider.class)) {
                    portComponent.setPortComponentName(ejbClass.getName());
                } else {
                    portComponent.setPortComponentName(ejbClass.getSimpleName());
                }
                webserviceDescription.getPortComponent().add(portComponent);

                ServiceImplBean serviceImplBean = new ServiceImplBean();
                serviceImplBean.setEjbLink(sessionBean.getEjbName());
                portComponent.setServiceImplBean(serviceImplBean);
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
                    Definition definition = getWsdl(ejbModule, webserviceDescription.getWsdlFile());
                    if (definition != null && definition.getServices().size() ==  1) {
                        QName serviceQName = (QName) definition.getServices().keySet().iterator().next();
                        portComponent.setWsdlService(serviceQName);
                    } else {
                        portComponent.setWsdlService(JaxWsUtils.getServiceQName(ejbClass));
                    }
                }
                if (portComponent.getLocation() == null && webserviceDescription.getWsdlFile() != null) {
                    // set location based on wsdl port
                    Definition definition = getWsdl(ejbModule, webserviceDescription.getWsdlFile());
                    String locationURI = getLocationFromWsdl(definition, portComponent);
                    portComponent.setLocation(locationURI);
                }
                if (portComponent.getProtocolBinding() == null) {
                    portComponent.setProtocolBinding(JaxWsUtils.getBindingUriFromAnn(ejbClass));
                }

                // handlers
                if (portComponent.getHandlerChains() == null) {
                    HandlerChains handlerChains = getHandlerChains(ejbClass, sessionBean.getServiceEndpoint(), ejbModule.getClassLoader());
                    portComponent.setHandlerChains(handlerChains);

                }
            } else {
                // todo location JAX-RPC services comes from wsdl file
            }
        }
    }

    private Definition getWsdl(DeploymentModule module, String wsdlFile) {
        Object object = module.getAltDDs().get(wsdlFile);
        if (object instanceof Definition) {
            Definition definition = (Definition) object;
            return definition;
        }

        try {
            URL wsdlUrl;
            if (object instanceof URL) {
                wsdlUrl = (URL) object;
            } else {
                URL baseUrl = getBaseUrl(module);
                wsdlUrl = new URL(baseUrl, wsdlFile);
            }

            Definition definition = ReadDescriptors.readWsdl(wsdlUrl);
            module.getAltDDs().put(wsdlFile, definition);
            return definition;
        } catch (Exception e) {
            logger.error("Unable to read wsdl file " + wsdlFile);
        }

        return null;
    }

    private URL getBaseUrl(DeploymentModule module) throws MalformedURLException {
        File file = new File(module.getJarLocation());
        if (!file.exists()) {
            return new URL(module.getJarLocation());
        }

        URL baseUrl = file.toURL();
        if (file.isFile()) {
            baseUrl = new URL("jar", null, baseUrl.toExternalForm() + "!/");
        }
        return baseUrl;
    }

    private String getLocationFromWsdl(Definition definition, PortComponent portComponent) {
        if (definition == null) return null;

        try {
            javax.wsdl.Service service = definition.getService(portComponent.getWsdlService());
            if (service == null) return null;

            Port port = service.getPort(portComponent.getWsdlPort().getLocalPart());
            if (port == null) return null;

            for (Object element : port.getExtensibilityElements()) {
                if (element instanceof SOAPAddress) {
                    SOAPAddress soapAddress = (SOAPAddress) element;
                    URI uri = new URI(soapAddress.getLocationURI());
                    return uri.getPath();
                } else if (element instanceof HTTPAddress) {
                    HTTPAddress httpAddress = (HTTPAddress) element;
                    URI uri = new URI(httpAddress.getLocationURI());
                    return uri.getPath();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static HandlerChains getHandlerChains(Class<?> declaringClass, String serviceEndpoint, ClassLoader classLoader) throws OpenEJBException {
        HandlerChain handlerChain = declaringClass.getAnnotation(HandlerChain.class);
        if (handlerChain == null && serviceEndpoint != null) {
            try {
                declaringClass = classLoader.loadClass(serviceEndpoint);
                handlerChain = declaringClass.getAnnotation(HandlerChain.class);
            } catch (ClassNotFoundException ignored) {
            }
        }
        HandlerChains handlerChains = null;
        if (handlerChain != null) {
            try {
                URL handlerFileURL = declaringClass.getResource(handlerChain.file());
                handlerChains = ReadDescriptors.readHandlerChains(handlerFileURL);
            } catch (Throwable e) {
                throw new OpenEJBException("Unable to load handler chain file: " + handlerChain.file(), e);
            }
        }
        return handlerChains;
    }
}
