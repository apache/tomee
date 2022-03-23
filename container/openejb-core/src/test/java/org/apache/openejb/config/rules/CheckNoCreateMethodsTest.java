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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.EntityContext;
import jakarta.ejb.FinderException;
import jakarta.ejb.LocalHome;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.RemoveException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import java.rmi.RemoteException;

@RunWith(ValidationRunner.class)
public class CheckNoCreateMethodsTest {
    @Keys({@Key(value = "no.home.create", count = 4), @Key(value = "unused.ejb.create", count = 2, type = KeyType.WARNING),
        @Key(value = "unused.ejbPostCreate", type = KeyType.WARNING), @Key("entity.no.ejb.create"), @Key(value = "session.no.ejb.create", count = 2)})
    public EjbJar noCreateMethod() throws OpenEJBException {
        System.setProperty("openejb.validation.output.level", "VERBOSE");
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean stateless = new StatelessBean(FooStateless.class);
        stateless.setHomeAndRemote(FooStatelessHome.class, FooStatelessRemote.class);
        stateless.setHomeAndLocal(FooStatelessLocalHome.class, FooStatelessLocal.class);
        ejbJar.addEnterpriseBean(stateless);
        final StatefulBean stateful = new StatefulBean(FooStateful.class);
        stateful.setHomeAndRemote(FooStatefulHome.class, FooStatefulRemote.class);
        stateful.setHomeAndLocal(FooStatefulLocalHome.class, FooStatefulLocal.class);
        ejbJar.addEnterpriseBean(stateful);
        final org.apache.openejb.jee.EntityBean bean = new org.apache.openejb.jee.EntityBean(MyEntity.class, PersistenceType.BEAN);
        bean.setLocalHome(MyLocalHome.class.getName());
        bean.setLocal(MyLocal.class.getName());
        ejbJar.addEnterpriseBean(bean);
        final org.apache.openejb.jee.EntityBean bean1 = new org.apache.openejb.jee.EntityBean(YourEntity.class, PersistenceType.BEAN);
        bean1.setLocalHome(MyLocalHome.class.getName());
        bean1.setLocal(MyLocal.class.getName());
        ejbJar.addEnterpriseBean(bean1);
        final StatelessBean bar = new StatelessBean(BarStateless.class);
        bar.setHomeAndRemote(BarStatelessHome.class, BarStatelessRemote.class);
        ejbJar.addEnterpriseBean(bar);
        final StatefulBean bazStateful = new StatefulBean(BazStateful.class);
        ejbJar.addEnterpriseBean(bazStateful);
        return ejbJar;
    }

    private static interface FooStatelessHome extends jakarta.ejb.EJBHome {
    }

    private static interface FooStatelessRemote extends jakarta.ejb.EJBObject {
    }

    private static interface FooStatelessLocalHome extends jakarta.ejb.EJBLocalHome {
    }

    private static interface FooStatelessLocal extends jakarta.ejb.EJBLocalObject {
    }

    private static class FooStateless implements SessionBean {
        public void ejbCreate() {
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Override
        public void setSessionContext(final SessionContext arg0) throws EJBException, RemoteException {
        }
    }

    private static interface FooStatefulHome extends jakarta.ejb.EJBHome {
    }

    private static interface FooStatefulRemote extends jakarta.ejb.EJBObject {
    }

    private static interface FooStatefulLocalHome extends jakarta.ejb.EJBLocalHome {
    }

    private static interface FooStatefulLocal extends jakarta.ejb.EJBLocalObject {
    }

    private static class FooStateful implements SessionBean {
        public void ejbCreate() {
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Override
        public void setSessionContext(final SessionContext arg0) throws EJBException, RemoteException {
        }
    }

    private static interface MyLocalHome extends EJBLocalHome {
        public MyLocal create(Integer pk) throws CreateException;

        public MyLocal findByPrimaryKey(Integer pk) throws FinderException;
    }

    private static interface MyLocal extends EJBLocalObject {
    }

    private static class MyEntity implements EntityBean {
        public Integer ejbCreate(final Integer pk) throws CreateException {
            return null;
        }

        public void ejbPostCreate(final String str) {
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbLoad() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws RemoveException, EJBException, RemoteException {
        }

        @Override
        public void ejbStore() throws EJBException, RemoteException {
        }

        @Override
        public void setEntityContext(final EntityContext arg0) throws EJBException, RemoteException {
        }

        @Override
        public void unsetEntityContext() throws EJBException, RemoteException {
        }
    }

    private static class YourEntity implements EntityBean {
//        public Integer ejbCreate(Integer pk) throws CreateException {
//            return null;
//        }

//        public void ejbPostCreate(Integer pk) {}

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbLoad() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws RemoveException, EJBException, RemoteException {
        }

        @Override
        public void ejbStore() throws EJBException, RemoteException {
        }

        @Override
        public void setEntityContext(final EntityContext arg0) throws EJBException, RemoteException {
        }

        @Override
        public void unsetEntityContext() throws EJBException, RemoteException {
        }
    }

    private static interface BarStatelessHome extends jakarta.ejb.EJBHome {
        public BarStatelessRemote create() throws CreateException, RemoteException;
    }

    private static interface BarStatelessRemote extends jakarta.ejb.EJBObject {
    }

    private static class BarStateless implements SessionBean {

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Override
        public void setSessionContext(final SessionContext arg0) throws EJBException, RemoteException {
        }
    }

    private static interface BazStatefulHome extends jakarta.ejb.EJBHome {
        public BazStatefulRemote create() throws CreateException, RemoteException;
    }

    private static interface BazStatefulRemote extends jakarta.ejb.EJBObject {
    }

    private static interface BazStatefulLocalHome extends jakarta.ejb.EJBLocalHome {
        public BazStatefulLocal create() throws CreateException;
    }

    private static interface BazStatefulLocal extends jakarta.ejb.EJBLocalObject {
    }

    @RemoteHome(BazStatefulHome.class)
    @LocalHome(BazStatefulLocalHome.class)
    private static class BazStateful implements SessionBean {
        // missing ejbCreate method

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
        }

        @Override
        public void setSessionContext(final SessionContext arg0) throws EJBException, RemoteException {
        }
    }
}
