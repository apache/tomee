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
package org.apache.openejb.core;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.RemoveException;
import org.apache.openejb.InvalidateReferenceException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.entity.EntityContext;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.CmpVersion;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class EJBCreateExceptionTest {
    @AppResource
    private Context ctx;

    @Test
    public void checkEntityException() throws NamingException, RemoteException, CreateException {
        try {
            HotelEJBHome home = (HotelEJBHome) ctx.lookup("HelloEntityRemoteHome");
            HotelEJBObject bean = home.create("id", "name");
        } catch (Throwable t) {
            Assert.assertEquals(UndeclaredThrowableException.class, t.getClass());
            Assert.assertEquals(RemoteException.class, t.getCause().getClass());
            final RemoteException re = (RemoteException) t.getCause();
            Assert.assertEquals(RuntimeException.class, re.getCause().getClass());
        }

    }

    @Test
    public void checkStatelessException() {

        try {
            final HelloHome helloHome = (HelloHome) ctx.lookup("HelloEJBStatelessRemoteHome");
            final HelloRemote helloRemote = helloHome.create();
            helloRemote.sayHello("Test");
            Assert.fail("Expected RemoteException not thrown");
        } catch (NamingException e) {
            Assert.fail("Expected RemoteException not thrown");
        } catch (CreateException e) {
            Assert.fail("Expected RemoteException not thrown");
        } catch (RemoteException e) {
            Assert.assertFalse(e.getCause() instanceof InvalidateReferenceException);
            Assert.assertEquals(RemoteException.class, e.getCause().getClass());
            final RemoteException cause = (RemoteException) e.getCause();
            Assert.assertEquals(RuntimeException.class, cause.getCause().getClass());
        }
    }

    @Test
    public void checkStatefulException() {

        try {
            final HelloHome helloHome = (HelloHome) ctx.lookup("HelloEJBStatefulRemoteHome");
            final HelloRemote helloRemote = helloHome.create();
            helloRemote.sayHello("Test");
            Assert.fail("Expected RemoteException not thrown");
        } catch (NamingException e) {
            Assert.fail("Expected RemoteException not thrown");
        } catch (CreateException e) {
            Assert.fail("Expected RemoteException not thrown");
        } catch (RemoteException e) {
            Assert.assertFalse(e.getCause() instanceof InvalidateReferenceException);
            Assert.assertEquals(RuntimeException.class, e.getCause().getClass());
        }
    }

    @Module
    public EjbJar jar() throws OpenEJBException {
        final EjbJar ejbJar = new EjbJar();
        final org.apache.openejb.jee.SessionBean statelessSessionBean = new org.apache.openejb.jee.SessionBean();
        statelessSessionBean.setEjbName("HelloEJBStateless");
        statelessSessionBean.setEjbClass(HelloBeanStateless.class);
        statelessSessionBean.setHome(HelloHome.class.getName());
        statelessSessionBean.setRemote(HelloRemote.class.getName());
        statelessSessionBean.setSessionType(SessionType.STATELESS);
        ejbJar.addEnterpriseBean(statelessSessionBean);

        final SessionBean statefulSessionBean = new SessionBean();
        statefulSessionBean.setEjbName("HelloEJBStateful");
        statefulSessionBean.setEjbClass(HelloBeanStateful.class);
        statefulSessionBean.setHome(HelloHome.class.getName());
        statefulSessionBean.setRemote(HelloRemote.class.getName());
        statefulSessionBean.setSessionType(SessionType.STATEFUL);
        ejbJar.addEnterpriseBean(statefulSessionBean);

        final org.apache.openejb.jee.EntityBean entityBean = new org.apache.openejb.jee.EntityBean();
        entityBean.setEjbName("HelloEntity");
        entityBean.setEjbClass(HotelBean.class);
        entityBean.setHome(HotelEJBHome.class.getName());
        entityBean.setRemote(HotelEJBObject.class.getName());
        entityBean.setPersistenceType(PersistenceType.CONTAINER);
        entityBean.setReentrant(false);
        entityBean.setCmpVersion(CmpVersion.CMP2);
        entityBean.setPrimkeyField("hotelId");
        entityBean.getCmpField().add(new CmpField("hotelname"));
        ejbJar.addEnterpriseBean(entityBean);
        return ejbJar;
    }

    public interface HotelEJBHome extends jakarta.ejb.EJBHome {
        HotelEJBObject create(String id, String name) throws CreateException;

        HotelEJBObject findByPrimaryKey(String id);
    }

    public interface HotelEJBObject extends EJBObject {
        String getHotelId();

        void setHotelId(String hotelId);

        String getHotelname();

        void setHotelname(String hotelname);
    }

    public static abstract class HotelBean implements EntityBean {
        private static final long serialVersionUID = -1740314851444302078L;

        public String ejbCreate(String id, String name) throws CreateException {
            this.setHotelId(id);
            this.setHotelname(name);
            throw new RuntimeException("Boom");
        }

        public abstract String getHotelId();

        public abstract void setHotelId(String hotelId);

        public abstract String getHotelname();

        public abstract void setHotelname(String hotelname);

        public void ejbPostCreate(String str1, String str2)
                throws CreateException {
        }


        public void ejbRemove() throws RemoveException, EJBException,
                RemoteException {
            // no-op
        }

        public void ejbActivate() throws EJBException, RemoteException {
            // no-op
        }

        public void ejbLoad() throws EJBException, RemoteException {
            // no-op
        }

        public void ejbPassivate() throws EJBException, RemoteException {
            // no-op
        }

        public void ejbStore() throws EJBException, RemoteException {
            // no-op
        }

        public void setEntityContext(EntityContext arg0) throws EJBException,
                RemoteException {
            // no-op
        }

        public void unsetEntityContext() throws EJBException, RemoteException {
            // no-op
        }
    }

    public static class HelloBeanStateless implements jakarta.ejb.SessionBean {

        public String sayHello(String name) {
            return "Hello, " + name + " from EJB 2.0!";
        }

        public String sayHelloBoom(String name) {
            throw new RuntimeException("BOOM!");
        }

        public void ejbCreate() {
            throw new RuntimeException("BOOM!");
        }

        public void setSessionContext(jakarta.ejb.SessionContext ctx) {
        }

        public void ejbRemove() {
        }

        public void ejbActivate() {
        }

        public void ejbPassivate() {
        }
    }

    public static class HelloBeanStateful implements jakarta.ejb.SessionBean {

        private String lastNameUSed;

        public String sayHello(String name) {
            lastNameUSed = name;
            return "Hello, " + name + " from EJB 2.0!";
        }

        public String repeatLastGreeting() {
            return "Welcome back, " + lastNameUSed;
        }

        public String sayHelloBoom(String name) {
            throw new RuntimeException("BOOM!");
        }

        public void ejbCreate() {
            throw new RuntimeException("BOOM!");
        }

        public void setSessionContext(jakarta.ejb.SessionContext ctx) {
        }

        public void ejbRemove() {
        }

        public void ejbActivate() {
        }

        public void ejbPassivate() {
        }
    }

    public interface HelloHome extends jakarta.ejb.EJBHome {
        HelloRemote create() throws jakarta.ejb.CreateException, RemoteException;
    }

    public interface HelloRemote extends jakarta.ejb.EJBObject {
        String sayHello(String name) throws RemoteException;

        String sayHelloBoom(String name) throws RemoteException;
    }

}
