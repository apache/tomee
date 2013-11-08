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
package org.apache.openjpa.persistence.xml;

import junit.framework.TestCase;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;

public class TestSchemaVersionValidation extends TestCase {
    
    /**
     * Verify a pu can be created with a version 2.0 persistence.xml
     */
    public void test2_0PersistenceXml() {        
        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.
            createEntityManagerFactory("XSDTest", 
                "org/apache/openjpa/persistence/xml/persistence-2_0.xml");
        emf.close();
    }

    /**
     * Verify schema validation will fail when using a 2.0 
     * persistence.xml that does not contain a persistence unit
     * (the 2.0 spec made it a requirement for the persistence file
     * to contain at least one pu.)
     */
    public void testBad2_0PersistenceXml() {
        try {
            OpenJPAEntityManagerFactory emf = OpenJPAPersistence.
                createEntityManagerFactory(null, 
                "org/apache/openjpa/persistence/xml/persistence-2_0-no-pu.xml");
            emf.close();
            fail();
        } catch (Exception e) {
            // JREs fail differently for this test. Detection and 
            // assertion of a JRE specific failure has shown to be error prone 
            // so only a generic exception is detected.
        }        
    }
    
    /**
     * Verify a version 2.0 persistence.xml can reference and the provider
     * can parse a version 1.0 orm.xml 
     */
    public void test2_0Persistence1_0OrmXml() {
        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.
            createEntityManagerFactory("XSDTest", 
            "org/apache/openjpa/persistence/xml/" +
            "persistence-2_0-orm-1_0.xml");
        OpenJPAEntityManager em = emf.createEntityManager();
        em.close();
        emf.close();
    }
    
    /**
     * Verify a version 2.0 persistence.xml can reference and the provider can
     * parse a version 2.0 orm.xml
     */
    public void test2_0Persistence2_0OrmXml() {
        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.
            createEntityManagerFactory("XSDTest", 
            "org/apache/openjpa/persistence/xml/" +
            "persistence-2_0-orm-2_0.xml");
        OpenJPAEntityManager em = emf.createEntityManager();
        em.close();
        emf.close();
    }
    

    
    /**
     * Verify a 1.0 persistence.xml can include a 2.0 orm.xml
     */
    public void test1_0Persistence2_0OrmXml() {
        OpenJPAEntityManagerFactory emf = OpenJPAPersistence.
            createEntityManagerFactory("XSDTest", 
            "org/apache/openjpa/persistence/xml/" +
            "persistence-2_0-orm-1_0.xml");
        OpenJPAEntityManager em = emf.createEntityManager();
        em.close();
        emf.close();
    }

}
