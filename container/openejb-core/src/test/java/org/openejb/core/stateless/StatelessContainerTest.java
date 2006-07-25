/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.core.stateless;

import junit.framework.TestCase;
import org.openejb.jee.StatelessBean;
import org.openejb.jee.EjbJar;
import org.openejb.jee.LifecycleCallback;
import org.openejb.alt.config.DeployedJar;
import org.openejb.alt.config.EjbJarInfoBuilder;
import org.openejb.alt.config.ejb.OpenejbJar;
import org.openejb.alt.config.ejb.EjbDeployment;
import org.openejb.assembler.classic.EjbJarBuilder;
import org.openejb.assembler.classic.EjbJarInfo;
import org.openejb.core.DeploymentInfo;
import org.openejb.ri.sp.PseudoTransactionService;
import org.openejb.ri.sp.PseudoSecurityService;
import org.openejb.OpenEJBException;

import javax.ejb.SessionContext;
import java.util.HashMap;

/**
 * @version $Revision$ $Date$
 */
public class StatelessContainerTest extends TestCase {

    public void testPojoStyleBean() throws Exception {

        StatelessBean bean = new StatelessBean("widget", WidgetBean.class.getName());
        bean.setBusinessLocal(Widget.class.getName());
        bean.addPostConstruct("init");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment("Stateless Container", "widget", "widget"));

        DeployedJar jar = new DeployedJar("", ejbJar, openejbJar);

        HashMap<String, DeploymentInfo> ejbs = build(jar);

        StatelessContainer container = new StatelessContainer("Stateless Container", new PseudoTransactionService(), new PseudoSecurityService(), ejbs, 10, 10, false);

        Object result = container.invoke("widget", Widget.class.getMethod("add", int.class, int.class), new Object[]{2, 3}, null, "");
        result = container.invoke("widget", Widget.class.getMethod("initCalled"), new Object[]{}, null, "");
        System.out.println("result = " + result);
    }

    private HashMap<String, DeploymentInfo> build(DeployedJar jar) throws OpenEJBException {
        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();
        EjbJarBuilder builder = new EjbJarBuilder(this.getClass().getClassLoader());
        EjbJarInfo jarInfo = infoBuilder.buildInfo(jar);
        HashMap<String, DeploymentInfo> ejbs = builder.build(jarInfo);
        return ejbs;
    }

    public static interface Widget {
        int add(int a, int b);
        boolean initCalled();
    }

    public static class WidgetBean implements Widget {
        private SessionContext sessionContext;
        private boolean initCalled;

        public void setSessionContext(SessionContext sessionContext){
            this.sessionContext = sessionContext;
        }

        public int add(int a, int b) {
            return a + b;
        }

        public boolean initCalled() {
            return initCalled;
        }

        public void init() {
            initCalled = true;
        }
    }
}
