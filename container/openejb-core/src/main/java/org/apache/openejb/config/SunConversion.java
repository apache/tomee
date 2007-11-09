/**
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

import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.jpa.AttributeOverride;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Field;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.JoinColumn;
import org.apache.openejb.jee.jpa.JoinTable;
import org.apache.openejb.jee.jpa.ManyToOne;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.PrimaryKeyJoinColumn;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.SecondaryTable;
import org.apache.openejb.jee.jpa.Table;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.EjbLink;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.jee.sun.Cmp;
import org.apache.openejb.jee.sun.CmpFieldMapping;
import org.apache.openejb.jee.sun.CmrFieldMapping;
import org.apache.openejb.jee.sun.ColumnName;
import org.apache.openejb.jee.sun.ColumnPair;
import org.apache.openejb.jee.sun.Ejb;
import org.apache.openejb.jee.sun.EjbRef;
import org.apache.openejb.jee.sun.EntityMapping;
import org.apache.openejb.jee.sun.Finder;
import org.apache.openejb.jee.sun.JaxbSun;
import org.apache.openejb.jee.sun.MessageDestinationRef;
import org.apache.openejb.jee.sun.OneOneFinders;
import org.apache.openejb.jee.sun.PortInfo;
import org.apache.openejb.jee.sun.ResourceEnvRef;
import org.apache.openejb.jee.sun.ResourceRef;
import org.apache.openejb.jee.sun.StubProperty;
import org.apache.openejb.jee.sun.SunApplication;
import org.apache.openejb.jee.sun.SunApplicationClient;
import org.apache.openejb.jee.sun.SunCmpMapping;
import org.apache.openejb.jee.sun.SunCmpMappings;
import org.apache.openejb.jee.sun.SunEjbJar;
import org.apache.openejb.jee.sun.SunWebApp;
import org.apache.openejb.jee.sun.Web;
import org.apache.openejb.jee.sun.WebserviceEndpoint;
import org.apache.openejb.jee.sun.WsdlPort;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

//
// Note to developer:  the best doc on what the sun-cmp-mappings element mean can be foudn here
//   http://java.sun.com/j2ee/1.4/docs/devguide/dgcmp.html
//   https://glassfish.dev.java.net/javaee5/docs/DG/beajj.html
//

public class SunConversion implements DynamicDeployer {
    public AppModule deploy(AppModule appModule) {
        SunApplication sunApplication = getSunApplication(appModule);
        if (sunApplication != null) {
            for (Web web : sunApplication.getWeb()) {
                String webUri = web.getWebUri();
                for (WebModule webModule : appModule.getWebModules()) {
                    if (webUri.equals(webModule.getModuleId()))  {
                        webModule.setContextRoot(web.getContextRoot());
                        break;
                    }
                }
            }
        }

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            convertModule(ejbModule, appModule.getCmpMappings());
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            convertModule(clientModule);
        }
        for (WebModule webModule : appModule.getWebModules()) {
            convertModule(webModule);
        }
        return appModule;
    }

    private SunApplication getSunApplication(AppModule appModule) {
        Object altDD = appModule.getAltDDs().get("sun-application.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbSun.unmarshal(SunApplication.class, new ByteArrayInputStream(((String)altDD).getBytes()));
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbSun.unmarshal(SunApplication.class, ((URL)altDD).openStream());
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof SunApplication) {
            return (SunApplication) altDD;
        }
        return null;
    }

    private SunApplicationClient getSunApplicationClient(ClientModule clientModule) {
        Object altDD = clientModule.getAltDDs().get("sun-application-client.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbSun.unmarshal(SunApplicationClient.class, new ByteArrayInputStream(((String)altDD).getBytes()));
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbSun.unmarshal(SunApplicationClient.class, ((URL)altDD).openStream());
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof SunApplicationClient) {
            return (SunApplicationClient) altDD;
        }
        return null;
    }

    private SunWebApp getSunWebApp(WebModule webModule) {
        Object altDD = webModule.getAltDDs().get("sun-web.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbSun.unmarshal(SunWebApp.class, new ByteArrayInputStream(((String)altDD).getBytes()));
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbSun.unmarshal(SunWebApp.class, ((URL)altDD).openStream());
            } catch (Exception e) {
                e.printStackTrace();
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof SunWebApp) {
            return (SunWebApp) altDD;
        }
        return null;
    }

    private SunEjbJar getSunEjbJar(EjbModule ejbModule) {
        Object altDD = ejbModule.getAltDDs().get("sun-ejb-jar.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbSun.unmarshal(SunCmpMappings.class, new ByteArrayInputStream(((String)altDD).getBytes()));
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbSun.unmarshal(SunEjbJar.class, ((URL)altDD).openStream());
            } catch (Exception e) {
                e.printStackTrace();
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof SunEjbJar) {
            return (SunEjbJar) altDD;
        }
        return null;
    }

    private SunCmpMappings getSunCmpMappings(EjbModule ejbModule) {
        Object altDD = ejbModule.getAltDDs().get("sun-cmp-mappings.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbSun.unmarshal(SunCmpMappings.class, new ByteArrayInputStream(((String)altDD).getBytes()));
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbSun.unmarshal(SunCmpMappings.class, ((URL)altDD).openStream());
            } catch (Exception e) {
                e.printStackTrace();
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof SunCmpMappings) {
            return (SunCmpMappings) altDD;
        }
        return null;
    }

    public void convertModule(ClientModule clientModule) {
        if (clientModule == null) {
            return;
        }

        ApplicationClient applicationClient = clientModule.getApplicationClient();
        if (applicationClient == null) {
            return;
        }
        SunApplicationClient sunApplicationClient = getSunApplicationClient(clientModule);
        if (sunApplicationClient == null) {
            return;
        }

        // map ejb-refs
        Map<String,org.apache.openejb.jee.EjbRef> refMap = applicationClient.getEjbRefMap();

        // map ejb-ref jndi name declaration to deploymentId
        for (EjbRef ref : sunApplicationClient.getEjbRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getEjbRefName();
                org.apache.openejb.jee.EjbRef ejbRef = refMap.get(refName);
                if (ejbRef == null) {
                    ejbRef = new org.apache.openejb.jee.EjbRef();
                    ejbRef.setEjbRefName(refName);
                    refMap.put(refName, ejbRef);
                    applicationClient.getEjbRef().add(ejbRef);
                }
                ejbRef.setMappedName(ref.getJndiName());
            }
        }

        // map resource-env-refs and message-destination-refs
        Map<String,JndiReference> resEnvMap = new TreeMap<String,JndiReference>();
        resEnvMap.putAll(applicationClient.getResourceEnvRefMap());
        resEnvMap.putAll(applicationClient.getMessageDestinationRefMap());

        for (ResourceRef ref : sunApplicationClient.getResourceRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getResRefName();
                JndiReference resEnvRef = resEnvMap.get(refName);
                if (resEnvRef != null) {
                    resEnvRef.setMappedName(ref.getJndiName());
                }
            }
        }
        for (ResourceEnvRef ref : sunApplicationClient.getResourceEnvRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getResourceEnvRefName();
                JndiReference resEnvRef = resEnvMap.get(refName);
                if (resEnvRef != null) {
                    resEnvRef.setMappedName(ref.getJndiName());
                }
            }
        }
        for (MessageDestinationRef ref : sunApplicationClient.getMessageDestinationRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getMessageDestinationRefName();
                JndiReference resEnvRef = resEnvMap.get(refName);
                if (resEnvRef != null) {
                    resEnvRef.setMappedName(ref.getJndiName());
                }
            }
        }

        Map<String, ServiceRef> serviceRefMap = applicationClient.getServiceRefMap();
        for (org.apache.openejb.jee.sun.ServiceRef ref : sunApplicationClient.getServiceRef()) {
            String refName = ref.getServiceRefName();
            ServiceRef serviceRef = serviceRefMap.get(refName);
            if (serviceRef != null) {
                Map<String,PortComponentRef> ports = new TreeMap<String,PortComponentRef>();
                for (PortComponentRef portComponentRef : serviceRef.getPortComponentRef()) {
                    ports.put(portComponentRef.getServiceEndpointInterface(), portComponentRef);
                }

                for (PortInfo portInfo : ref.getPortInfo()) {
                    PortComponentRef portComponentRef = ports.get(portInfo.getServiceEndpointInterface());
                    if (portComponentRef != null) {
                        WsdlPort wsdlPort = portInfo.getWsdlPort();
                        if (wsdlPort != null) {
                            QName qname = new QName(wsdlPort.getNamespaceURI(), wsdlPort.getLocalpart());
                            portComponentRef.setQName(qname);
                        }
                        for (StubProperty stubProperty : portInfo.getStubProperty()) {
                            String name = stubProperty.getName();
                            String value = stubProperty.getValue();
                            portComponentRef.getProperties().setProperty(name, value);
                        }
                    }
                }

                String wsdlOverride = ref.getWsdlOverride();
                if (wsdlOverride != null && wsdlOverride.length() > 0) {
                    String serviceId = extractServiceId(wsdlOverride);
                    serviceRef.setMappedName(serviceId);
                }
            }
        }
    }

    public void convertModule(WebModule webModule) {
        if (webModule == null) {
            return;
        }

        WebApp webApp = webModule.getWebApp();
        if (webApp == null) {
            return;
        }
        SunWebApp sunWebApp = getSunWebApp(webModule);
        if (sunWebApp == null) {
            return;
        }

        if (sunWebApp.getContextRoot() != null) {
            webModule.setContextRoot(sunWebApp.getContextRoot());
        }

        // map ejb-refs
        Map<String,org.apache.openejb.jee.JndiReference> refMap = new TreeMap<String,org.apache.openejb.jee.JndiReference>();
        refMap.putAll(webApp.getEjbRefMap());
        refMap.putAll(webApp.getEjbLocalRefMap());

        // map ejb-ref jndi name declaration to deploymentId
        for (EjbRef ref : sunWebApp.getEjbRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getEjbRefName();
                org.apache.openejb.jee.JndiReference ejbRef = refMap.get(refName);
                if (ejbRef == null) {
                    ejbRef = new org.apache.openejb.jee.EjbRef();
                    ejbRef.setName(refName);
                    refMap.put(refName, ejbRef);
                    webApp.getEjbRef().add((org.apache.openejb.jee.EjbRef) ejbRef);
                }
                ejbRef.setMappedName(ref.getJndiName());
            }
        }

        // map resource-env-refs and message-destination-refs
        Map<String,JndiReference> resEnvMap = new TreeMap<String,JndiReference>();
        resEnvMap.putAll(webApp.getResourceRefMap());
        resEnvMap.putAll(webApp.getResourceEnvRefMap());
        resEnvMap.putAll(webApp.getMessageDestinationRefMap());

        for (ResourceRef ref : sunWebApp.getResourceRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getResRefName();
                JndiReference resEnvRef = resEnvMap.get(refName);
                if (resEnvRef != null) {
                    resEnvRef.setMappedName(ref.getJndiName());
                }
            }
        }
        for (ResourceEnvRef ref : sunWebApp.getResourceEnvRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getResourceEnvRefName();
                JndiReference resEnvRef = resEnvMap.get(refName);
                if (resEnvRef != null) {
                    resEnvRef.setMappedName(ref.getJndiName());
                }
            }
        }
        for (MessageDestinationRef ref : sunWebApp.getMessageDestinationRef()) {
            if (ref.getJndiName() != null) {
                String refName = ref.getMessageDestinationRefName();
                JndiReference resEnvRef = resEnvMap.get(refName);
                if (resEnvRef != null) {
                    resEnvRef.setMappedName(ref.getJndiName());
                }
            }
        }

        Map<String, ServiceRef> serviceRefMap = webApp.getServiceRefMap();
        for (org.apache.openejb.jee.sun.ServiceRef ref : sunWebApp.getServiceRef()) {
            String refName = ref.getServiceRefName();
            ServiceRef serviceRef = serviceRefMap.get(refName);
            if (serviceRef != null) {
                Map<String,PortComponentRef> ports = new TreeMap<String,PortComponentRef>();
                for (PortComponentRef portComponentRef : serviceRef.getPortComponentRef()) {
                    ports.put(portComponentRef.getServiceEndpointInterface(), portComponentRef);
                }

                for (PortInfo portInfo : ref.getPortInfo()) {
                    PortComponentRef portComponentRef = ports.get(portInfo.getServiceEndpointInterface());
                    if (portComponentRef != null) {
                        WsdlPort wsdlPort = portInfo.getWsdlPort();
                        if (wsdlPort != null) {
                            QName qname = new QName(wsdlPort.getNamespaceURI(), wsdlPort.getLocalpart());
                            portComponentRef.setQName(qname);
                        }
                        for (StubProperty stubProperty : portInfo.getStubProperty()) {
                            String name = stubProperty.getName();
                            String value = stubProperty.getValue();
                            portComponentRef.getProperties().setProperty(name, value);
                        }
                    }
                }

                String wsdlOverride = ref.getWsdlOverride();
                if (wsdlOverride != null && wsdlOverride.length() > 0) {
                    String serviceId = extractServiceId(wsdlOverride);
                    serviceRef.setMappedName(serviceId);
                }
            }
        }

        // map wsdl locations
        if (webModule.getWebservices() != null) {
            Map<String, WebserviceDescription> descriptions = webModule.getWebservices().getWebserviceDescriptionMap();
            for (org.apache.openejb.jee.sun.WebserviceDescription sunDescription : sunWebApp.getWebserviceDescription()) {
                WebserviceDescription description = descriptions.get(sunDescription.getWebserviceDescriptionName());
                if (description == null) continue;

                String serviceId = extractSerivceId(sunDescription.getWsdlPublishLocation(), description.getWsdlFile());
                if (serviceId != null) {
                    description.setId(serviceId);
                }
            }
        }
    }


    public static String extractServiceId(String location) {
        return extractSerivceId(location, null);
    }

    public static String extractSerivceId(String location, String wsdlFile) {
        if (location == null) return null;

        if (location.startsWith("file:")) {
            // location format = file:{repository}/{location}.wsdl
            location = location.replaceFirst("file:[^/]*/", "");

            // append wsdl name without leading META-INF/wsdl or WEB-INF/wsdl or ending .wsdl
            if (wsdlFile != null) {
                wsdlFile = wsdlFile.replaceFirst("META-INF/wsdl/", "");
                wsdlFile = wsdlFile.replaceFirst("WEB-INF/wsdl/", "");
                location = location + "/" + wsdlFile;
            }
            location = location.replaceFirst("\\.wsdl$", "");
        } else if (location.startsWith("http:") || location.startsWith("https:")) {
            // location format = https://{server}:{port}/{location}?WSDL
            location = location.replaceFirst("http[s]?://[^/]*/", "");
            location = location.replaceFirst("\\?.*$", "");
        }

        if (location.length() == 0) location = null;
        return location;
    }

    public void convertModule(EjbModule ejbModule, EntityMappings entityMappings) {
        Map<String, EntityData> entities =  new TreeMap<String, EntityData>();
        for (Entity entity : entityMappings.getEntity()) {
            entities.put(entity.getDescription(), new SunConversion.EntityData(entity));
        }

        // merge data from sun-ejb-jar.xml file
        SunEjbJar sunEjbJar = getSunEjbJar(ejbModule);
        mergeEjbConfig(ejbModule, sunEjbJar);
        mergeEntityMappings(entities, ejbModule.getModuleId(), ejbModule.getEjbJar(), ejbModule.getOpenejbJar(), sunEjbJar);

        // merge data from sun-cmp-mappings.xml file
        SunCmpMappings sunCmpMappings = getSunCmpMappings(ejbModule);
        if (sunCmpMappings != null) {
            for (SunCmpMapping sunCmpMapping : sunCmpMappings.getSunCmpMapping()) {
                mergeEntityMappings(entities, ejbModule.getModuleId(), ejbModule, entityMappings, sunCmpMapping);
            }
        }
    }

    private void mergeEjbConfig(EjbModule ejbModule, SunEjbJar sunEjbJar) {
        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        if (openejbJar == null) return;
        if (sunEjbJar == null) return;
        if (sunEjbJar.getEnterpriseBeans() == null) return;

        Map<String,Map<String, WebserviceEndpoint>> endpointMap = new HashMap<String,Map<String, WebserviceEndpoint>>();
        for (Ejb ejb : sunEjbJar.getEnterpriseBeans().getEjb()) {
            EjbDeployment deployment = openejbJar.getDeploymentsByEjbName().get(ejb.getEjbName());
            if (deployment == null) {
                // warn no matching deployment
                continue;
            }

            // ejb jndi name is the deploymentId
            if (ejb.getJndiName() != null) {
                deployment.setDeploymentId(ejb.getJndiName());
            }

            // map ejb-ref jndi name declaration to deploymentId
            Map<String, EjbLink> linksMap = deployment.getEjbLinksMap();
            for (EjbRef ref : ejb.getEjbRef()) {
                if (ref.getJndiName() != null) {
                    String refName = ref.getEjbRefName();
                    EjbLink link = linksMap.get(refName);
                    if (link == null) {
                        link = new EjbLink();
                        link.setEjbRefName(refName);
                        linksMap.put(refName, link);
                        deployment.getEjbLink().add(link);
                    }
                    link.setDeployentId(ref.getJndiName());
                }
            }

            Map<String, ResourceLink> resourceLinksMap = deployment.getResourceLinksMap();
            for (ResourceRef ref : ejb.getResourceRef()) {
                if (ref.getJndiName() != null) {
                    String refName = ref.getResRefName();
                    ResourceLink link = resourceLinksMap.get(refName);
                    if (link == null) {
                        link = new ResourceLink();
                        link.setResRefName(refName);
                        resourceLinksMap.put(refName, link);
                        deployment.getResourceLink().add(link);
                    }
                    link.setResId(ref.getJndiName());
                }
            }

            for (ResourceEnvRef ref : ejb.getResourceEnvRef()) {
                if (ref.getJndiName() != null) {
                    String refName = ref.getResourceEnvRefName();
                    ResourceLink link = resourceLinksMap.get(refName);
                    if (link == null) {
                        link = new ResourceLink();
                        link.setResRefName(refName);
                        resourceLinksMap.put(refName, link);
                        deployment.getResourceLink().add(link);
                    }
                    link.setResId(ref.getJndiName());
                }
            }

            for (MessageDestinationRef ref : ejb.getMessageDestinationRef()) {
                if (ref.getJndiName() != null) {
                    String refName = ref.getMessageDestinationRefName();
                    ResourceLink link = resourceLinksMap.get(refName);
                    if (link == null) {
                        link = new ResourceLink();
                        link.setResRefName(refName);
                        resourceLinksMap.put(refName, link);
                        deployment.getResourceLink().add(link);
                    }
                    link.setResId(ref.getJndiName());
                }
            }

            EnterpriseBean bean = ejbJar.getEnterpriseBeansByEjbName().get(ejb.getEjbName());
            if (bean != null) {
                Map<String, ServiceRef> serviceRefMap = bean.getServiceRefMap();
                for (org.apache.openejb.jee.sun.ServiceRef ref : ejb.getServiceRef()) {
                    String refName = ref.getServiceRefName();
                    ServiceRef serviceRef = serviceRefMap.get(refName);
                    if (serviceRef != null) {
                        Map<String,PortComponentRef> ports = new TreeMap<String,PortComponentRef>();
                        for (PortComponentRef portComponentRef : serviceRef.getPortComponentRef()) {
                            ports.put(portComponentRef.getServiceEndpointInterface(), portComponentRef);
                        }

                        for (PortInfo portInfo : ref.getPortInfo()) {
                            PortComponentRef portComponentRef = ports.get(portInfo.getServiceEndpointInterface());
                            if (portComponentRef != null) {
                                WsdlPort wsdlPort = portInfo.getWsdlPort();
                                if (wsdlPort != null) {
                                    QName qname = new QName(wsdlPort.getNamespaceURI(), wsdlPort.getLocalpart());
                                    portComponentRef.setQName(qname);
                                }
                                for (StubProperty stubProperty : portInfo.getStubProperty()) {
                                    String name = stubProperty.getName();
                                    String value = stubProperty.getValue();
                                    portComponentRef.getProperties().setProperty(name, value);
                                }
                            }
                        }

                        String wsdlOverride = ref.getWsdlOverride();
                        if (wsdlOverride != null && wsdlOverride.length() > 0) {
                            String serviceId = extractServiceId(wsdlOverride);
                            serviceRef.setMappedName(serviceId);
                        }
                    }
                }
            }

            if (ejb.getMdbResourceAdapter() != null) {
                // resource adapter id is the MDB container ID
                String resourceAdapterId = ejb.getMdbResourceAdapter().getResourceAdapterMid();
                deployment.setContainerId(resourceAdapterId);
            }

            endpointMap.put(ejb.getEjbName(), ejb.getWebserviceEndpointMap());
        }

        // map wsdl locations
        if (ejbModule.getWebservices() != null) {
            Map<String, org.apache.openejb.jee.sun.WebserviceDescription> sunDescriptions = sunEjbJar.getEnterpriseBeans().getWebserviceDescriptionMap();
            for (WebserviceDescription description : ejbModule.getWebservices().getWebserviceDescription()) {
                org.apache.openejb.jee.sun.WebserviceDescription sunDescription = sunDescriptions.get(description.getWebserviceDescriptionName());

                // get the serviceId if specified
                String serviceId = null;
                if (sunDescription != null) {
                    serviceId = extractSerivceId(sunDescription.getWsdlPublishLocation(), description.getWsdlFile());
                }
                if (serviceId != null) {
                    description.setId(serviceId);
                }

                for (PortComponent port : description.getPortComponent()) {
                    // set the ejb bind location
                    ServiceImplBean bean = port.getServiceImplBean();
                    if (bean != null && bean.getEjbLink() != null) {
                        Map<String, WebserviceEndpoint> endpoints = endpointMap.get(bean.getEjbLink());
                        if (endpoints != null) {
                            WebserviceEndpoint endpoint = endpoints.get(port.getPortComponentName());
                            if (endpoint != null && endpoint.getEndpointAddressUri() != null) {
                                port.setLocation(endpoint.getEndpointAddressUri());
                            }
                        }
                    }
                }
            }
        }
    }

    private void mergeEntityMappings(Map<String, EntityData> entities, String moduleId, EjbJar ejbJar, OpenejbJar openejbJar, SunEjbJar sunEjbJar) {
        if (openejbJar == null) return;
        if (sunEjbJar == null) return;
        if (sunEjbJar.getEnterpriseBeans() == null) return;

        for (Ejb ejb : sunEjbJar.getEnterpriseBeans().getEjb()) {
            Cmp cmp = ejb.getCmp();
            if (cmp == null) {
                // skip non cmp beans
                continue;
            }

            // skip all non-CMP beans
            EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejb.getEjbName());
            if (!(enterpriseBean instanceof org.apache.openejb.jee.EntityBean) ||
                    ((EntityBean) enterpriseBean).getPersistenceType() != PersistenceType.CONTAINER) {
                continue;
            }
            EntityBean bean = (EntityBean) enterpriseBean;
            EntityData entityData = entities.get(moduleId + "#" + ejb.getEjbName());
            if (entityData == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            Collection<String> cmpFields = new ArrayList<String>(bean.getCmpField().size());
            for (CmpField cmpField : bean.getCmpField()) {
                cmpFields.add(cmpField.getFieldName());
            }

            OneOneFinders oneOneFinders = cmp.getOneOneFinders();
            if (oneOneFinders != null) {
                for (Finder finder : oneOneFinders.getFinder()) {
                    List<List<String>> params = parseQueryParamters(finder.getQueryParams());
                    String queryFilter = finder.getQueryFilter();
                    String ejbQl = convertToEjbQl(entityData.entity.getName(), cmpFields, finder.getQueryParams(), queryFilter);

                    NamedQuery namedQuery = new NamedQuery();

                    StringBuilder name = new StringBuilder();
                    name.append(entityData.entity.getName()).append(".").append(finder.getMethodName());
                    if (!params.isEmpty()) {
                        name.append('(');
                        boolean first = true;
                        for (List<String> methodParam : params) {
                            if (!first) name.append(",");
                            name.append(methodParam.get(0));
                            first = false;
                        }
                        name.append(')');
                    }
                    namedQuery.setName(name.toString());
                    namedQuery.setQuery(ejbQl);
                    entityData.entity.getNamedQuery().add(namedQuery);
                }
            }
        }
    }

    public void mergeEntityMappings(Map<String, EntityData> entities, String moduleId, EjbModule ejbModule, EntityMappings entityMappings, SunCmpMapping sunCmpMapping) {
        for (EntityMapping bean : sunCmpMapping.getEntityMapping()) {
            SunConversion.EntityData entityData = entities.get(moduleId + "#" + bean.getEjbName());
            if (entityData == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            Table table = new Table();
            // table.setSchema(schema);
            table.setName(bean.getTableName());
            entityData.entity.setTable(table);

            // warn about no equivalent of the consistence modes in sun file

            for (org.apache.openejb.jee.sun.SecondaryTable sunSecondaryTable : bean.getSecondaryTable()) {
                SecondaryTable secondaryTable = new SecondaryTable();
                secondaryTable.setName(sunSecondaryTable.getTableName());
                for (ColumnPair columnPair : sunSecondaryTable.getColumnPair()) {
                    SunColumnName localColumnName = new SunColumnName(columnPair.getColumnName().get(0), table.getName());
                    SunColumnName referencedColumnName = new SunColumnName(columnPair.getColumnName().get(1), table.getName());

                    // if user specified in reverse order, swap
                    if (localColumnName.table != null) {
                        SunColumnName temp = localColumnName;
                        localColumnName = referencedColumnName;
                        referencedColumnName = temp;
                    }

                    PrimaryKeyJoinColumn primaryKeyJoinColumn = new PrimaryKeyJoinColumn();
                    primaryKeyJoinColumn.setName(localColumnName.column);
                    primaryKeyJoinColumn.setReferencedColumnName(referencedColumnName.column);
                    secondaryTable.getPrimaryKeyJoinColumn().add(primaryKeyJoinColumn);
                }
            }

            for (CmpFieldMapping cmpFieldMapping : bean.getCmpFieldMapping()) {
                String fieldName = cmpFieldMapping.getFieldName();
                Field field = entityData.fields.get(fieldName);

                if (field == null) {
                    // todo warn no such cmp-field in the ejb-jar.xml
                    continue;
                }

                boolean readOnly = cmpFieldMapping.getReadOnly() != null;

                for (ColumnName columnName : cmpFieldMapping.getColumnName()) {
                    SunColumnName sunColumnName = new SunColumnName(columnName, table.getName());
                    Column column = new Column();
                    column.setTable(sunColumnName.table);
                    column.setName(sunColumnName.column);
                    if (readOnly) {
                        column.setInsertable(false);
                        column.setUpdatable(false);
                    }
                    field.setColumn(column);
                }
                // todo set fetch lazy when fetchWith is null
                // FetchedWith fetchedWith = cmpFieldMapping.getFetchedWith();
            }

            for (CmrFieldMapping cmrFieldMapping : bean.getCmrFieldMapping()) {
                String fieldName = cmrFieldMapping.getCmrFieldName();
                cmrFieldMapping.getColumnPair();
                RelationField field = entityData.relations.get(fieldName);
                if (field == null) {
                    // todo warn no such cmr-field in the ejb-jar.xml
                    continue;
                }

                if (field instanceof OneToOne) {
                    for (ColumnPair columnPair : cmrFieldMapping.getColumnPair()) {
                        SunColumnName localColumnName = new SunColumnName(columnPair.getColumnName().get(0), table.getName());
                        SunColumnName referencedColumnName = new SunColumnName(columnPair.getColumnName().get(1), table.getName());

                        // if user specified in reverse order, swap
                        if (localColumnName.table != null) {
                            SunColumnName temp = localColumnName;
                            localColumnName = referencedColumnName;
                            referencedColumnName = temp;
                        }

                        boolean isFk = !entityData.hasPkColumnMapping(localColumnName.column);
                        if (isFk) {
                            // Make sure that the field with the FK is marked as the owning field
                            field.setMappedBy(null);
                            field.getRelatedField().setMappedBy(field.getName());

                            JoinColumn joinColumn = new JoinColumn();
                            joinColumn.setName(localColumnName.column);
                            joinColumn.setReferencedColumnName(referencedColumnName.column);
                            field.getJoinColumn().add(joinColumn);
                        }
                    }
                } else if (field instanceof OneToMany) {
                    // Bi-directional OneToMany do not have field mappings
                    if (!field.getRelatedField().isSyntheticField()) {
                        continue;
                    }

                    for (ColumnPair columnPair : cmrFieldMapping.getColumnPair()) {
                        SunColumnName localColumnName = new SunColumnName(columnPair.getColumnName().get(0), table.getName());
                        SunColumnName otherColumnName = new SunColumnName(columnPair.getColumnName().get(1), table.getName());

                        // if user specified in reverse order, swap
                        if (localColumnName.table != null) {
                            SunColumnName temp = localColumnName;
                            localColumnName = otherColumnName;
                            otherColumnName = temp;
                        }

                        JoinColumn joinColumn = new JoinColumn();
                        // for OneToMany the join column name is the other (fk) column
                        joinColumn.setName(otherColumnName.column);
                        // and the referenced column is the local (pk) column
                        joinColumn.setReferencedColumnName(localColumnName.column);
                        field.getRelatedField().getJoinColumn().add(joinColumn);
                    }
                } else if (field instanceof ManyToOne) {
                    for (ColumnPair columnPair : cmrFieldMapping.getColumnPair()) {
                        SunColumnName localColumnName = new SunColumnName(columnPair.getColumnName().get(0), table.getName());
                        SunColumnName referencedColumnName = new SunColumnName(columnPair.getColumnName().get(1), table.getName());

                        // if user specified in reverse order, swap
                        if (localColumnName.table != null) {
                            SunColumnName temp = localColumnName;
                            localColumnName = referencedColumnName;
                            referencedColumnName = temp;
                        }

                        JoinColumn joinColumn = new JoinColumn();
                        joinColumn.setName(localColumnName.column);
                        joinColumn.setReferencedColumnName(referencedColumnName.column);
                        field.getJoinColumn().add(joinColumn);
                    }
                } else {
                    // skip the non owning side
                    if (field.getMappedBy() != null) continue;

                    JoinTable joinTable = new JoinTable();
                    field.setJoinTable(joinTable);
                    for (ColumnPair columnPair : cmrFieldMapping.getColumnPair()) {
                        SunColumnName localColumnName = new SunColumnName(columnPair.getColumnName().get(0), table.getName());
                        SunColumnName joinTableColumnName = new SunColumnName(columnPair.getColumnName().get(1), table.getName());

                        if (localColumnName.table == null || joinTableColumnName.table == null) {
                            // if user specified in reverse order, swap
                            if (localColumnName.table != null) {
                                SunColumnName temp = localColumnName;
                                localColumnName = joinTableColumnName;
                                joinTableColumnName = temp;
                            }

                            // join table is the table name of the referenced column
                            joinTable.setName(joinTableColumnName.table);

                            JoinColumn joinColumn = new JoinColumn();
                            joinColumn.setName(joinTableColumnName.column);
                            joinColumn.setReferencedColumnName(localColumnName.column);
                            joinTable.getJoinColumn().add(joinColumn);
                        } else {
                            // if user specified in reverse order, swap
                            if (localColumnName.table.equals(joinTable.getName())) {
                                SunColumnName temp = localColumnName;
                                localColumnName = joinTableColumnName;
                                joinTableColumnName = temp;
                            }

                            JoinColumn joinColumn = new JoinColumn();
                            joinColumn.setName(joinTableColumnName.column);
                            joinColumn.setReferencedColumnName(localColumnName.column);
                            joinTable.getInverseJoinColumn().add(joinColumn);
                        }

                    }
                }
            }
        }
    }

    public String convertToEjbQl(String abstractSchemaName, String queryParams, String queryFilter) {
        return convertToEjbQl(abstractSchemaName, Collections.<String>emptyList(), queryParams, queryFilter);
    }

    public String convertToEjbQl(String abstractSchemaName, Collection<String>  cmpFields, String queryParams, String queryFilter) {
        List<List<String>> variableNames = parseQueryParamters(queryParams);

        StringBuilder ejbQl = new StringBuilder();
        ejbQl.append("SELECT OBJECT(o) FROM ").append(abstractSchemaName).append(" AS o");
        String filter = convertToEjbQlFilter(cmpFields, variableNames, queryFilter);
        if (filter != null) {
            ejbQl.append(" WHERE ").append(filter);
        }
        return ejbQl.toString();
    }

    private List<List<String>> parseQueryParamters(String queryParams) {
        if (queryParams == null) return Collections.emptyList();

        List bits = Collections.list(new StringTokenizer(queryParams, " \t\n\r\f,", false));
        List<List<String>> params = new ArrayList<List<String>>(bits.size() / 2);
        for (int i = 0; i < bits.size(); i++) {
            String type = resolveType((String) bits.get(i));
            String param = (String) bits.get(++i);
            params.add(Arrays.asList(type, param));
        }
        return params;
    }

    private String resolveType(String type) {
        try {
            ClassLoader.getSystemClassLoader().loadClass(type);
            return type;
        } catch (ClassNotFoundException e) {
        }
        try {
            String javaLangType = "java.lang" + type;
            ClassLoader.getSystemClassLoader().loadClass(javaLangType);
            return javaLangType;
        } catch (ClassNotFoundException e) {
        }
        return type;
    }

    private String convertToEjbQlFilter(Collection<String> cmpFields, List<List<String>> queryParams, String queryFilter) {
        if (queryFilter == null) return null;

        Map<String, String> variableMap = new TreeMap<String, String>();
        for (String cmpField : cmpFields) {
            variableMap.put(cmpField, "o." + cmpField);
        }
        for (int i = 0; i < queryParams.size(); i++) {
            List<String> param = queryParams.get(i);
            variableMap.put(param.get(1), "?" + (i + 1));
        }

        Map<String, String> symbolMap = new TreeMap<String, String>();
        symbolMap.put("&&", "and");
        symbolMap.put("||", "or");
        symbolMap.put("!", "not");
        symbolMap.put("==", "=");
        symbolMap.put("!=", "<>");

        StringBuilder ejbQlFilter = new StringBuilder(queryFilter.length() * 2);
        List<String> tokens = tokenize(queryFilter);
        for (String token : tokens) {
            String mappedToken = symbolMap.get(token);
            if (mappedToken == null) {
                mappedToken = variableMap.get(token);
            }

            if (mappedToken != null) {
                ejbQlFilter.append(mappedToken);
            } else {
                ejbQlFilter.append(token);
            }
            ejbQlFilter.append(" ");
        }
        String filter = ejbQlFilter.toString().trim();
        if (filter.equalsIgnoreCase("true")) {
            return null;
        } else {
            return filter;
        }
    }

    private static enum TokenType {
        WHITESPACE, SYMBOL, NORMAL
    }

    private List<String> tokenize(String queryFilter) {
        LinkedList<String> tokens = new LinkedList<String>();
        List bits = Collections.list(new StringTokenizer(queryFilter, " \t\n\r\f()&|<>=!~+-/*", true));

        boolean inWitespace = false;
        String currentSymbol = "";
        for (int i = 0; i < bits.size(); i++) {
            TokenType tokenType;
            String bit = (String) bits.get(i);
            switch (bit.charAt(0)) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                    inWitespace = true;
                    tokenType = TokenType.WHITESPACE;
                    break;
                case '&':
                case '|':
                case '=':
                case '>':
                case '<':
                case '!':
                    // symbols are blindly coalesced so you can end up with nonsence like +-=+
                    currentSymbol += bit.charAt(0);
                    tokenType = TokenType.SYMBOL;
                    break;
                default:
                    tokenType = TokenType.NORMAL;
            }
            if (tokenType != TokenType.WHITESPACE && inWitespace) {
                // sequences of white space are simply removed
                inWitespace = false;
            }
            if (tokenType != TokenType.SYMBOL && currentSymbol.length() > 0) {
                tokens.add(currentSymbol);
                currentSymbol = "";
            }
            if (tokenType == TokenType.NORMAL) {
                tokens.add(bit);
            }
        }
        // add saved symobl if we have one
        if (currentSymbol.length() > 0) {
            tokens.add(currentSymbol);
            currentSymbol = "";
        }
        // strip off leading space
        if (tokens.getFirst().equals(" ")) {
            tokens.removeFirst();
        }
        return tokens;
    }

    private class SunColumnName{
        private final String table;
        private final String column;

        public SunColumnName(ColumnName columnName, String primaryTableName) {
            this(columnName.getvalue(), primaryTableName);
        }

        public SunColumnName(String fullName, String primaryTableName) {
            int dot = fullName.indexOf('.');
            if (dot > 0) {
                String t = fullName.substring(0, dot);
                if (primaryTableName.equals(t)) {
                    table = null;
                } else {
                    table = t;
                }
                column = fullName.substring(dot + 1);
            } else {
                table = null;
                column = fullName;
            }
        }
    }

    private class EntityData {
        private final Entity entity;
        private final Map<String, Id> ids = new TreeMap<String, Id>();
        private final Map<String, Field> fields = new TreeMap<String, Field>();
        private final Map<String, RelationField> relations = new TreeMap<String, RelationField>();

        public EntityData(Entity entity) {
            if (entity == null) throw new NullPointerException("entity is null");
            this.entity = entity;

            Attributes attributes = entity.getAttributes();
            if (attributes != null) {
                for (Id id : attributes.getId()) {
                    String name = id.getName();
                    ids.put(name, id);
                    fields.put(name, id);
                }

                for (Basic basic : attributes.getBasic()) {
                    String name = basic.getName();
                    fields.put(name, basic);
                }

                for (RelationField relationField : attributes.getOneToOne()) {
                    String name = relationField.getName();
                    relations.put(name, relationField);
                }

                for (RelationField relationField : attributes.getOneToMany()) {
                    String name = relationField.getName();
                    relations.put(name, relationField);
                }

                for (RelationField relationField : attributes.getManyToOne()) {
                    String name = relationField.getName();
                    relations.put(name, relationField);
                }

                for (RelationField relationField : attributes.getManyToMany()) {
                    String name = relationField.getName();
                    relations.put(name, relationField);
                }
            }

            for (AttributeOverride attributeOverride : entity.getAttributeOverride()) {
                String name = attributeOverride.getName();
                fields.put(name, attributeOverride);
            }
        }

        public boolean hasPkColumnMapping(String column) {
            for (Id id : ids.values()) {
                if (column.equals(id.getColumn().getName())) {
                    return true;
                }
            }
            return false;
        }
    }
}
