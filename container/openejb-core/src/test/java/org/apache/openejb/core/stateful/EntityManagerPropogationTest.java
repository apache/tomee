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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openjpa.persistence.ArgumentException;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import static javax.persistence.PersistenceContextType.EXTENDED;
import static javax.persistence.PersistenceContextType.TRANSACTION;
import java.io.IOException;

public class EntityManagerPropogationTest extends TestCase {

    public void testExtended() throws Exception {

        InitialContext ctx = new InitialContext();

        Node node = (Node) ctx.lookup("ExtendedLocal");

        // This bean should still be attached
        // when the transaction commits
        Color attached = node.create(1, "Red");

        while (node.getChild() != null) {

            assertTrue(node.contains(attached));

            node = node.getChild();
        }

    }

    /**
     * Test that the extended persistence context can
     * survive the removal of the parent
     * 
     * @throws Exception
     */
    public void testExtendedRemove() throws Exception {

        InitialContext ctx = new InitialContext();

        Node node = (Node) ctx.lookup("ExtendedLocal");

        // This bean should still be attached
        // when the transaction commits
        Color attached = node.create(1, "Red");

        while (node.getChild() != null) {

            assertTrue(node.contains(attached));

            Node next = node.getChild();

            node.remove();

            try {
                node.contains(attached);
                fail("The Stateful bean should have been removed");
            } catch (NoSuchEJBException e) {
                // good
            }
            node = next;
        }

    }

    /**
     * Test that two Stateful session bean siblings
     * do not share the same extended persistence context
     *
     * A stateful session bean must be a child in order
     * for the context to be propogated to that bean.
     *
     * @throws Exception
     */
    public void testNotTooExtended() throws Exception {

        InitialContext ctx = new InitialContext();

        // Stateful Node tree A and Node tree B are syblings
        Node chainA = (Node) ctx.lookup("ExtendedLocal");

        Node chainB = (Node) ctx.lookup("ExtendedLocal");

        // This bean should still be attached
        // when the transaction commits
        Color attachedA = chainA.create(1, "Red");

        while (chainB.getChild() != null) {

            assertFalse(chainB.contains(attachedA));

            chainB = chainB.getChild();
        }

    }

    public void testTransaction() throws Exception {

        InitialContext ctx = new InitialContext();

        Node node = (Node) ctx.lookup("TransactionLocal");

        // This bean should not still be attached
        // when the transaction commits
        Color detached = node.create(2, "Blue");

        while (node.getChild() != null) {

            assertFalse(node.contains(detached));

            node = node.getChild();
        }

    }

    public void setUp() throws OpenEJBException, IOException, NamingException {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        System.setProperty("openejb.embedded", "true");

        // Boot up the minimum required OpenEJB components
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Start creating the test application 

        // Create an ejb-jar.xml for this app
        EjbJar ejbJar = new EjbJar();

        // Add six beans and link them all in a chain
        addStatefulBean(ejbJar, ExtendedContextBean.class, "Extended", "Extendedx2");
        addStatefulBean(ejbJar, ExtendedContextBean.class, "Extendedx2", "Extendedx3");
        addStatefulBean(ejbJar, ExtendedContextBean.class, "Extendedx3", "Extendedx4");
        addStatefulBean(ejbJar, ExtendedContextBean.class, "Extendedx4", "Extendedx5");
        addStatefulBean(ejbJar, ExtendedContextBean.class, "Extendedx5", "Extendedx6");
        ejbJar.addEnterpriseBean(new StatefulBean("Extendedx6", EndNodeBean.class));

        // Add six beans and link them all in a chain
        addStatefulBean(ejbJar, TransactionContextBean.class, "Transaction", "Transactionx2");
        addStatefulBean(ejbJar, TransactionContextBean.class, "Transactionx2", "Transactionx3");
        addStatefulBean(ejbJar, TransactionContextBean.class, "Transactionx3", "Transactionx4");
        addStatefulBean(ejbJar, TransactionContextBean.class, "Transactionx4", "Transactionx5");
        addStatefulBean(ejbJar, TransactionContextBean.class, "Transactionx5", "Transactionx6");
        ejbJar.addEnterpriseBean(new StatefulBean("Transactionx6", EndNodeBean.class));

        ejbJar.setAssemblyDescriptor(new AssemblyDescriptor());
        ejbJar.getAssemblyDescriptor().addApplicationException(IllegalArgumentException.class, false);
        ejbJar.getAssemblyDescriptor().addApplicationException(ArgumentException.class, false);

        EjbModule ejbModule = new EjbModule(ejbJar);

        // Create an "ear"
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), "test-app");

        // Add the ejb-jar.xml to the ear
        appModule.getEjbModules().add(ejbModule);

        // Create a persistence-unit for this app
        PersistenceUnit unit = new PersistenceUnit("testUnit");
        unit.getClazz().add(Color.class.getName());
        unit.setProperties(new org.apache.openejb.jee.jpa.unit.Properties());
        unit.getProperties().setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");

        // Add the persistence.xml to the "ear"
        appModule.getPersistenceModules().add(new PersistenceModule("root", new Persistence(unit)));

        // Configure and assemble the ear -- aka. deploy it
        AppInfo info = config.configureApplication(appModule);
        assembler.createApplication(info);
    }

    private void addStatefulBean(EjbJar ejbJar, Class<?> ejbClass, String name, String reference) {
        StatefulBean bean = ejbJar.addEnterpriseBean(new StatefulBean(name, ejbClass));
        bean.getEjbLocalRef().add(new EjbLocalRef("child", reference));
    }

    @Entity
    public static class Color {

        @Id
        private int id;

        private String name;

        public Color() {
        }

        public Color(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static interface Node {

        void remove();
        
        Color create(int id, String name);

        boolean contains(Color bean);

        Node getChild();
    }


    /**
     * The root stateful session bean is essentially the "owner"
     * of the persistence context and all children downstream
     * with an EXTENDED persistence context should share the same
     * persistence context and underlying EntityManager
     * <p/>
     * Transactions are dissabled to ensure that we aren't relying
     * on the JTA propogation that also exists with an EXTENDED
     * persistence context
     */

    public static class ExtendedContextBean extends NodeBean {

        @PersistenceContext(unitName = "testUnit", type = EXTENDED)
        protected EntityManager context;

        protected EntityManager getEntityManager() {
            return context;
        }
    }

    public static class TransactionContextBean extends NodeBean {

        @PersistenceContext(unitName = "testUnit", type = TRANSACTION)
        protected EntityManager context;

        protected EntityManager getEntityManager() {
            return context;
        }
    }

    public static abstract class NodeBean implements Node {

        @EJB(name = "child")
        protected Node child;

        public Color create(int id, String name) {
            Color color = new Color(id, name);
            getEntityManager().persist(color);
            return color;
        }

        protected abstract EntityManager getEntityManager();

        public Node getChild() {
            return child;
        }

        public boolean contains(Color bean) throws IllegalArgumentException {
            // This call should not fail as the bean was created
            // via the extended persistence context we are now in
            return getEntityManager().contains(bean);
        }

        @Remove
        public void remove(){}

    }

    public static class EndNodeBean implements Node {

        public boolean contains(Color color) {
            return false;
        }

        public Color create(int id, String name) {
            return null;
        }

        public Node getChild() {
            return null;
        }

        @Remove
        public void remove(){}

    }

}
