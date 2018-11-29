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
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
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
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.junit.AfterClass;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.LocalHome;
import javax.ejb.RemoteHome;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;
import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class LegacyInterfaceTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(MySingletonBean.class));
        ejbJar.addEnterpriseBean(new EntityBean(MyBmpBean.class, PersistenceType.BEAN));

        //<entity>
        //  <ejb-name>License</ejb-name>
        //  <local-home>org.apache.openejb.test.entity.cmr.onetoone.LicenseLocalHome</local-home>
        //  <local>org.apache.openejb.test.entity.cmr.onetoone.LicenseLocal</local>
        //  <ejb-class>org.apache.openejb.test.entity.cmr.onetoone.LicenseBean</ejb-class>
        //  <persistence-type>Container</persistence-type>
        //  <prim-key-class>java.lang.Integer</prim-key-class>
        //  <reentrant>false</reentrant>
        //  <cmp-version>2.x</cmp-version>
        //  <abstract-schema-name>License</abstract-schema-name>
        //  <cmp-field>
        //    <field-name>id</field-name>
        //  </cmp-field>
        //  <cmp-field>
        //    <field-name>number</field-name>
        //  </cmp-field>
        //  <cmp-field>
        //    <field-name>points</field-name>
        //  </cmp-field>
        //  <cmp-field>
        //    <field-name>notes</field-name>
        //  </cmp-field>
        //  <primkey-field>id</primkey-field>
        //  <query>
        //    <!-- CompondPK one-to-one shares the local home interface so we need to declare this useless finder -->
        //    <query-method>
        //      <method-name>findByPrimaryKey</method-name>
        //      <method-params>
        //        <method-param>org.apache.openejb.test.entity.cmr.onetoone.LicensePk</method-param>
        //      </method-params>
        //    </query-method>
        //    <ejb-ql>SELECT OBJECT(DL) FROM License DL</ejb-ql>
        //  </query>
        //</entity>

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

        //<container-transaction>
        //  <method>
        //    <ejb-name>MyBean</ejb-name>
        //    <method-name>*</method-name>
        //  </method>
        //  <trans-attribute>Supports</trans-attribute>
        //</container-transaction>

        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, null, "MyBmpBean", "*"));
        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, null, "MyCmpBean", "*"));
        transactions.add(new ContainerTransaction(TransAttribute.SUPPORTS, null, "MySingletonBean", "*"));

        final File f = new File("test").getAbsoluteFile();
        if (!f.exists() && !f.mkdirs()) {
            throw new Exception("Failed to create test directory: " + f);
        }

        final AppModule module = new AppModule(this.getClass().getClassLoader(), f.getAbsolutePath());
        module.getEjbModules().add(new EjbModule(ejbJar));
        assembler.createApplication(config.configureApplication(module));

    }

    public void testCustomCmpMappings() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

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

        final Entity entity = new Entity();
        entity.setClazz("openejb.org.apache.openejb.core.MyCmpBean");
        entity.setName("MyCmpBean");
        entity.setDescription("MyCmpBean");
        entity.setAttributes(new Attributes());

        final NamedQuery namedQuery = new NamedQuery();
        namedQuery.setQuery("SELECT OBJECT(DL) FROM License DL");
        entity.getNamedQuery().add(namedQuery);

        final Id id = new Id();
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

        final AppModule module = new AppModule(this.getClass().getClassLoader(), f.getAbsolutePath());
        final EjbModule ejbModule = new EjbModule(ejbJar);
        ejbModule.getAltDDs().put("openejb-cmp-orm.xml", entityMappings);
        module.getEjbModules().add(ejbModule);

        assertNull(module.getCmpMappings());
        assembler.createApplication(config.configureApplication(module));
        assertNotNull(module.getCmpMappings());
        final List<Basic> basicList = module.getCmpMappings().getEntityMap().get("openejb.org.apache.openejb.core.MyCmpBean").getAttributes().getBasic();
        assertEquals(1, basicList.size());
        assertEquals(300, basicList.get(0).getColumn().getLength().intValue());
        assertEquals("wNAME", basicList.get(0).getColumn().getName());
    }

    public void testCustomCmpMappingsWithMappingFileDefinedInPersistenceXml() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

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

        final AppModule module = new AppModule(this.getClass().getClassLoader(), f.getAbsolutePath());
        final EjbModule ejbModule = new EjbModule(ejbJar);

        Persistence persistence = new Persistence();
        PersistenceUnit pu = persistence.addPersistenceUnit("cmp");
        pu.setTransactionType(TransactionType.JTA);
        pu.setJtaDataSource("fake");
        pu.setNonJtaDataSource("fake");
        pu.getMappingFile().add("test-orm.xml");
        pu.getClazz().add("openejb.org.apache.openejb.core.MyCmpBean");
        module.addPersistenceModule(new PersistenceModule("pu", persistence));

        module.getEjbModules().add(ejbModule);

        assertNull(module.getCmpMappings());
        assembler.createApplication(config.configureApplication(module));
        assertNotNull(module.getCmpMappings());

        // no mapping should be automatically generated
        assertTrue(module.getCmpMappings().getEntityMap().isEmpty());

        // pu should not be modified, no duplicate classes
        assertEquals(1, pu.getClazz().size());
        assertEquals("openejb.org.apache.openejb.core.MyCmpBean", pu.getClazz().get(0));
        assertEquals(1, pu.getMappingFile().size());
        assertEquals("test-orm.xml", pu.getMappingFile().get(0));
    }

    @LocalHome(MyLocalHome.class)
    @RemoteHome(MyRemoteHome.class)
    public static abstract class MyCmpBean implements javax.ejb.EntityBean {

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
    public class MyBmpBean implements javax.ejb.EntityBean {

        public void doit() {
        }

        public java.util.Collection ejbFindEmptyCollection() throws javax.ejb.FinderException, java.rmi.RemoteException {
            return new java.util.Vector();
        }

        public Integer ejbFindByPrimaryKey(final Integer primaryKey) throws javax.ejb.FinderException {
            return new Integer(-1);
        }

        public Integer ejbCreateObject(final String name) throws javax.ejb.CreateException {
            return new Integer(-1);
        }

        public void ejbPostCreateObject(final String name) throws javax.ejb.CreateException {
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

    public interface MyRemoteHome extends javax.ejb.EJBHome {

        public MyRemoteObject createObject(String name)
            throws javax.ejb.CreateException, java.rmi.RemoteException;

        public MyRemoteObject findByPrimaryKey(Integer primarykey)
            throws javax.ejb.FinderException, java.rmi.RemoteException;

        public java.util.Collection findEmptyCollection()
            throws javax.ejb.FinderException, java.rmi.RemoteException;

    }

    public interface MyRemoteObject extends javax.ejb.EJBObject {

        public void doit() throws RemoteException;

    }

    public interface MyLocalHome extends javax.ejb.EJBLocalHome {

        public MyLocalObject createObject(String name)
            throws javax.ejb.CreateException;

        public MyLocalObject findByPrimaryKey(Integer primarykey)
            throws javax.ejb.FinderException;

        public java.util.Collection findEmptyCollection()
            throws javax.ejb.FinderException;

    }

    public interface MyLocalObject extends javax.ejb.EJBLocalObject {

        public void doit();

    }

    @LocalHome(MySessionLocalHome.class)
    @RemoteHome(MySessionRemoteHome.class)
    public static class MySingletonBean implements javax.ejb.SessionBean {

        public void doit() {
        }

        public void ejbCreateObject() throws javax.ejb.CreateException {
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

    public interface MySessionRemoteHome extends javax.ejb.EJBHome {
        public MySessionRemoteObject createObject()
            throws javax.ejb.CreateException, java.rmi.RemoteException;
    }

    public interface MySessionRemoteObject extends javax.ejb.EJBObject {
        public void doit();
    }

    public interface MySessionLocalHome extends javax.ejb.EJBLocalHome {
        public MySessionLocalObject createObject()
            throws javax.ejb.CreateException;
    }

    public interface MySessionLocalObject extends javax.ejb.EJBLocalObject {
        public void doit();
    }

}
