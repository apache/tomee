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

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestSimpleXmlEntity
    extends SingleEMTestCase {

    public void setUp() {
        setUp(
        org.apache.openjpa.persistence.embed.attrOverrides.AnnoOverEmbed.class,
        CLEAR_TABLES);
    }

    protected String getPersistenceUnitName() {
        return "xml-persistence-unit";
    }

    public void testId() {
        em.getTransaction().begin();
        SimpleXmlEntity e = new SimpleXmlEntity();
        em.persist(e);
        em.flush();
        assertNotNull(e.getId());
        try {
            Integer.parseInt(e.getId());
            fail("uuid-based id should not be an integer; was " + e.getId());
        } catch (NumberFormatException nfe) {
            // expected
        }
        em.getTransaction().rollback();
    }

    public void testNamedQueryInXmlNamedEntity() {
        em.createNamedQuery("SimpleXml.findAll").getResultList();
    }

    public void testNamedQueryInXmlUsingShortClassName() {
        try {
            em.createNamedQuery("SimpleXmlEntity.findAll").getResultList();
            fail("should not be able to execute query using short class name " +
                "for entity that has an entity name specified");
        } catch (ArgumentException ae) {
            assertMarkerInErrorMessage(ae, "The name \"SimpleXmlEntity\" is not a recognized entity");
        }
    }

    public void testNamedEntityInDynamicQuery() {
        em.createQuery("select o from SimpleXml o").getResultList();
    }

    public void testShortClassNameInDynamicQuery() {
        try {
            em.createQuery("select o from SimpleXmlEntity o").getResultList();
            fail("should not be able to execute query using short class name " +
                "for entity that has an entity name specified");
        } catch (ArgumentException ae) {
            assertMarkerInErrorMessage(ae, "The name \"SimpleXmlEntity\" is not a recognized entity");
        }
    }
    
    /**
     * Asserts that the given marker string appears in the error message.
     * Not a kosher way to verify -- but ...
     */
    void assertMarkerInErrorMessage(Exception ex, String marker) {
        String message = ex.getMessage();
        assertTrue("Can not find [" + marker + "] in the message [" + message + "]", message.indexOf(marker) != -1);
    }
}
