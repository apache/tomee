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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.EjbJarInfoBuilder;
import org.apache.openejb.config.JndiEncInfoBuilder;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.assembler.classic.EjbJarBuilder;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;

import javax.ejb.SessionContext;
import javax.ejb.Remote;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class StatefulContainerTest extends TestCase {

    public void testBusinessLocalInterface() throws Exception {

        // Do a create...

        InitialContext ctx = new InitialContext();
        Object object = ctx.lookup("WidgetBeanBusinessLocal");

        assertTrue("instanceof widget", object instanceof Widget);

        Widget widget = (Widget) object;

        // Do a business method...
        Stack<Lifecycle> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle",lifecycle);

        // Do a remove...
        widget.destroy();

        // Check the lifecycle of the bean
        List expected = Arrays.asList(StatefulContainerTest.Lifecycle.values());

        assertEquals(StatefulContainerTest.join("\n", expected) , join("\n", WidgetBean.lifecycle));
    }

    public void testBusinessRemoteInterface() throws Exception {
        WidgetBean.lifecycle.clear();

        // Do a create...

        InitialContext ctx = new InitialContext();
        Object object = ctx.lookup("WidgetBeanBusinessRemote");

        assertTrue("instanceof widget", object instanceof RemoteWidget);

        RemoteWidget widget = (RemoteWidget) object;

        // Do a business method...
        Stack<Lifecycle> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle",lifecycle);
        assertNotSame("is copy", lifecycle, WidgetBean.lifecycle);

        // Do a remove...
        widget.destroy();

        try {
            widget.destroy();
            fail("Calling a removed bean should not be possible");
        } catch (Exception e) {
        }

        // Check the lifecycle of the bean
        List expected = Arrays.asList(StatefulContainerTest.Lifecycle.values());

        assertEquals(StatefulContainerTest.join("\n", expected) , join("\n", WidgetBean.lifecycle));
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createConnectionManager(config.configureService(ConnectionManagerInfo.class));

        // containers
        StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        statefulContainerInfo.properties.setProperty("PoolSize", "0");
        statefulContainerInfo.properties.setProperty("BulkPassivate", "1");
        assembler.createContainer(statefulContainerInfo);

        // Setup the descriptor information

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(WidgetBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        WidgetBean.lifecycle.clear();

    }

    private static String join(String delimeter, List items){
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    private HashMap<String, DeploymentInfo> build(Properties props, EjbModule ejbModule) throws OpenEJBException {
        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();
        EjbJarInfo jarInfo = infoBuilder.buildInfo(ejbModule);

        // Process JNDI refs
        JndiEncInfoBuilder.initJndiReferences(ejbModule, jarInfo);

        EjbJarBuilder builder = new EjbJarBuilder(props, this.getClass().getClassLoader());
        HashMap<String, DeploymentInfo> ejbs = builder.build(jarInfo,null);
        return ejbs;
    }

    @Local
    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
        void destroy();
    }

    @Remote
    public static interface RemoteWidget extends Widget {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, PRE_PASSIVATE1, POST_ACTIVATE1, BUSINESS_METHOD, PRE_PASSIVATE2, POST_ACTIVATE2, PRE_PASSIVATE3, POST_ACTIVATE3, REMOVE, PRE_DESTROY, 
    }

    public static class WidgetBean implements Widget, RemoteWidget, Serializable {
        private static final long serialVersionUID = -8499745487520955081L;

        private int activates = 0;
        private int passivates = 0;

        public static Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setSessionContext(SessionContext sessionContext){
            lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        @PostActivate
        public void activate(){
            lifecycle.push(Enum.valueOf(Lifecycle.class, "POST_ACTIVATE" + (++activates)));
        }

        @PrePassivate
        public void passivate(){
            lifecycle.push(Enum.valueOf(Lifecycle.class, "PRE_PASSIVATE" + (++passivates)));
        }

        @PostConstruct
        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        @PreDestroy
        public void predestroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }

        @Remove
        public void destroy() {
            lifecycle.push(Lifecycle.REMOVE);
        }
    }
}
