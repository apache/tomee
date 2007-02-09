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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.axis;

import java.net.URI;

import org.apache.geronimo.axis.server.ServiceInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.openejb.DeploymentInfo;

public class WSContainerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(WSContainerGBean.class, WSContainer.class, NameFactory.WEB_SERVICE_LINK);

//        infoFactory.addOperation("invoke", new Class[]{WebServiceContainer.Request.class, WebServiceContainer.Response.class});

        infoFactory.addReference("EJBContainer", DeploymentInfo.class);
        infoFactory.addAttribute("location", URI.class, true);
        infoFactory.addAttribute("wsdlURI", URI.class, true);
        infoFactory.addAttribute("securityRealmName", String.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("transportGuarantee", String.class, true);
        infoFactory.addAttribute("authMethod", String.class, true);
        infoFactory.addAttribute("serviceInfo", ServiceInfo.class, true);
        infoFactory.addAttribute("virtualHosts", String[].class, true);
        infoFactory.addReference("WebServiceContainer", SoapHandler.class);

        infoFactory.setConstructor(new String[]{
            "EJBContainer",
            "location",
            "wsdlURI",
            "WebServiceContainer",
            "serviceInfo",
            "securityRealmName",
            "realmName",
            "transportGuarantee",
            "authMethod",
            "virtualHosts"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    //TODO why is a test method in runtime code?
//    public static ObjectName addGBean(Kernel kernel, String name, ObjectName ejbContainer, ObjectName listener, URI location, URI wsdlURI, ServiceInfo serviceInfo) throws GBeanAlreadyExistsException, GBeanNotFoundException {
//        GBeanData gbean = createGBean(name, ejbContainer, listener, location, wsdlURI, serviceInfo, null, null, null, null);
//        kernel.loadGBean(gbean, WSContainer.class.getClassLoader());
//        kernel.startGBean(gbean.getName());
//        return gbean.getName();
//    }
//
//    private static GBeanData createGBean(String name, ObjectName ejbContainer, ObjectName listener, URI location, URI wsdlURI, ServiceInfo serviceInfo, String securityRealmName, String realmName, String transportGuarantee, String authMethod) {
//        assert ejbContainer != null : "EJBContainer objectname is null";
//
//        ObjectName gbeanName = JMXUtil.getObjectName("openejb:type=WSContainer,name=" + name);
//
//        GBeanData gbean = new GBeanData(gbeanName, WSContainerGBean.GBEAN_INFO);
//        gbean.setReferencePattern("EJBContainer", ejbContainer);
//        gbean.setAttribute("location", location);
//        gbean.setAttribute("wsdlURI", wsdlURI);
//        gbean.setAttribute("serviceInfo", serviceInfo);
//        gbean.setAttribute("securityRealmName", securityRealmName);
//        gbean.setAttribute("realmName", realmName);
//        gbean.setAttribute("transportGuarantee", transportGuarantee);
//        gbean.setAttribute("authMethod", authMethod);
//
//        gbean.setReferencePattern("WebServiceContainer", listener);
//
//        return gbean;
//    }
}
