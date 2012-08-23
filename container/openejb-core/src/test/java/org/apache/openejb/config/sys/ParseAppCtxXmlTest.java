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
package org.apache.openejb.config.sys;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppContextConfigDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ParseAppCtxXmlTest {
    @Test
    public void parse() throws IOException, OpenEJBException {
        final AppModule module = new AppModule(ParseAppCtxXmlTest.class.getClassLoader(), "");
        module.getAltDDs().put("app-ctx.xml", ParseAppCtxXmlTest.class.getClassLoader().getResource("complete-app-ctx.xml"));
        module.getEjbModules().add(new EjbModule(new EjbJar()));
        module.getEjbModules().iterator().next().getEjbJar().addEnterpriseBean(new SingletonBean("CalculatorBean", "CalculatorBean"));
        new AppContextConfigDeployer().deploy(module);

        // Properties
        assertEquals("10", module.getProperties().getProperty("AsynchronousPool.CorePoolSize"));
        assertEquals("10", module.getProperties().getProperty("AsynchronousPool.MaximumPoolSize"));
        assertEquals("foo", module.getProperties().getProperty("AnyPropertyPrefix.someproperty"));
        assertEquals("my-app", module.getProperties().getProperty("org.quartz.scheduler.instanceName"));
        assertEquals("my-bean", module.getProperties().getProperty("org.quartz.scheduler.instanceId"));
        assertEquals("org.superbiz.MyLogPlugin", module.getProperties().getProperty("org.quartz.plugin.LogPlugin.class"));

        // BeanContext
        assertEquals("wss4j", module.getEjbModules().iterator().next().getOpenejbJar().getDeploymentsByEjbName().get("CalculatorBean").getProperties().getProperty("cxf.jaxws.in-interceptors"));

        // Pojo
        assertEquals("my-feature", module.getPojoConfigurations().get("org.foo.bar").getProperties().getProperty("cxf.jaxrs.features"));

        // Resources
        assertEquals("UsernameToken", module.getServices().iterator().next().getProperties().getProperty("action"));
        assertEquals("notsureitwillconnectthisway", module.getResources().iterator().next().getProperties().getProperty("JdbcUrl"));
    }
}
