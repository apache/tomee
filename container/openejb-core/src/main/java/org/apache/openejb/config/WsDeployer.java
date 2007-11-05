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
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.xml.namespace.QName;
import javax.wsdl.Definition;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

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
                resolveServiceRefs(enterpriseBean, ejbModule.getJarLocation());
            }
        }
        for (WebModule webModule : appModule.getWebModules()) {
            resolveServiceRefs(webModule.getWebApp(), webModule.getJarLocation());
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            resolveServiceRefs(clientModule.getApplicationClient(), clientModule.getJarLocation());
        }

        return appModule;
    }

    private void resolveServiceRefs(JndiConsumer jndiConsumer, String baseLocation) {
        URL baseURL;
        try {
            File file = new File(baseLocation);
            if (file.exists()) {
                baseURL = file.toURL();
                if (file.isFile()) {
                    baseURL = new URL("jar", null, baseURL.toExternalForm() + "!/");
                }
            } else {
                baseURL = new URL(baseLocation);
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid module location " + baseLocation);
            return;
        }

        Map<URL,Definition> wsdlFiles = new HashMap<URL,Definition>();
        for (ServiceRef serviceRef : jndiConsumer.getServiceRef()) {
            if (serviceRef.getServiceQname() == null && serviceRef.getWsdlFile() != null) {
                // parse the wsdl and get the serviceQname
                try {
                    String wsdlFile = serviceRef.getWsdlFile();
                    URL wsdlUrl = new URL(baseURL, wsdlFile);
                    Definition definition = wsdlFiles.get(wsdlUrl);
                    if (definition == null) {
                        definition = ReadDescriptors.readWsdl(wsdlUrl);
                        wsdlFiles.put(wsdlUrl, definition);
                    }

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
        WebserviceDescription webserviceDescription = null;
        for (Servlet servlet : webApp.getServlet()) {
            String className = servlet.getServletClass();

            try {
                Class<?> clazz = webModule.getClassLoader().loadClass(className);
                if (JaxWsUtils.isWebService(clazz)) {
                    // add servlet mapping if not already declared
                    ServletMapping servletMapping = servletMappings.get(servlet.getServletName());
                    if (servletMapping == null) {
                        servletMapping = new ServletMapping();
                        servletMapping.setServletName(servlet.getServletName());

                        String location = "/" + JaxWsUtils.getServiceName(clazz);
                        servletMapping.getUrlPattern().add(location);
                        webApp.getServletMapping().add(servletMapping);
                    }

                    // define port if not already declared
                    PortComponent portComponent = portMap.get(clazz.getName());
                    if (portComponent == null) {
                        // create port
                        portComponent = new PortComponent();
                        ServiceImplBean serviceImplBean = new ServiceImplBean();
                        serviceImplBean.setServletLink(className);
                        portComponent.setServiceImplBean(serviceImplBean);

                        // add port declaration
                        if (webservices == null) {
                            webservices = new Webservices();
                            webModule.setWebservices(webservices);
                        }
                        if (webserviceDescription == null) {
                            webserviceDescription = new WebserviceDescription();
                            webserviceDescription.setWebserviceDescriptionName(JaxWsUtils.getServiceName(clazz));
                            webservices.getWebserviceDescription().add(webserviceDescription);
                        }
                        webserviceDescription.getPortComponent().add(portComponent);
                    }

                    // default portId == moduleId.servletName
                    if (portComponent.getId() == null) {
                        portComponent.setId(webModule.getModuleId() + "." + servlet.getServletName());
                    }

                    // set port values from annotations if not already set
                    if (portComponent.getServiceEndpointInterface() == null) {
                        portComponent.setServiceEndpointInterface(JaxWsUtils.getServiceInterface(clazz));
                    }
                    if (portComponent.getPortComponentName() == null) {
                        portComponent.setPortComponentName(JaxWsUtils.getName(clazz));
                    }
                    if (portComponent.getWsdlPort() == null) {
                        portComponent.setWsdlPort(JaxWsUtils.getPortQName(clazz));
                    }
                    if (portComponent.getWsdlService() == null) {
                        portComponent.setWsdlService(JaxWsUtils.getServiceQName(clazz));
                    }
                    if (webserviceDescription.getWsdlFile() == null) {
                        webserviceDescription.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(clazz, webModule.getClassLoader()));
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

            Class<?> ejbClass = null;
            try {
                ejbClass = ejbModule.getClassLoader().loadClass(sessionBean.getEjbClass());
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Unable to load ejb class: " + sessionBean.getEjbClass(), e);
            }

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
                webserviceDescription.getPortComponent().add(portComponent);

                ServiceImplBean serviceImplBean = new ServiceImplBean();
                serviceImplBean.setEjbLink(sessionBean.getEjbName());
                portComponent.setServiceImplBean(serviceImplBean);
            }

            // default portId == deploymentId
            if (portComponent.getId() == null) {
                portComponent.setId(deployment.getDeploymentId());
            }

            // set service endpoint interface
            if (portComponent.getServiceEndpointInterface() == null) {
                portComponent.setServiceEndpointInterface(sessionBean.getServiceEndpoint());
            }

            // default location is /@WebService.serviceName/@WebService.name
            if (JaxWsUtils.isWebService(ejbClass)) {
                if (portComponent.getPortComponentName() == null) {
                    portComponent.setPortComponentName(JaxWsUtils.getName(ejbClass));
                }
                if (portComponent.getWsdlPort() == null) {
                    portComponent.setWsdlPort(JaxWsUtils.getPortQName(ejbClass));
                }
                if (portComponent.getWsdlService() == null) {
                    portComponent.setWsdlService(JaxWsUtils.getServiceQName(ejbClass));
                }
                if (webserviceDescription.getWsdlFile() == null) {
                    webserviceDescription.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(ejbClass, ejbModule.getClassLoader()));
                }
            } else {
                // todo location JAX-RPC services comes from wsdl file
            }
        }
    }
}
