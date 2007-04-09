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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.util.proxy.Jdk13ProxyFactory;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.EjbJarInfoBuilder;
import org.apache.openejb.config.JndiEncInfoBuilder;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.assembler.classic.EjbJarBuilder;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.ri.sp.PseudoTransactionService;

import javax.ejb.SessionContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class StatelessContainerTest extends TestCase {
    private StatelessContainer container;
    private DeploymentInfo deploymentInfo;

    public void testPojoStyleBean() throws Exception {

        Object result = container.invoke("widget", Widget.class.getMethod("getLifecycle"), new Object[]{}, null);
        assertTrue("instance of Stack", result instanceof Stack);

        Stack<Lifecycle> actual = (Stack<Lifecycle>) result;

        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected), join("\n", actual));
    }

    public void testBusinessLocalInterface() throws Exception {

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

        // Check the lifecycle of the bean
        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected) , join("\n", lifecycle));
    }

    public void testBusinessRemoteInterface() throws Exception {

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

        // Check the lifecycle of the bean
        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected) , join("\n", lifecycle));
    }

    protected void setUp() throws Exception {
        super.setUp();
        StatelessBean bean = new StatelessBean("widget", WidgetBean.class.getName());
        bean.setBusinessLocal(Widget.class.getName());
        bean.setBusinessRemote(RemoteWidget.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment("Stateless Container", "widget", "widget"));

        EjbModule jar = new EjbModule(this.getClass().getClassLoader(), "", ejbJar, openejbJar);

        PseudoTransactionService transactionManager = new PseudoTransactionService();
        PseudoSecurityService securityService = new PseudoSecurityService();
        SystemInstance.get().setComponent(SecurityService.class, securityService);
        container = new StatelessContainer("Stateless Container", transactionManager, securityService, 10, 0, false);
        Properties props = new Properties();
        props.put(container.getContainerID(), container);

        HashMap<String, DeploymentInfo> ejbs = build(props, jar);
        deploymentInfo = ejbs.get("widget");

        ProxyManager.registerFactory("ivm_server", new Jdk13ProxyFactory());
        ProxyManager.setDefaultFactory("ivm_server");

        WidgetBean.lifecycle.clear();
    }

    private static String join(String delimeter, List items) {
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

    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
    }

    public static interface RemoteWidget extends Widget {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    public static class WidgetBean implements Widget, RemoteWidget {

        private static Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        public void setSessionContext(SessionContext sessionContext) {
            //lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void destroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }
}
