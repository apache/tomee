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

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;

import java.util.List;

public class MappedNameBuilderTest extends TestCase {
    public void testShouldCreateJndiEntryForBeanWithMappedName() throws Exception {
        AppModule appModule = new AppModule(new FakeClassLoader(), "");
        EjbJar ejbJar = new EjbJar();
        OpenejbJar openejbJar = new OpenejbJar();

        SessionBean sessionBean = new SessionBean("SessionBean", "org.superbiz.SessionBean", SessionType.STATELESS);
        sessionBean.setMappedName("MappedName");
        ejbJar.addEnterpriseBean(sessionBean);

        EjbDeployment ejbDeployment = new EjbDeployment("containerId","deploymentId", "SessionBean");
        openejbJar.addEjbDeployment(ejbDeployment);
        appModule.getEjbModules().add(new EjbModule(ejbJar, openejbJar));

        appModule = new MappedNameBuilder().deploy(appModule);

        EjbDeployment retrievedDeployment = appModule.getEjbModules().get(0).getOpenejbJar().getDeploymentsByEjbName().get("SessionBean");
        List<Jndi> jndiList = retrievedDeployment.getJndi();

        assertNotNull(jndiList);
        assertEquals(1, jndiList.size());
        assertEquals("MappedName", jndiList.get(0).getName());
        assertEquals("Remote", jndiList.get(0).getInterface());
    }

    public void testIgnoreMappedNameIfOpenejbJarModuleDoesntExist() throws Exception {
        AppModule appModule = new AppModule(new FakeClassLoader(), "");
        EjbJar ejbJar = new EjbJar();

        SessionBean sessionBean = new SessionBean("SessionBean", "org.superbiz.SessionBean", SessionType.STATELESS);
        sessionBean.setMappedName("MappedName");
        ejbJar.addEnterpriseBean(sessionBean);

        appModule.getEjbModules().add(new EjbModule(ejbJar, null));
        appModule = new MappedNameBuilder().deploy(appModule);

        OpenejbJar openejbJar = appModule.getEjbModules().get(0).getOpenejbJar();
        assertNull(openejbJar);
    }

    public void testShouldIgnoreMappedNameIfDeploymentDoesntExist() throws Exception {
        AppModule appModule = new AppModule(new FakeClassLoader(), "");
        EjbJar ejbJar = new EjbJar();
        OpenejbJar openejbJar = new OpenejbJar();

        SessionBean sessionBean = new SessionBean("SessionBean", "org.superbiz.SessionBean", SessionType.STATELESS);
        sessionBean.setMappedName("MappedName");
        ejbJar.addEnterpriseBean(sessionBean);

        appModule.getEjbModules().add(new EjbModule(ejbJar, openejbJar));
        appModule = new MappedNameBuilder().deploy(appModule);

        EjbDeployment deployment = appModule.getEjbModules().get(0).getOpenejbJar().getDeploymentsByEjbName().get("SessionBean");
        assertNull(deployment);
    }

    private class FakeClassLoader extends ClassLoader {
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return Object.class;
        }
    }
}
