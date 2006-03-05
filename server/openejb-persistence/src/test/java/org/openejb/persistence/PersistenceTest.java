/**
 * 
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openejb.persistence;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import junit.framework.TestCase;

public class PersistenceTest extends TestCase {

    private static final String DATASOURCE_NAME = "jdbc/MyDataSource";

    private DataSource ds = new FakeDataSource(); 

    private Context ctx = null;

    private String previousFactory = null;

    public void testParser() throws Exception {

        try {
            ClassLoader cl = this.getClass().getClassLoader();

            // Load the META-INF/persistence.xml from the classloader
            URL url = cl.getResource("META-INF/persistence.xml");

            // Get a EntityManagerFactory list
            PersistenceDeployer pm = new PersistenceDeployer();
            pm.loadPersistence(cl, url);

            EntityManagerFactory emf = (EntityManagerFactory) ctx
                    .lookup(PersistenceDeployer.FACTORY_JNDI_ROOT + "/TestUnit");
            assertNotNull(emf);

            assertEntityManagerFactory(emf);
        } finally {
            ctx.unbind(PersistenceDeployer.FACTORY_JNDI_ROOT + "/TestUnit");
        }
    }

    public void testDeployer() throws Exception {
        try {
            ClassLoader cl = this.getClass().getClassLoader();

            // Get a EntityManagerFactory list
            PersistenceDeployer pm = new PersistenceDeployer();
            pm.deploy(cl);

            EntityManagerFactory emf = (EntityManagerFactory) ctx
                    .lookup(PersistenceDeployer.FACTORY_JNDI_ROOT + "/TestUnit");
            assertNotNull(emf);

            assertEntityManagerFactory(emf);
        } finally {
            ctx.unbind(PersistenceDeployer.FACTORY_JNDI_ROOT + "/TestUnit");
        }
    }

    private void assertEntityManagerFactory(EntityManagerFactory emf) {

        EntityManager em = emf.createEntityManager();
        if (!(em instanceof FakeEntityManager))
            fail("EntityManager is not a FakeEntityManager!");

        PersistenceUnitInfo pu = ((FakeEntityManagerFactory) emf)
                .getPersistenceUnitInfo();
        assertNotNull(pu);

        Properties props = pu.getProperties();
        assertEquals("true", props.getProperty("test.property"));
        assertEquals("false", props.getProperty("test.property2"));

        // Datasource same as in JNDI?
        assertEquals(ds, pu.getJtaDataSource());

        List<String> mappingFiles = pu.getMappingFileNames();
        assertTrue(mappingFiles.contains("ormap.xml"));
        assertTrue(mappingFiles.contains("ormap2.xml"));

        List<String> managedClasses = pu.getManagedClassNames();
        assertTrue(managedClasses.contains("org.openejb.persistence.TestClass"));
        assertTrue(managedClasses
                .contains("org.openejb.persistence.TestClass2"));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Set up a fake JNDI instance
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.openejb.persistence.JNDIContextFactory");
        previousFactory = System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.openejb.persistence.JNDIContextFactory");

        ctx = new InitialContext(env);

        ctx.bind(DATASOURCE_NAME, ds);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if (previousFactory != null)
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                            previousFactory);
    }

}
