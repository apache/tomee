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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config.rules;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckNoCreateMethodsTest {
    @Keys(@Key(value = "no.home.create", count = 4))
    public EjbJar noCreateMethod() throws OpenEJBException {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
        EjbJar ejbJar = new EjbJar();
        StatelessBean stateless = new StatelessBean(FooStateless.class);
        stateless.setHomeAndRemote(FooStatelessHome.class, FooStatelessRemote.class);
        stateless.setHomeAndLocal(FooStatelessLocalHome.class, FooStatelessLocal.class);
        ejbJar.addEnterpriseBean(stateless);
        StatefulBean stateful = new StatefulBean(FooStateful.class);
        stateful.setHomeAndRemote(FooStatefulHome.class, FooStatefulRemote.class);
        stateful.setHomeAndLocal(FooStatefulLocalHome.class, FooStatefulLocal.class);
        ejbJar.addEnterpriseBean(stateful);
        return ejbJar;
    }

    private static interface FooStatelessHome extends javax.ejb.EJBHome {}

    private static interface FooStatelessRemote extends javax.ejb.EJBObject {}

    private static interface FooStatelessLocalHome extends javax.ejb.EJBLocalHome {}

    private static interface FooStatelessLocal extends javax.ejb.EJBLocalObject {}

    private static class FooStateless implements SessionBean {
        public void ejbCreate() {}

        @Override
        public void ejbActivate() throws EJBException, RemoteException {}

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {}

        @Override
        public void ejbRemove() throws EJBException, RemoteException {}

        @Override
        public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {}
    }

    private static interface FooStatefulHome extends javax.ejb.EJBHome {}

    private static interface FooStatefulRemote extends javax.ejb.EJBObject {}

    private static interface FooStatefulLocalHome extends javax.ejb.EJBLocalHome {}

    private static interface FooStatefulLocal extends javax.ejb.EJBLocalObject {}

    private static class FooStateful implements SessionBean {
        public void ejbCreate() {}

        @Override
        public void ejbActivate() throws EJBException, RemoteException {}

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {}

        @Override
        public void ejbRemove() throws EJBException, RemoteException {}

        @Override
        public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {}
    }
}
