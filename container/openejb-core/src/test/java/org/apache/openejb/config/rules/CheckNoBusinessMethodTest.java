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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckNoBusinessMethodTest {
    @Keys( { @Key(value = "no.busines.method.case", count = 4), @Key(value = "no.busines.method.args", count = 4), @Key(value = "no.busines.method", count = 4) })
    public EjbJar noBusinessMethod() throws OpenEJBException {
        // System.setProperty("openejb.validation.output.level", "VERBOSE");
        EjbJar ejbJar = new EjbJar();
        StatelessBean stateLessLocal = new StatelessBean(FooStatelessSession.class);
        stateLessLocal.setLocalHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooLocalHome");
        stateLessLocal.setLocal("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooLocal");
        StatefulBean statefulLocal = new StatefulBean(FooStatefulSession.class);
        statefulLocal.setLocalHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooLocalHome");
        statefulLocal.setLocal("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooLocal");
        StatelessBean stateLessRemote = new StatelessBean(FooRemoteStatelessSession.class);
        stateLessRemote.setHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooRemoteHome");
        stateLessRemote.setRemote("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooRemote");
        StatefulBean statefulRemote = new StatefulBean(FooRemoteStatefulSession.class);
        statefulRemote.setHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooRemoteHome");
        statefulRemote.setRemote("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooRemote");
        ejbJar.addEnterpriseBean(stateLessLocal);
        ejbJar.addEnterpriseBean(statefulLocal);
        ejbJar.addEnterpriseBean(stateLessRemote);
        ejbJar.addEnterpriseBean(statefulRemote);
        return ejbJar;
    }

    private static interface FooLocalHome extends EJBLocalHome {
        FooLocal create() throws CreateException;
    }

    private static interface FooLocal extends EJBLocalObject {
        void foo(String x, String y);

        // this method is not implemented by the bean class
        void foo1();
    }

    private static class FooStatelessSession implements SessionBean {
        // method name is same as in the Local interface, except arguments are different
        public void foo(int x, String y) {}

        // method name has a different case
        public void Foo(String x, String y) {}

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

    private static class FooStatefulSession implements SessionBean {
        // method name is same as in the Local interface, except arguments are different
        public void foo(int x, String y) {}

        // method name has a different case
        public void Foo(String x, String y) {}

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

    // =================
    private static interface FooRemoteHome extends EJBHome {
        FooRemote create() throws RemoteException, CreateException;
    }

    private static interface FooRemote extends EJBObject {
        void foo(String x, String y) throws RemoteException;

        // This method is not implemented by the bean class
        void foo1() throws RemoteException;
    }

    private static class FooRemoteStatelessSession implements SessionBean {
        // method name is same as in the Remote interface, except arguments are different
        public void foo(int x, String y) {}

        // method name has a different case
        public void Foo(String x, String y) {}

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

    private static class FooRemoteStatefulSession implements SessionBean {
        // method name is same as in the Remote interface, except arguments are different
        public void foo(int x, String y) {}

        // method name has a different case
        public void Foo(String x, String y) {}

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
