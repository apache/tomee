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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
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
import org.apache.openejb.test.entity.cmr.cmrmapping.OneInverseSideBean;
import org.apache.openejb.test.entity.cmr.cmrmapping.OneInverseSideLocal;
import org.apache.openejb.test.entity.cmr.cmrmapping.OneInverseSideLocalHome;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.LocalHome;
import javax.ejb.RemoteHome;
import javax.ejb.RemoveException;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class LegacyInterfaceTest extends TestCase {

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
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

        EntityBean cmp = ejbJar.addEnterpriseBean(new EntityBean(MyCmpBean.class, PersistenceType.CONTAINER));
        cmp.setPrimKeyClass(Integer.class.getName());
        cmp.setPrimkeyField("id");
        cmp.getCmpField().add(new CmpField("id"));
        cmp.getCmpField().add(new CmpField("name"));
        Query query = new Query();
        query.setQueryMethod(new QueryMethod("findByPrimaryKey", Integer.class.getName()));
        query.setEjbQl("SELECT OBJECT(DL) FROM License DL");
        cmp.getQuery().add(query);
        List<ContainerTransaction> transactions = ejbJar.getAssemblyDescriptor().getContainerTransaction();

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

        AppModule module = new AppModule(this.getClass().getClassLoader(), "test");
        module.getEjbModules().add(new EjbModule(ejbJar));
        assembler.createApplication(config.configureApplication(module));

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

        public Integer ejbCreateObject(String id) throws CreateException {
            return null;
        }

        public void ejbPostCreateObject(String id) {
        }

        public void setEntityContext(EntityContext ctx) {
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

        public Integer ejbFindByPrimaryKey(Integer primaryKey) throws javax.ejb.FinderException {
            return new Integer(-1);
        }

        public Integer ejbCreateObject(String name) throws javax.ejb.CreateException {
            return new Integer(-1);
        }

        public void ejbPostCreateObject(String name) throws javax.ejb.CreateException {
        }


        public void ejbLoad() throws EJBException, RemoteException {
        }

        public void setEntityContext(EntityContext entityContext) throws EJBException, RemoteException {
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

        public void doit(){}
        
        public void ejbCreateObject() throws javax.ejb.CreateException {
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
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
