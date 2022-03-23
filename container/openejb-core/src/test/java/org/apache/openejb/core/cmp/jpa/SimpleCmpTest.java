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
package org.apache.openejb.core.cmp.jpa;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.core.entity.EntityContext;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.RemoveException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class SimpleCmpTest {
    @AppResource
    private Context ctx;

    @Test
    public void checkNoLeak() throws NamingException, RemoteException {
        TestBeanEJBHome home = (TestBeanEJBHome) ctx.lookup("TestBeanRemoteHome");
        TestBeanEJBObject bean = home.create();

        bean.check(bean.createHotelBean(0)); // the bean has the asserts
    }

    @Module
    public EjbJar jar() throws OpenEJBException {
        return ReadDescriptors.readEjbJar(new ByteArrayInputStream(
                ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ejb-jar xsi:schemaLocation=\"" +
                        "http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xmlns=\"http://java.sun.com/xml/ns/j2ee\" version=\"2.1\" id=\"ejb-jar_ID\">\n" +
                        "<display-name>OMEApp</display-name>\n" +
                        "<enterprise-beans>\n" +
                        "<entity id=\"ContainerManagedEntity_1\">\n" +
                        "<ejb-name>HotelBean</ejb-name>\n" +
                        "<local-home>" + HotelEJBLocalHome.class.getName() + "</local-home>\n" +
                        "<local>" + HotelEJBLocalObject.class.getName() + "</local>\n" +
                        "<ejb-class>" + HotelBean.class.getName() + "</ejb-class>\n" +
                        "<persistence-type>Container</persistence-type>\n" +
                        "<prim-key-class>java.lang.String</prim-key-class>\n" +
                        "<reentrant>false</reentrant>\n" +
                        "<cmp-version>2.x</cmp-version>\n" +
                        "<abstract-schema-name>HotelBean</abstract-schema-name>\n" +
                        "<cmp-field id=\"HotelBean_primKey\">\n" +
                        "<field-name>hotelId</field-name>\n" +
                        "</cmp-field>\n" +
                        "<cmp-field id=\"HotelBean_name\">\n" +
                        "<field-name>hotelname</field-name>\n" +
                        "</cmp-field>\n" +
                        "<primkey-field>hotelId</primkey-field>\n" +
                        "</entity>\n" +
                        "<session>\n" +
                        "<ejb-name>TestBean</ejb-name>\n" +
                        "<home>" + TestBeanEJBHome.class.getName() + "</home>\n" +
                        "<remote>" + TestBeanEJBObject.class.getName() + "</remote>\n" +
                        "<local-home>" + TestBeanEJBLocalHome.class.getName() + "</local-home>\n" +
                        "<local>" + TestBeanEJBLocalObject.class.getName() + "</local>\n" +
                        "<ejb-class>" + TestBean.class.getName() + "</ejb-class>\n" +
                        "<session-type>Stateless</session-type>\n" +
                        "<transaction-type>Container</transaction-type>\n" +
                        "<ejb-local-ref>\n" +
                        "<ejb-ref-name>ejb/HotelEJBLocalHome</ejb-ref-name>\n" +
                        "<ejb-ref-type>Entity</ejb-ref-type>\n" +
                        "<local-home>" + HotelEJBLocalHome.class.getName() + "</local-home>\n" +
                        "<local>" + HotelEJBLocalObject.class.getName() + "</local>\n" +
                        "</ejb-local-ref>\n" +
                        "</session>\n" +
                        "</enterprise-beans>\n" +
                        "</ejb-jar>").getBytes()));
    }

    public interface HotelEJBLocalHome extends jakarta.ejb.EJBLocalHome {
        HotelEJBLocalObject create(String id, String name) throws CreateException;
        HotelEJBLocalObject findByPrimaryKey(String id);
    }

    public interface TestBeanEJBHome extends jakarta.ejb.EJBHome {
        TestBeanEJBObject create() throws RemoteException;
    }

    public interface TestBeanEJBLocalHome extends jakarta.ejb.EJBLocalHome {

        TestBeanEJBLocalObject create();
    }

    public interface TestBeanEJBLocalObject extends jakarta.ejb.EJBLocalObject {
        void createHotelBean(int num) throws CreateException;
    }

    public static class TestBean implements SessionBean {

        private static final long serialVersionUID = 1L;
        protected SessionContext sessionContext;
        private HotelEJBLocalHome hotel;

        public void ejbCreate() {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(SessionContext arg0) throws EJBException,
                RemoteException {
            this.sessionContext = arg0;
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public Object createHotelBean(int num) throws CreateException {
            hotel = (HotelEJBLocalHome) this.sessionContext.lookup("ejb/HotelEJBLocalHome");
            final String pk = hotel.create(String.valueOf(num), "Some Hotel").getPrimaryKey().toString();
            assertNotNull(hotel.findByPrimaryKey(pk));
            return pk;
        }

        public void check(final String pk) {
            hotel = (HotelEJBLocalHome) this.sessionContext.lookup("ejb/HotelEJBLocalHome");

            // main part of the test is there
            final EntityEjbHomeHandler handler = EntityEjbHomeHandler.class.cast(Proxy.getInvocationHandler(hotel));
            try {
                final Object registry = handler.getBeanContext().get(
                        Thread.currentThread().getContextClassLoader() // private so use reflection
                                .loadClass("org.apache.openejb.core.ivm.BaseEjbProxyHandler$ProxyRegistry"));
                assertNull(registry); // not even instantiated since we have a wrapper (stateless)
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }

            // ensure we didn't deleted the entry
            assertNotNull(hotel.findByPrimaryKey(pk));
        }
    }

    public interface HotelEJBLocalObject extends EJBLocalObject {

        java.lang.String getHotelId();

        void setHotelId(java.lang.String hotelId);

        String getHotelname();

        void setHotelname(String hotelname);
    }

    public interface TestBeanEJBObject extends jakarta.ejb.EJBObject {
        String createHotelBean(int num) throws RemoteException;
        void check(String pk) throws RemoteException;
    }

    public static abstract class HotelBean implements EntityBean {
        private static final long serialVersionUID = -1740314851444302078L;


        public String ejbCreate(String id, String name) throws CreateException {
            this.setHotelId(id);
            this.setHotelname(name);
            return null;
        }

        public abstract java.lang.String getHotelId();

        public abstract void setHotelId(java.lang.String hotelId);

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
}
