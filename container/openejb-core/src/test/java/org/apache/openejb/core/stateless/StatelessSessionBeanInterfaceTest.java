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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.stateless;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.Join;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Stack;

@RunWith(ApplicationComposer.class)
public class StatelessSessionBeanInterfaceTest extends Assert {

    @EJB
    private GreenRemoteHome greenRemoteHome;

    @EJB
    private BlueRemoteHome blueRemoteHome;

    @Module
    public EjbJar orange() {
        final EjbJar ejbJar = new EjbJar();
        {
            final StatelessBean bean = new StatelessBean("green", GreenBean.class);
            bean.setHomeAndRemote(GreenRemoteHome.class, GreenRemote.class);
            ejbJar.addEnterpriseBean(bean);
        }
        {
            final StatelessBean bean = new StatelessBean("blue", BlueBean.class);
            bean.setHomeAndRemote(BlueRemoteHome.class, BlueRemote.class);
            ejbJar.addEnterpriseBean(bean);
        }

        return ejbJar;
    }

    @Test
    public void testRegularEjbCreate() throws Exception {
        lifecycle.clear();

        assertNotNull(greenRemoteHome);
        final GreenRemote greenRemote = greenRemoteHome.create();
        assertEquals("", Join.join("\n", lifecycle));

        final String s = greenRemote.businessMethod("one", "two");
        assertEquals("setSessionContext\n" +
                "ejbCreate\n" +
                "businessMethod", Join.join("\n", lifecycle));

    }

    @Test
    public void testNamedEjbCreateMethod() throws Exception {
        lifecycle.clear();

        assertNotNull(blueRemoteHome);
        final BlueRemote greenRemote = blueRemoteHome.createObject();
        assertEquals("", Join.join("\n", lifecycle));

        final String s = greenRemote.businessMethod("one", "two");
        assertEquals("setSessionContext\n" +
                "ejbCreate\n" +
                "businessMethod", Join.join("\n", lifecycle));

    }

    private static final List<String> lifecycle = new Stack<String>();


    public static class GreenBean implements SessionBean {

        public void ejbCreate() {
            lifecycle.add("ejbCreate");
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
            lifecycle.add("ejbActivate");
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
            lifecycle.add("ejbPassivate");
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
            lifecycle.add("ejbRemove");
        }

        @Override
        public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
            lifecycle.add("setSessionContext");
        }

        public String businessMethod(String x, String y) {
            lifecycle.add("businessMethod");
            return Join.join("\n", x, y);
        }

    }

    private interface GreenRemoteHome extends EJBHome {
        GreenRemote create() throws RemoteException, CreateException;
    }

    private interface GreenRemote extends EJBObject {
        String businessMethod(String x, String y) throws RemoteException;
    }


    public static class BlueBean implements SessionBean {

        public void ejbCreateObject() {
            lifecycle.add("ejbCreate");
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
            lifecycle.add("ejbActivate");
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
            lifecycle.add("ejbPassivate");
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
            lifecycle.add("ejbRemove");
        }

        @Override
        public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
            lifecycle.add("setSessionContext");
        }

        public String businessMethod(String x, String y) {
            lifecycle.add("businessMethod");
            return Join.join("\n", x, y);
        }

    }

    private interface BlueRemoteHome extends EJBHome {
        BlueRemote createObject() throws RemoteException, CreateException;
    }

    private interface BlueRemote extends EJBObject {
        String businessMethod(String x, String y) throws RemoteException;
    }

}
