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

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.PortInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.oejb2.*;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import java.util.List;
import java.util.Properties;

public class AppInfoBuilderTest extends TestCase {
    
    public void testShouldAddSecurityDetailsToPortInfo() throws Exception {
        EjbJar ejbJar = new EjbJar();
        SessionBean sessionBean = new SessionBean();
        sessionBean.setEjbName("MySessionBean");
        sessionBean.setEjbClass("org.superbiz.MySessionBean");
        sessionBean.setRemote("org.superbiz.MySession");
        ejbJar.addEnterpriseBean(sessionBean);
        
        OpenejbJar openejbJar = new OpenejbJar();
        EjbDeployment ejbDeployment = new EjbDeployment();
        openejbJar.addEjbDeployment(ejbDeployment);
        
        ejbDeployment.setEjbName("MySessionBean");
        ejbDeployment.addProperty("webservice.security.realm", "MyRealm");
        ejbDeployment.addProperty("webservice.security.securityRealm", "MySecurityRealm");
        ejbDeployment.addProperty("webservice.security.transportGarantee", TransportGuaranteeType.NONE.value());
        ejbDeployment.addProperty("webservice.security.authMethod", AuthMethodType.BASIC.value());
        ejbDeployment.addProperty("wss4j.in.action", "Timestamp");
        ejbDeployment.addProperty("wss4j.out.action", "Timestamp");

        EjbModule ejbModule =  new EjbModule(ejbJar, openejbJar);

        EjbJarInfo ejbJarInfo = new EjbJarInfo();
        PortInfo portInfo = new PortInfo();
        portInfo.serviceLink = "MySessionBean"; 
        ejbJarInfo.portInfos.add(portInfo);

        new AppInfoBuilder(null).configureWebserviceSecurity(ejbJarInfo, ejbModule);

        List<PortInfo> portInfoList = ejbJarInfo.portInfos;
        assertEquals(1, portInfoList.size());
        PortInfo info = portInfoList.get(0);
        assertEquals("MyRealm", info.realmName);
        assertEquals("MySecurityRealm", info.securityRealmName);
        assertEquals("BASIC", info.authMethod);
        assertEquals("NONE", info.transportGuarantee);
        assertEquals("Timestamp", portInfo.properties.getProperty("wss4j.in.action"));
        assertEquals("Timestamp", portInfo.properties.getProperty("wss4j.out.action"));
    }

    public void testShouldUseDefaultsIfSettingIsNull() throws Exception {
        EjbJar ejbJar = new EjbJar();
        SessionBean sessionBean = new SessionBean();
        sessionBean.setEjbName("MySessionBean");
        sessionBean.setEjbClass("org.superbiz.MySessionBean");
        sessionBean.setRemote("org.superbiz.MySession");
        ejbJar.addEnterpriseBean(sessionBean);
        
        OpenejbJar openejbJar = new OpenejbJar();
        EjbDeployment ejbDeployment = new EjbDeployment();
        openejbJar.addEjbDeployment(ejbDeployment);
        
        ejbDeployment.setEjbName("MySessionBean");

        EjbModule ejbModule =  new EjbModule(ejbJar, openejbJar);

        EjbJarInfo ejbJarInfo = new EjbJarInfo();
        PortInfo portInfo = new PortInfo();
        portInfo.serviceLink = "MySessionBean";
        ejbJarInfo.portInfos.add(portInfo);

        new AppInfoBuilder(null).configureWebserviceSecurity(ejbJarInfo, ejbModule);

        List<PortInfo> portInfoList = ejbJarInfo.portInfos;
        assertEquals(1, portInfoList.size());
        PortInfo info = portInfoList.get(0);
        assertEquals(null, info.realmName);
        assertEquals(null, info.securityRealmName);
        assertEquals("NONE", info.authMethod);
        assertEquals("NONE", info.transportGuarantee);
        assertTrue(portInfo.properties.isEmpty());
    }

    public void testShouldIgnorePortInfoThatDontMatchTheEjb() throws Exception {
        EjbJar ejbJar = new EjbJar();
        SessionBean sessionBean = new SessionBean();
        sessionBean.setEjbName("MySessionBean");
        sessionBean.setEjbClass("org.superbiz.MySessionBean");
        sessionBean.setRemote("org.superbiz.MySession");
        ejbJar.addEnterpriseBean(sessionBean);

        OpenejbJar openejbJar = new OpenejbJar();
        EjbDeployment ejbDeployment = new EjbDeployment();
        openejbJar.addEjbDeployment(ejbDeployment);
        
        ejbDeployment.setEjbName("MySessionBean");

        EjbModule ejbModule =  new EjbModule(ejbJar, openejbJar);

        EjbJarInfo ejbJarInfo = new EjbJarInfo();
        PortInfo portInfo = new PortInfo();
        portInfo.authMethod = "DIGEST";
        portInfo.realmName = "";
        portInfo.securityRealmName = "";
        portInfo.transportGuarantee = "CONFIDENTIAL";
        portInfo.serviceLink = "DifferentInfo";
        Properties props = new Properties();
        props.put("wss4j.in.action", "Timestamp");
        props.put("wss4j.out.action", "Timestamp");
        portInfo.properties = props;
        ejbJarInfo.portInfos.add(portInfo);

        new AppInfoBuilder(null).configureWebserviceSecurity(ejbJarInfo, ejbModule);

        List<PortInfo> portInfoList = ejbJarInfo.portInfos;
        assertEquals(1, portInfoList.size());
        PortInfo info = portInfoList.get(0);
        assertEquals("", info.realmName);
        assertEquals("", info.securityRealmName);
        assertEquals("DIGEST", info.authMethod);
        assertEquals("CONFIDENTIAL", info.transportGuarantee);
        assertEquals("Timestamp", portInfo.properties.getProperty("wss4j.in.action"));
        assertEquals("Timestamp", portInfo.properties.getProperty("wss4j.out.action"));
    }
}
