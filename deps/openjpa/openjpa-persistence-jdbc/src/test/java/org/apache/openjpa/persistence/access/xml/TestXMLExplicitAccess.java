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
package org.apache.openjpa.persistence.access.xml;

import java.util.Date;

import javax.persistence.Query;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestXMLExplicitAccess extends SingleEMFTestCase {
        
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    protected String getPersistenceUnitName() {
        return "Access-1";
    }
    /**
     * Validates the use of field level access on an
     * entity, mappedsuperclass, and embeddable at the
     * class level.
     */
    public void testClassSpecifiedFieldAccess() {

        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLFieldAccess fa = new XMLFieldAccess();
        // Set the persistent field through a misnamed setter         
        fa.setStringField("XMLFieldAccess");
        
        em.getTransaction().begin();
        em.persist(fa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the field name to verify that
        // field access is in use.
        Query qry = em.createNamedQuery("XMLFieldAccess.query");
        qry.setParameter("id", fa.getId());
        qry.setParameter("strVal", "XMLFieldAccess");
        XMLFieldAccess fa2 = (XMLFieldAccess)qry.getSingleResult();
        assertEquals(fa.getId(), fa2.getId());
        
        em.close();
    }

    /**
     * Validates the use of property level access on an
     * entity, mappedsuperclass, and embeddable at the
     * class level.
     */
    public void testClassSpecifiedPropertyAccess() {

        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLPropAccess pa = new XMLPropAccess();
        // Set the persistent field through a misnamed setter         
        pa.setStrProp("PropertyAccess");
        
        em.getTransaction().begin();
        em.persist(pa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the field name to verify that
        // field access is in use.
        Query qry = em.createNamedQuery("XMLPropAccess.query");
        qry.setParameter("id", pa.getId());
        qry.setParameter("strVal", "PropertyAccess");
        XMLPropAccess pa2 = (XMLPropAccess)qry.getSingleResult();
        assertEquals(pa, pa2);
        
        em.close();
    }
    
    /**
     * Validates the use of explicit field access on an entity, 
     * mappedsuperclass, and embeddable with property access
     * defined at the class level and field access defined
     * on specific methods. 
     */    
    
    public void testClassSpecifiedMixedSinglePCFieldAccess() {

        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLDefFieldMixedPropAccess dfmpa = new XMLDefFieldMixedPropAccess();
        // Call non-PC setter
        dfmpa.setStrField("NonPCSetter");
        // Call setter with property access
        dfmpa.setStringField("XMLDFMPA");
        
        em.getTransaction().begin();
        em.persist(dfmpa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent property was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLDFMPA.query");
        qry.setParameter("id", dfmpa.getId());
        qry.setParameter("strVal", "XMLDFMPA");
        XMLDefFieldMixedPropAccess dfmpa2 = 
            (XMLDefFieldMixedPropAccess)qry.getSingleResult();
        assertEquals(dfmpa, dfmpa2);
        assertEquals(dfmpa2.getStringField(), "XMLDFMPA");

        try {
            qry = em.createNamedQuery("XMLDFMPA.badQuery");
            qry.setParameter("id", dfmpa.getId());
            qry.setParameter("strVal", "XMLDFMPA");
            qry.getSingleResult();
            fail("Execution of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"strField\" in \"XMLDefFieldMixedPropAccess\"",
                "[id, stringField, version]");
        } finally {
            em.close();
        }
    }
    
    /**
     * Validates the use of explicit property access on an entity, 
     * mappedsuperclass, and embeddable with field access
     * defined at the class level and property access defined
     * on specific methods. 
     */
    public void testClassSpecifiedMixedSinglePCPropertyAccess() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLDefPropMixedFieldAccess dpmfa = new XMLDefPropMixedFieldAccess();
        // Call setter with underlying field access
        dpmfa.setStrProp("XMLDPMFA");
        
        em.getTransaction().begin();
        em.persist(dpmfa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLDPMFA.query");
        qry.setParameter("id", dpmfa.getId());
        qry.setParameter("strVal", "XMLDPMFA");
        XMLDefPropMixedFieldAccess dpmfa2 = 
            (XMLDefPropMixedFieldAccess)qry.getSingleResult();
        assertEquals(dpmfa, dpmfa2);
        assertEquals(dpmfa2.getStrProp(), "XMLDPMFA");

        try {
            qry = em.createNamedQuery("XMLDPMFA.badQuery");
            qry.setParameter("id", dpmfa.getId());
            qry.setParameter("strVal", "XMLDPMFA");
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"strProp\" in \"XMLDefPropMixedFieldAccess\"",
                "[id, strField, version]");
        } finally {
            em.close();
        }
    }
    
    /**
     * Validates that a mapped superclass using field access and an entity
     * subclass using property access get mapped properly.
     */
    public void testAbstractMappedSuperField() {
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("Access-XML",
            "org/apache/openjpa/persistence/access/" +
            "access-persistence.xml");

        OpenJPAEntityManagerSPI em = emf1.createEntityManager();

        XMLPropertySub ps = new XMLPropertySub();
        // Call super setter with underlying field access
        ps.setName("AbsMappedSuperName");
        // Call base setter with property access
        Date now = new Date();
        ps.setCreateDate(now);
        
        em.getTransaction().begin();
        em.persist(ps);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLPropertySub.query");
        qry.setParameter("id", ps.getId());
        qry.setParameter("name", "AbsMappedSuperName");
        qry.setParameter("crtDate", now);
        XMLPropertySub ps2 = 
            (XMLPropertySub)qry.getSingleResult();
        assertEquals(ps, ps2);
        assertEquals(ps2.getName(), "AbsMappedSuperName");
        assertEquals(ps2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLPropertySub.badQuery");
            qry.setParameter("id", ps.getId());
            qry.setParameter("name", "AbsMappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"crtDate\" in \"XMLPropertySub\"",
                "[createDate, id, name]");
        } finally {
            em.close();
            clear(emf1);
            closeEMF(emf1);
        }
    }

    /**
     * Validates that a mapped superclass using property access and an entity
     * subclass using field access get mapped properly.
     */
    public void testAbstractMappedSuperProperty() {

        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("Access-XML",
            "org/apache/openjpa/persistence/access/" +
            "access-persistence.xml");

        OpenJPAEntityManagerSPI em = emf1.createEntityManager();
        
        XMLFieldSub fs = new XMLFieldSub();
        // Call super setter with underlying field access
        fs.setName("AbsMappedSuperName");
        // Call base setter with property access
        Date now = new Date();
        fs.setCreateDate(now);
        
        em.getTransaction().begin();
        em.persist(fs);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLFieldSub.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "AbsMappedSuperName");
        qry.setParameter("crtDate", now);
        XMLFieldSub fs2 = 
            (XMLFieldSub)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "AbsMappedSuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLFieldSub.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "AbsMappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"createDate\" in \"XMLFieldSub\"",
                "[crtDate, id, name]");
        } finally {
            em.close();
            clear(emf1);
            closeEMF(emf1);
        }
    }

    /**
     * Validates that an mapped superclass using field access and an 
     * entity subclass using property access get mapped properly.
     * The subclass uses a storage field in the superclass. 
     */
    public void testMappedSuperField() {
        
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("Access-XML",
            "org/apache/openjpa/persistence/access/" +
            "access-persistence.xml");

        OpenJPAEntityManagerSPI em = emf1.createEntityManager();
        
        XMLPropertySub2 ps = new XMLPropertySub2();
        // Call super setter with underlying field access
        ps.setName("MappedSuperName");
        // Call base setter with property access
        Date now = new Date();
        ps.setCreateDate(now);
        
        em.getTransaction().begin();
        em.persist(ps);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLPropertySub2.query");
        qry.setParameter("id", ps.getId());
        qry.setParameter("name", "MappedSuperName");
        qry.setParameter("crtDate", now);
        XMLPropertySub2 ps2 = 
            (XMLPropertySub2)qry.getSingleResult();
        assertEquals(ps, ps2);
        assertEquals(ps2.getName(), "MappedSuperName");
        assertEquals(ps2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLPropertySub2.badQuery");
            qry.setParameter("id", ps.getId());
            qry.setParameter("name", "MappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"XMLPropertySub2\"",
                    "[createDate, id, name]");
        } finally {
            em.close();
            clear(emf1);
            closeEMF(emf1);
        }
    }

    /**
     * Validates that an mapped superclass using field access and an 
     * entity subclass using property access get mapped properly.
     * The subclass uses a storage field in the superclass. 
     */
    public void testMappedSuperProperty() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLFieldSub2 fs = new XMLFieldSub2();
        // Call super setter with underlying field access
        fs.setName("MappedSuperName");
        // Call base setter with property access
        Date now = new Date();
        fs.setCreateDate(now);
        
        em.getTransaction().begin();
        em.persist(fs);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLFieldSub2.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "MappedSuperName");
        qry.setParameter("crtDate", now);
        XMLFieldSub2 fs2 = 
            (XMLFieldSub2)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "MappedSuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLFieldSub2.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "MappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"createDate\" in \"XMLFieldSub2\"",
                    "[crtDate, id, name]");
        } finally {
            em.close();        
        }
    }

    /**
     * Validates that a mix of access types can be used within multiple 
     * persistent classes within an inheritance hierarchy that uses 
     * MappedSuperclass.
     */
    public void testMixedMappedSuper() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLMixedFieldSub fs = new XMLMixedFieldSub();
        // Call super setter with underlying field access
        fs.setName("XMLMixedMappedSuperName");
        fs.setMyFieldProp("MyFieldName");
        // Call base setter with property access
        Date now = new Date();
        fs.setCreateDate(now);
        
        em.getTransaction().begin();
        em.persist(fs);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLMixedFieldSub.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "XMLMixedMappedSuperName");
        qry.setParameter("crtDate", now);
        qry.setParameter("myField", "MyFieldName");
        XMLMixedFieldSub fs2 = 
            (XMLMixedFieldSub)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "XMLMixedMappedSuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLMixedFieldSub.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "XMLMixedMappedSuperName");
            qry.setParameter("myField", "MyFieldName");
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"myFieldProp\" in \"XMLMixedFieldSub\"",
                    "[createDate, mid, myField, name]");
        } finally {
            em.close();        
        }        
    }

    /**
     * Validates that a mix of access types can be used within
     * an inheritance hierarchy which uses default Entity inheritance.
     * NOTE: be sure to test with all forms of inheritance.
     */
    public void testEntityFieldDefaultInheritance() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLFieldSub3 fs = new XMLFieldSub3();
        // Call super setter with underlying field access
        fs.setName("EntitySuperName");
        // Call base setter with property access
        Date now = new Date();
        fs.setCreateDate(now);

        XMLSuperPropertyEntity spe = new XMLSuperPropertyEntity();
        spe.setName("SuperPropertyEntity");
        
        em.getTransaction().begin();
        em.persist(fs);
        em.persist(spe);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLFieldSub3.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "EntitySuperName");
        qry.setParameter("crtDate", now);
        XMLFieldSub3 fs2 = 
            (XMLFieldSub3)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "EntitySuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLFieldSub3.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "EntitySuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"createDate\" in \"XMLFieldSub3\"",
                    "[crtDate, id, name]");
        }

        qry = em.createNamedQuery("XMLSuperPropertyEntity.query");
        qry.setParameter("id", spe.getId());
        qry.setParameter("name", "SuperPropertyEntity");
        XMLSuperPropertyEntity spe2 = 
            (XMLSuperPropertyEntity)qry.getSingleResult();
        assertEquals(spe, spe2);
        assertEquals(spe2.getName(), "SuperPropertyEntity");

        try {
            // This query ensures that a subclass property didn't somehow get
            // picked up by the superclass while building field metadata using
            // explicit access.
            qry = em.createNamedQuery("XMLSuperPropertyEntity.badQuery");
            qry.setParameter("id", spe.getId());
            qry.setParameter("name", "SuperPropertyEntity");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"XMLSuperPropertyEntity\"",
                    "[id, name]");
        } finally {
            em.close();        
        }
    }

    /**
     * Validates that a mix of access types can be used within
     * an inheritance hierarchy which uses default Entity inheritance.
     * NOTE: be sure to test with all forms of inheritance.
     */
    public void testEntityPropertyDefaultInheritance() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLPropertySub3 ps = new XMLPropertySub3();
        // Call super setter with underlying field access
        ps.setName("EntitySuperName");
        // Call base setter with property access
        Date now = new Date();
        ps.setCreateDate(now);

        XMLSuperFieldEntity sfe = new XMLSuperFieldEntity();
        sfe.setName("SuperFieldEntity");
        
        em.getTransaction().begin();
        em.persist(ps);
        em.persist(sfe);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("XMLPropertySub3.query");
        qry.setParameter("id", ps.getId());
        qry.setParameter("name", "EntitySuperName");
        qry.setParameter("crtDate", now);
        XMLPropertySub3 ps2 = 
            (XMLPropertySub3)qry.getSingleResult();
        assertEquals(ps, ps2);
        assertEquals(ps2.getName(), "EntitySuperName");
        assertEquals(ps2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("XMLPropertySub3.badQuery");
            qry.setParameter("id", ps.getId());
            qry.setParameter("name", "EntitySuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"XMLPropertySub3\"",
                    "[createDate, id, name]");
        }

        qry = em.createNamedQuery("XMLSuperFieldEntity.query");
        qry.setParameter("id", sfe.getId());
        qry.setParameter("name", "SuperFieldEntity");
        XMLSuperFieldEntity sfe2 = 
            (XMLSuperFieldEntity)qry.getSingleResult();
        assertEquals(sfe2, sfe2);
        assertEquals(sfe2.getName(), "SuperFieldEntity");

        try {
            // This query ensures that a subclass property didn't somehow get
            // picked up by the superclass while building field metadata using
            // explicit access.
            qry = em.createNamedQuery("XMLSuperFieldEntity.badQuery");
            qry.setParameter("id", sfe.getId());
            qry.setParameter("name", "SuperFieldEntity");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"XMLSuperFieldEntity\"",
                    "[id, name]");
        } finally {
            em.close();        
        }
    }

    /**
     * Validates an embeddable with field access can be used within an
     * entity with property access
     */
    @AllowFailure(value=true, 
        message="Support for explicit Access on embeddables is not complete.")
    public void testEmbeddablesField() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLEmbedFieldAccess efa = new XMLEmbedFieldAccess();
        efa.setFirstName("J");
        efa.setLastName("Tolkien");
        
        XMLPropEmbedEntity pe = new XMLPropEmbedEntity();
        pe.setName("PropEmbedEntity");
        pe.setEmbedProp(efa);
        
        em.getTransaction().begin();
        em.persist(pe);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("XMLPropEmbedEntity.query");
        qry.setParameter("id", pe.getId());
        qry.setParameter("name", "PropEmbedEntity");
        qry.setParameter("firstName", "J");
        qry.setParameter("lastName", "Tolkien");
        XMLPropEmbedEntity pe2 = (XMLPropEmbedEntity)qry.getSingleResult();
        assertEquals(pe, pe2);
        assertEquals(efa, pe2.getEmbedProp());

        try {
            qry = em.createNamedQuery("XMLPropEmbedEntity.badQuery");
            qry.setParameter("id", pe.getId());
            qry.setParameter("name", "PropEmbedEntity");
            qry.setParameter("firstName", "J");
            qry.setParameter("lastName", "Tolkien");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"firstName\" in \"XMLEmbedFieldAccess\"",
                    "[fName, lName]");
        } finally {
            em.close();        
        }
    }

    /**
     * Validates an embeddable with property access can be used within an
     * entity with field access
     */
    @AllowFailure(value=true, 
        message="Support for explicit Access on embeddables is not complete.")
    public void testEmbeddablesProperty() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLEmbedPropAccess epa = new XMLEmbedPropAccess();
        epa.setFirstName("Walt");
        epa.setLastName("Whitman");
        
        XMLFieldEmbedEntity fe = new XMLFieldEmbedEntity();
        fe.setName("FieldEmbedEntity");
        fe.setEPA(epa);
        
        em.getTransaction().begin();
        em.persist(fe);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("XMLFieldEmbedEntity.query");
        qry.setParameter("id", fe.getId());
        qry.setParameter("name", "FieldEmbedEntity");
        qry.setParameter("firstName", "Walt");
        qry.setParameter("lastName", "Whitman");
        XMLFieldEmbedEntity fe2 = (XMLFieldEmbedEntity)qry.getSingleResult();
        fe2.getEPA().getFirstName();
        fe2.getEPA().getLastName();
        assertEquals(fe, fe2);
        assertEquals(epa, fe2.getEPA());

        try {
            qry = em.createNamedQuery("XMLFieldEmbedEntity.badQuery");
            qry.setParameter("id", fe.getId());
            qry.setParameter("name", "FieldEmbedEntity");
            qry.setParameter("firstName", "Walt");
            qry.setParameter("lastName", "Whitman");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"fName\" in \"XMLEmbedPropAccess\"",
                    "[firstName, lastName]");
        } finally {
            em.close();        
        }
    }

    /**
     * Validates an embeddable with mixed access can be used within an
     * entity with mixed access
     */
    @AllowFailure(value=true, 
        message="Support for explicit Access on embeddables is not complete.")
    public void testMixedEmbeddables() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        XMLEmbedMixedAccess ema = new XMLEmbedMixedAccess();
        ema.setFirstName("J");
        ema.setLastName("Tolkien");
        ema.setMiddleName("R");
        
        XMLPropMixedEntity pm = new XMLPropMixedEntity();
        pm.setName("PropMixedEntity");
        pm.setEmbedProp(ema);
        
        em.getTransaction().begin();
        em.persist(pm);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("XMLPropMixedEntity.query");
        qry.setParameter("id", pm.getId());
        qry.setParameter("name", "PropMixedEntity");
        qry.setParameter("firstName", "J");
        qry.setParameter("lastName", "Tolkien");
        qry.setParameter("middleName", "R");
        XMLPropMixedEntity pm2 = (XMLPropMixedEntity)qry.getSingleResult();
        assertEquals(pm, pm2);
        assertEquals(ema, pm2.getEmbedProp());

        try {
            qry = em.createNamedQuery("XMLPropMixedEntity.badQuery");
            qry.setParameter("id", pm.getId());
            qry.setParameter("name", "PropMixedEntity");
            qry.setParameter("firstName", "J");
            qry.setParameter("lastName", "Tolkien");
            qry.setParameter("middleName", "R");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"middleName\" in \"XMLEmbedMixedAccess\"",
                "[firstName, lastName, mName]");
        } finally {
            em.close();        
        }
    }
}
