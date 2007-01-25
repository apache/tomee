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
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.alt.config.EjbModule;
import org.apache.openejb.alt.config.EjbJarInfoBuilder;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.ri.sp.PseudoTransactionService;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.util.proxy.Jdk13ProxyFactory;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.classic.EjbJarBuilder;
import org.apache.openejb.assembler.classic.EjbJarInfo;

import javax.ejb.SessionContext;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class StatefulContainerTest extends TestCase {
    private StatefulContainer container;
    private DeploymentInfo deploymentInfo;

    public void testPojoStyleBean() throws Exception {

        // Do a create...
        Method createMethod = DeploymentInfo.BusinessLocalHome.class.getMethod("create");

        Object result = container.invoke("widget", createMethod, new Object[]{}, null, "");
        assertTrue("instance of ProxyInfo", result instanceof ProxyInfo);
        ProxyInfo proxyInfo = (ProxyInfo) result;

        // Do a business method...
        result = container.invoke("widget", Widget.class.getMethod("getLifecycle"), new Object[]{}, proxyInfo.getPrimaryKey(), "");
        assertTrue("instance of Stack", result instanceof Stack);

        // Do a remove...
        result = container.invoke("widget", Widget.class.getMethod("destroy"), new Object[]{}, proxyInfo.getPrimaryKey(), "");

        // Check the lifecycle of the bean
        List expected = Arrays.asList(StatefulContainerTest.Lifecycle.values());

        assertEquals(StatefulContainerTest.join("\n", expected) , join("\n", WidgetBean.lifecycle));
    }

    public void testBusinessLocalInterface() throws Exception {

        // Do a create...

        CoreDeploymentInfo coreDeploymentInfo = (CoreDeploymentInfo) deploymentInfo;
        DeploymentInfo.BusinessLocalHome businessLocalHome = coreDeploymentInfo.getBusinessLocalHome();
        assertNotNull("businessLocalHome", businessLocalHome);

        Object object = businessLocalHome.create();
        assertNotNull("businessLocalHome.create()", businessLocalHome);

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

        CoreDeploymentInfo coreDeploymentInfo = (CoreDeploymentInfo) deploymentInfo;
        DeploymentInfo.BusinessRemoteHome businessRemoteHome = coreDeploymentInfo.getBusinessRemoteHome();
        assertNotNull("businessRemoteHome", businessRemoteHome);

        Object object = businessRemoteHome.create();
        assertNotNull("businessRemoteHome.create()", businessRemoteHome);

        assertTrue("instanceof widget", object instanceof RemoteWidget);

        RemoteWidget widget = (RemoteWidget) object;

        // Do a business method...
        Stack<Lifecycle> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle",lifecycle);
        assertNotSame("is copy", lifecycle, WidgetBean.lifecycle);

        // Do a remove...
        widget.destroy();

        // Check the lifecycle of the bean
        List expected = Arrays.asList(StatefulContainerTest.Lifecycle.values());

        assertEquals(StatefulContainerTest.join("\n", expected) , join("\n", WidgetBean.lifecycle));
    }

    protected void setUp() throws Exception {
        // Setup the descriptor information

        StatefulBean bean = new StatefulBean("widget", WidgetBean.class.getName());
        bean.setBusinessLocal(Widget.class.getName());
        bean.setBusinessRemote(RemoteWidget.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");
        bean.addPrePassivate("passivate");
        bean.addPostActivate("activate");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment("Stateful Container", "widget", "widget"));

        EjbModule jar = new EjbModule(this.getClass().getClassLoader(), "", ejbJar, openejbJar);

        // Build and register the TransactionManager and SecurityService

        PseudoTransactionService transactionManager = new PseudoTransactionService();
        SystemInstance.get().setComponent(TransactionManager.class, transactionManager);

        PseudoSecurityService securityService = new PseudoSecurityService();
        SystemInstance.get().setComponent(SecurityService.class, securityService);


        // Create the Container
        container = new StatefulContainer("Stateful Container", transactionManager, securityService,null, 10, 0, 1);
        Properties props = new Properties();
        props.put(container.getContainerID(), container);

        // Build the DeploymentInfos

        HashMap<String, DeploymentInfo> ejbs = build(props, jar);
        deploymentInfo = ejbs.get("widget");

        ProxyManager.registerFactory("ivm_server", new Jdk13ProxyFactory());
        ProxyManager.setDefaultFactory("ivm_server");


        WidgetBean.lifecycle.clear();

    }

    private static String join(String delimeter, List items){
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    private HashMap<String, DeploymentInfo> build(Properties props, EjbModule jar) throws OpenEJBException {
        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();
        EjbJarBuilder builder = new EjbJarBuilder(props, this.getClass().getClassLoader());
        EjbJarInfo jarInfo = infoBuilder.buildInfo(jar);
        HashMap<String, DeploymentInfo> ejbs = builder.build(jarInfo,null);
        return ejbs;
    }

    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
        void destroy();
    }

    public static interface RemoteWidget extends Widget {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, POST_CONSTRUCT, PRE_PASSIVATE1, POST_ACTIVATE1, BUSINESS_METHOD, PRE_PASSIVATE2, POST_ACTIVATE2, PRE_DESTROY
    }

    public static class WidgetBean implements Widget, RemoteWidget, Serializable {

        private int activates = 0;
        private int passivates = 0;

        public static Stack<Lifecycle> lifecycle = new Stack();

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }
        public void setSessionContext(SessionContext sessionContext){
        //    lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        public void activate(){
            lifecycle.push((Lifecycle) Enum.valueOf(Lifecycle.class, "POST_ACTIVATE" + (++activates)));
        }

        public void passivate(){
            lifecycle.push((Lifecycle) Enum.valueOf(Lifecycle.class, "PRE_PASSIVATE" + (++passivates)));
        }

        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void destroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }
}
