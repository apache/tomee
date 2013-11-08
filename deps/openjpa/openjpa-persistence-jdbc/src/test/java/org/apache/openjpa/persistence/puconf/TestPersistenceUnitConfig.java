/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.puconf;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.PersistenceTestCase;

public class TestPersistenceUnitConfig extends PersistenceTestCase {
    private String persistenceXmlResource;

    public TestPersistenceUnitConfig() {
        persistenceXmlResource = getClass().getPackage().getName().replaceAll("\\.", "/") + 
        "/META-INF/persistence.xml";
    }

    public EntityManagerFactory createEmf(String unitName) {
        return OpenJPAPersistence.createEntityManagerFactory(unitName, persistenceXmlResource);
    }

    public void testCreateEMFWithGoodPU() {
        EntityManagerFactory emf = null;
        try {
            emf = createEmf("PUTest-Good");         
            assertNotNull("Assert emf was successfully created.", emf);
        } finally {
            if (emf != null) {
                try {
                    emf.close();
                } catch (Throwable t) {
                    // Swallow Exception
                }
            }
        }
    }
    
    public void testCreateEMFWithBadJarFileElement() {
        EntityManagerFactory emf = null;
        try {
            // Create EMF, expecting no problems.
            emf = createEmf("PUTest-Good");
        } finally {
            if (emf != null) {
                try {
                    emf.close();
                } catch (Throwable t) {
                    // Swallow Exception
                }
            }
        }
    }

    public void testCreateEMFWithNonOpenJPAProvider() {
        EntityManagerFactory emf = null;
        try {
            emf = createEmf("PUTest-NonOpenJPAProvider");

            // Did not catch the expected MissingResourceException Exception
            fail("The createEntityManager() operation did not throw any Exception.");
        } catch (java.util.MissingResourceException mre) {
            // Caught the expected PersistenceException
        } finally {
            if (emf != null) {
                try {
                    emf.close();
                } catch (Throwable t) {
                    // Swallow Exception
                }
            }
        }
    }

    public void testCreateEMFWithBadJarFileElementAndNonOpenJPAProvider() {
        EntityManagerFactory emf = null;
        try {
            emf = createEmf("PUTest-BadJarFile-NonOpenJPAProvider");

            // Did not catch the expected MissingResourceException Exception
            fail("The createEntityManager() operation did not throw any Exception.");
        } catch (java.util.MissingResourceException mre) {
            // Caught the expected PersistenceException
        } finally {
            if (emf != null) {
                try {
                    emf.close();
                } catch (Throwable t) {
                    // Swallow Exception
                }
            }
        }
    }
    
}
