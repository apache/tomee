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
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class ParseAppCtxXmlTest {
    @AppResource
    private AppModule module;

    @Module
    public AppModule module() {
        final AppModule app = new AppModule(ParseAppCtxXmlTest.class.getClassLoader(), "");
        app.getAltDDs().put("app-ctx.xml", ParseAppCtxXmlTest.class.getClassLoader().getResource("complete-app-ctx.xml"));
        app.getEjbModules().add(ejbModule("1"));
        app.getEjbModules().iterator().next().getEjbJar().addEnterpriseBean(new SingletonBean("CalculatorBean", CalculatorConfigured.class.getName()));
        app.getEjbModules().add(ejbModule("2"));
        app.getEjbModules().get(1).getEjbJar().addEnterpriseBean(new SingletonBean("BeanInAModule", CalculatorConfigured2.class.getName()));
        return app;
    }

    @Test
    public void parse() throws IOException, OpenEJBException {
        // Properties
        assertEquals("dummy", module.getProperties().getProperty("foo.bar"));
        assertEquals("10", module.getProperties().getProperty("AsynchronousPool.CorePoolSize"));
        assertEquals("10", module.getProperties().getProperty("AsynchronousPool.MaximumPoolSize"));
        assertEquals("foo", module.getProperties().getProperty("AnyPropertyPrefix.someproperty"));
        assertEquals("my-app", module.getProperties().getProperty("org.quartz.scheduler.instanceName"));
        assertEquals("my-bean", module.getProperties().getProperty("org.quartz.scheduler.instanceId"));
        assertEquals("org.superbiz.MyLogPlugin", module.getProperties().getProperty("org.quartz.plugin.LogPlugin.class"));
        assertEquals("3", module.getProperties().getProperty("1.2"));

        // imported config
        assertEquals("true", module.getProperties().getProperty("i.m.imported"));

        // BeanContext
        final EjbDeployment calculator = module.getEjbModules().get(1).getOpenejbJar().getDeploymentsByEjbName().get("CalculatorBean");
        assertEquals("ok", calculator.getProperties().getProperty("no.root"));
        assertEquals("wss4j", calculator.getProperties().getProperty("cxf.jaxws.in-interceptors"));

        // ModuleContext
        final EjbDeployment beanInAModule = module.getEjbModules().get(2).getOpenejbJar().getDeploymentsByEjbName().get("BeanInAModule");
        assertEquals("mId", beanInAModule.getProperties().getProperty("module.id"));
        assertEquals("2", module.getEjbModules().get(2).getProperties().getProperty("modulecontext"));

        // Pojo
        assertEquals("my-feature", module.getPojoConfigurations().get("org.foo.bar").getProperties().getProperty("cxf.jaxrs.features"));

        // Resources
        assertEquals("UsernameToken", module.getServices().iterator().next().getProperties().getProperty("action"));
        assertEquals("notsureitwillconnectthisway", module.getResources().iterator().next().getProperties().getProperty("JdbcUrl"));
    }

    private EjbModule ejbModule(final String id) {
        final EjbModule module = new EjbModule(new EjbJar());
        module.setModuleId(id);
        return module;
    }

    // just some class to be able to deploy "fake" ejbs

    public static class CalculatorConfigured {

    }

    public static class CalculatorConfigured2 {

    }
}
