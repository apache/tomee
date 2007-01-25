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
package org.apache.openejb.persistence;

import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Map;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
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

    public void testNothing() {}

    public void _testDeployer() throws Exception {
        String jndiPrefix = "java:openejb/PersistenceFactories";
        try {
            ClassLoader cl = this.getClass().getClassLoader();

            // Get a EntityManagerFactory list
            PersistenceDeployer pm = new PersistenceDeployer(new GlobalJndiDataSourceResolver(null), null);
            Map<String, EntityManagerFactory> factories = pm.deploy(cl);
            for (Map.Entry<String, EntityManagerFactory> entry : factories.entrySet()) {
                // Store EntityManagerFactory in the JNDI
                String name = jndiPrefix + "/" + entry.getKey();
                bind(name, entry.getValue(), ctx);
            }

            EntityManagerFactory emf = (EntityManagerFactory) ctx.lookup(jndiPrefix + "/TestUnit");
            assertNotNull(emf);

            assertEntityManagerFactory(emf);
        } finally {
            cleanupJNDI(jndiPrefix + "/TestUnit");
        }
    }

    private void bind(String name, Object obj, Context ctx) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);

        CompositeName composite = new CompositeName(name);
        if (composite.size() > 1) {
            for (int i = 0; i < composite.size() - 1; i++) {
                try {
                    Object ctxObj = ctx.lookup(composite.get(i));
                    if (!(ctxObj instanceof Context)) {
                        throw new NamingException("Invalid JNDI path.");
                    }
                    ctx = (Context) ctxObj;
                } catch (NameNotFoundException e) {
                    // Name was not found, so add a new subcontext
                    ctx = ctx.createSubcontext(composite.get(i));
                }
            }
        }

        ctx.bind(composite.get(composite.size() - 1), obj);
    }

    private void assertEntityManagerFactory(EntityManagerFactory emf) {

        EntityManager em = emf.createEntityManager();
        if (!(em instanceof FakeEntityManager))
            fail("EntityManager is not a FakeEntityManager!");

        PersistenceUnitInfo pu = ((FakeEntityManagerFactory) emf).getPersistenceUnitInfo();
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
        assertTrue(managedClasses.contains("org.apache.openejb.persistence.TestClass"));
        assertTrue(managedClasses.contains("org.apache.openejb.persistence.TestClass2"));

    }

    private void cleanupJNDI(String jndi) throws Exception {
        CompositeName composite = new CompositeName(jndi);
        for (int i = composite.size(); i > 0; i--) {
            try {
                Object value = ctx.lookup(composite.getPrefix(i).toString());
                if (value instanceof Context) {
                    Object parent = ctx;
                    if (i > 1)
                        parent = ctx.lookup(composite.getPrefix(i - 1).toString());
                    ((Context) parent).destroySubcontext(composite.get(i - 1));
                } else
                    ctx.unbind(composite.getPrefix(i).toString());
            } catch (NamingException e) {
                // Just ignore it
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Set up a fake JNDI instance
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.persistence.JNDIContextFactory");
        previousFactory = System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.openejb.persistence.JNDIContextFactory");

        ctx = new InitialContext(env);

        ctx.createSubcontext("jdbc").bind("MyDataSource", ds);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        cleanupJNDI(DATASOURCE_NAME);
        if (previousFactory != null)
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, previousFactory);
        else
            System.getProperties().remove(Context.INITIAL_CONTEXT_FACTORY);
    }

}
