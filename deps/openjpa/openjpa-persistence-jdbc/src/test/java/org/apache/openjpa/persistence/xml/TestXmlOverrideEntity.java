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

import javax.persistence.EntityManager;

import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.InvalidStateException;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestXmlOverrideEntity extends SingleEMFTestCase {

    public void setUp() throws ClassNotFoundException {
        setUp(
        org.apache.openjpa.persistence.embed.attrOverrides.AnnoOverEmbed.class,
        CLEAR_TABLES);

        // make sure that XmlOverrideEntity is registered for our metadata tests
        Class.forName(XmlOverrideEntity.class.getName(), true,
            XmlOverrideEntity.class.getClassLoader());
    }

    protected String getPersistenceUnitName() {
        return "xml-persistence-unit";
    }

    public void testOverrideHappenedDuringEnhancement()
        throws ClassNotFoundException {
        // this mostly tests our test harness. Since XmlOverrideEntity
        // has annotation-based metadata, it is important that the first
        // PU in which it gets scanned-and-enhanced knows about overriding.
        assertTrue(PersistenceCapable.class.isAssignableFrom(
            XmlOverrideEntity.class));
        assertEquals("XmlOverride",
            PCRegistry.getTypeAlias(XmlOverrideEntity.class));
    }

    public void testOverriddenEntityName() {
        emf.createEntityManager().close();
        ClassMetaData meta = JPAFacadeHelper.getMetaData(emf,
            XmlOverrideEntity.class);
        assertEquals("XmlOverride", meta.getTypeAlias());
        MetaDataRepository repo = emf.getConfiguration()
            .getMetaDataRepositoryInstance();
        assertEquals(meta, repo.getMetaData("XmlOverride",
            XmlOverrideEntity.class.getClassLoader(), true));
    }

    /**
     * Tests that the optional attribute on a basic field can be overrided by
     * an xml descriptor. 
     * 
     * XmlOverrideEntity.name is annotated with optional=false
     * XmlOverrideEntity.description is annotated with optional=true. 
     * 
     * The optional attributes are reversed in orm.xml. 
     */
    public void testOptionalAttributeOverride() {
        EntityManager em = emf.createEntityManager();

        XmlOverrideEntity optional = new XmlOverrideEntity();

        optional.setName(null);
        optional.setDescription("description");

        em.getTransaction().begin();
        em.persist(optional);
        em.getTransaction().commit();

        try {
            em.getTransaction().begin();
            optional.setDescription(null);
            em.getTransaction().commit();
            fail("XmlOrverrideEntity.description should not be optional. "
                    + "Expecting an InvalidStateException.");
        } catch (InvalidStateException e) {
        }

        em.getTransaction().begin();
        em.remove(em.find(XmlOverrideEntity.class, optional.getId()));
        em.getTransaction().commit();
        
        em.close();
    }
    
    
    public void testColumnOverride() { 
        EntityManager em = emf.createEntityManager();

        ClassMapping mapping = getMapping("XmlOverride");
        
        FieldMapping fm = mapping.getFieldMapping("picture");
        
        Column[] columns = fm.getColumns();
        
        assertEquals(1, columns.length);
        assertEquals("pic_xml", columns[0].getName());
        
        em.close();
    }
}

