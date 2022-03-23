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
package org.apache.openejb.core;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.osgi.client.LocalInitialContextFactory;
import org.junit.AfterClass;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.LocalBean;
import jakarta.ejb.LocalHome;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.Properties;


/**
 * @version $Rev$ $Date$
 */
public class EjbContextTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean("Stateful", MySessionBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean("Stateless", MySessionBean.class));
        ejbJar.addEnterpriseBean(new SingletonBean("Singleton", MySessionBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        final Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        final InitialContext context = new InitialContext(properties);


        {
            final MySessionBean bean = (MySessionBean) context.lookup("StatefulLocalBean");
            bean.test();
        }
        {
            final MySessionBean bean = (MySessionBean) context.lookup("StatelessLocalBean");
            bean.test();
        }
        {
            final MySessionBean bean = (MySessionBean) context.lookup("SingletonLocalBean");
            bean.test();
        }
    }

    public static interface Home extends EJBLocalHome {
        LocalObject create() throws CreateException;
    }

    public static interface LocalObject extends EJBLocalObject {
    }

    @LocalBean
    @LocalHome(Home.class)
    public static class MySessionBean implements SessionBean {

        private Exception exception;

        public void ejbCreate() throws CreateException {
        }

        public void test() throws Exception {
            if (exception != null) throw exception;
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(final SessionContext sessionContext) throws EJBException, RemoteException {
            try {
                sessionContext.getEJBLocalHome();
            } catch (final Exception e) {
                exception = e;
            }
        }
    }

}
