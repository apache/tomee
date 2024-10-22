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
package org.apache.openejb.core.webservices;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.Query;
import org.apache.openejb.jee.QueryMethod;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EntityContext;
import jakarta.ejb.LocalHome;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.RemoveException;
import jakarta.ejb.SessionContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

@RunWith(ApplicationComposer.class)
public class JPACMDIntegrationTest {

    @jakarta.persistence.PersistenceUnit
    private EntityManagerFactory emf;


    @Module
    public Persistence persistence() throws Exception {
        final PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(User.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.getProperties().setProperty("openjpa.DatCache", "false");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new org.apache.openejb.jee.jpa.unit.Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }


    @Module
    public EjbModule ejbModule() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(MySingletonBean.class));
        ejbJar.addEnterpriseBean(new EntityBean(MyBmpBean.class, PersistenceType.BEAN));

        final EntityBean cmp = ejbJar.addEnterpriseBean(new EntityBean(MyCmpBean.class, PersistenceType.CONTAINER));
        cmp.setPrimKeyClass(Integer.class.getName());
        cmp.setPrimkeyField("id");
        cmp.getCmpField().add(new CmpField("id"));
        cmp.getCmpField().add(new CmpField("name"));
        final Query query = new Query();
        query.setQueryMethod(new QueryMethod("findByPrimaryKey", Integer.class.getName()));
        query.setEjbQl("SELECT OBJECT(DL) FROM License DL");
        cmp.getQuery().add(query);
        final List<ContainerTransaction> transactions = ejbJar.getAssemblyDescriptor().getContainerTransaction();

        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, null, "MyBmpBean", "*"));
        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, null, "MyCmpBean", "*"));
        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, null, "MySingletonBean", "*"));

        final File f = new File("test").getAbsoluteFile();
        if (!f.exists() && !f.mkdirs()) {
            throw new Exception("Failed to create test directory: " + f);
        }

        final EntityMappings entityMappings = new EntityMappings();

        final org.apache.openejb.jee.jpa.Entity entity = new org.apache.openejb.jee.jpa.Entity();
        entity.setClazz("openejb.org.apache.openejb.core.MyCmpBean");
        entity.setName("MyCmpBean");
        entity.setDescription("MyCmpBean");
        entity.setAttributes(new Attributes());

        final NamedQuery namedQuery = new NamedQuery();
        namedQuery.setQuery("SELECT OBJECT(DL) FROM License DL");
        entity.getNamedQuery().add(namedQuery);

        final org.apache.openejb.jee.jpa.Id id = new org.apache.openejb.jee.jpa.Id();
        id.setName("id");
        entity.getAttributes().getId().add(id);

        final Basic basic = new Basic();
        basic.setName("name");
        final Column column = new Column();
        column.setName("wNAME");
        column.setLength(300);
        basic.setColumn(column);
        entity.getAttributes().getBasic().add(basic);

        entityMappings.getEntity().add(entity);

        return new EjbModule(ejbJar);
    }


    @Test
    public void shouldCreateEntityMapper() {
        EntityManager entityManager = emf.createEntityManager();

        User user = new User();
        user.id = "id";
        user.name = "ada";
        entityManager.merge(user);

        User user1 = entityManager.find(User.class, "id");
        Assert.assertNotNull(user1);
        System.out.println(user1);

    }

    @jakarta.persistence.Entity
    public static class User {

        @jakarta.persistence.Id
        private String id;

        @jakarta.persistence.Column
        private String name;
    }


    @LocalHome(MyLocalHome.class)
    @RemoteHome(MyRemoteHome.class)
    public static abstract class MyCmpBean implements jakarta.ejb.EntityBean {

        // CMP
        public abstract Integer getId();

        public abstract void setId(Integer id);

        public abstract String getName();

        public abstract void setName(String number);

        public void doit() {
        }

        public Integer ejbCreateObject(final String id) throws CreateException {
            return null;
        }

        public void ejbPostCreateObject(final String id) {
        }

        public void setEntityContext(final EntityContext ctx) {
        }

        public void unsetEntityContext() {
        }

        public void ejbActivate() {
        }

        public void ejbPassivate() {
        }

        public void ejbLoad() {
        }

        public void ejbStore() {
        }

        public void ejbRemove() throws RemoveException {
        }
    }

    @LocalHome(MyLocalHome.class)
    @RemoteHome(MyRemoteHome.class)
    public class MyBmpBean implements jakarta.ejb.EntityBean {

        public void doit() {
        }

        public java.util.Collection ejbFindEmptyCollection() throws jakarta.ejb.FinderException, java.rmi.RemoteException {
            return new java.util.Vector();
        }

        public Integer ejbFindByPrimaryKey(final Integer primaryKey) throws jakarta.ejb.FinderException {
            return -1;
        }

        public Integer ejbCreateObject(final String name) throws jakarta.ejb.CreateException {
            return -1;
        }

        public void ejbPostCreateObject(final String name) throws jakarta.ejb.CreateException {
        }


        public void ejbLoad() throws EJBException, RemoteException {
        }

        public void setEntityContext(final EntityContext entityContext) throws EJBException, RemoteException {
        }

        public void unsetEntityContext() throws EJBException, RemoteException {
        }

        public void ejbStore() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws RemoveException, EJBException, RemoteException {
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }
    }

    public interface MyRemoteHome extends jakarta.ejb.EJBHome {

        MyRemoteObject createObject(String name)
                throws jakarta.ejb.CreateException, java.rmi.RemoteException;

        MyRemoteObject findByPrimaryKey(Integer primarykey)
                throws jakarta.ejb.FinderException, java.rmi.RemoteException;

        java.util.Collection findEmptyCollection()
                throws jakarta.ejb.FinderException, java.rmi.RemoteException;

    }

    public interface MyRemoteObject extends jakarta.ejb.EJBObject {

        public void doit() throws RemoteException;

    }

    public interface MyLocalHome extends jakarta.ejb.EJBLocalHome {

        public MyLocalObject createObject(String name)
                throws jakarta.ejb.CreateException;

        public MyLocalObject findByPrimaryKey(Integer primarykey)
                throws jakarta.ejb.FinderException;

        public java.util.Collection findEmptyCollection()
                throws jakarta.ejb.FinderException;

    }

    public interface MyLocalObject extends jakarta.ejb.EJBLocalObject {

        public void doit();

    }

    @LocalHome(MySessionLocalHome.class)
    @RemoteHome(MySessionRemoteHome.class)
    public static class MySingletonBean implements jakarta.ejb.SessionBean {

        public void doit() {
        }

        public void ejbCreateObject() throws jakarta.ejb.CreateException {
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(final SessionContext sessionContext) throws EJBException, RemoteException {
        }
    }

    public interface MySessionRemoteHome extends jakarta.ejb.EJBHome {
         MySessionRemoteObject createObject()
                throws jakarta.ejb.CreateException, java.rmi.RemoteException;
    }

    public interface MySessionRemoteObject extends jakarta.ejb.EJBObject {
        void doit();
    }

    public interface MySessionLocalHome extends jakarta.ejb.EJBLocalHome {
         MySessionLocalObject createObject()
                throws jakarta.ejb.CreateException;
    }

    public interface MySessionLocalObject extends jakarta.ejb.EJBLocalObject {
        public void doit();
    }



}
