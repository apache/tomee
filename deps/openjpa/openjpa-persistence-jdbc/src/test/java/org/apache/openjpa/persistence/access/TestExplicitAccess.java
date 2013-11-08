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
package org.apache.openjpa.persistence.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.persistence.Query;

import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestExplicitAccess extends SingleEMFTestCase {

    public void setUp() {
        setUp(CLEAR_TABLES, 
            PropAccess.class, FieldAccess.class,
            DefFieldMixedPropAccess.class , DefPropMixedFieldAccess.class,
            AbstractMappedSuperField.class, PropertySub.class,
            MappedSuperField.class, PropertySub2.class,
            SuperFieldEntity.class, PropertySub3.class,
            AbstractMappedSuperProperty.class, FieldSub.class,
            MappedSuperProperty.class, FieldSub2.class,
            SuperPropertyEntity.class, FieldSub3.class,
            MixedMappedSuper.class, MixedFieldSub.class,
            FieldEmbedEntity.class, EmbedFieldAccess.class,
            PropEmbedEntity.class, EmbedPropAccess.class,
            PropMixedEntity.class, EmbedMixedAccess.class,
            MixedNestedEmbedEntity.class, EmbedInnerProp.class,
            EmbedOuterField.class, MixedMultEmbedEntity.class,
            FieldAccessPropStratsEntity.class, 
            PropAccessFieldStratsEntity.class,
            EmbedId.class, MenuItem.class, Ingredient.class, Quantity.class);
    }

    
    /**
     * Validates the use of field level access on an
     * entity, mappedsuperclass, and embeddable at the
     * class level.
     */
    public void testClassSpecifiedFieldAccess() {

        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        FieldAccess fa = new FieldAccess();
        // Set the persistent field through a misnamed setter         
        fa.setStringField("FieldAccess");
        
        em.getTransaction().begin();
        em.persist(fa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the field name to verify that
        // field access is in use.
        Query qry = em.createNamedQuery("FieldAccess.query");
        qry.setParameter("id", fa.getId());
        qry.setParameter("strVal", "FieldAccess");
        FieldAccess fa2 = (FieldAccess)qry.getSingleResult();
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
        
        PropAccess pa = new PropAccess();
        // Set the persistent field through a misnamed setter         
        pa.setStrProp("PropertyAccess");
        
        em.getTransaction().begin();
        em.persist(pa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the field name to verify that
        // field access is in use.
        Query qry = em.createNamedQuery("PropertyAccess.query");
        qry.setParameter("id", pa.getId());
        qry.setParameter("strVal", "PropertyAccess");
        PropAccess pa2 = (PropAccess)qry.getSingleResult();
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
        
        DefFieldMixedPropAccess dfmpa = new DefFieldMixedPropAccess();
        // Call non-PC setter
        dfmpa.setStrField("NonPCSetter");
        // Call setter with property access
        dfmpa.setStringField("DFMPA");
        
        em.getTransaction().begin();
        em.persist(dfmpa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent property was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("DFMPA.query");
        qry.setParameter("id", dfmpa.getId());
        qry.setParameter("strVal", "DFMPA");
        DefFieldMixedPropAccess dfmpa2 = 
            (DefFieldMixedPropAccess)qry.getSingleResult();
        assertEquals(dfmpa, dfmpa2);
        assertEquals(dfmpa2.getStringField(), "DFMPA");

        try {
            qry = em.createNamedQuery("DFMPA.badQuery");
            qry.setParameter("id", dfmpa.getId());
            qry.setParameter("strVal", "DFMPA");
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"strField\" in \"DefFieldMixedPropAccess\"",
                "[id, stringField, version]");
        }

        em.close();
    }
    
    /**
     * Validates the use of explicit property access on an entity, 
     * mappedsuperclass, and embeddable with field access
     * defined at the class level and property access defined
     * on specific methods. 
     */    
    public void testClassSpecifiedMixedSinglePCPropertyAccess() {

        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        DefPropMixedFieldAccess dpmfa = new DefPropMixedFieldAccess();
        // Call setter with underlying field access
        dpmfa.setStrProp("DPMFA");
        
        em.getTransaction().begin();
        em.persist(dpmfa);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("DPMFA.query");
        qry.setParameter("id", dpmfa.getId());
        qry.setParameter("strVal", "DPMFA");
        DefPropMixedFieldAccess dpmfa2 = 
            (DefPropMixedFieldAccess)qry.getSingleResult();
        assertEquals(dpmfa, dpmfa2);
        assertEquals(dpmfa2.getStrProp(), "DPMFA");

        try {
            qry = em.createNamedQuery("DPMFA.badQuery");
            qry.setParameter("id", dpmfa.getId());
            qry.setParameter("strVal", "DPMFA");
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"strProp\" in \"DefPropMixedFieldAccess\"",
                "[id, strField, version]");
        }

        em.close();
    }
    
    /**
     * Validates that a mapped superclass using field access and an entity
     * subclass using property access get mapped properly.
     */
    public void testAbstractMappedSuperField() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        PropertySub ps = new PropertySub();
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
        Query qry = em.createNamedQuery("PropertySub.query");
        qry.setParameter("id", ps.getId());
        qry.setParameter("name", "AbsMappedSuperName");
        qry.setParameter("crtDate", now);
        PropertySub ps2 = 
            (PropertySub)qry.getSingleResult();
        assertEquals(ps, ps2);
        assertEquals(ps2.getName(), "AbsMappedSuperName");
        assertEquals(ps2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("PropertySub.badQuery");
            qry.setParameter("id", ps.getId());
            qry.setParameter("name", "AbsMappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"crtDate\" in \"PropertySub\"",
                "[createDate, id, name]");
        }

        em.close();
    }

    /**
     * Validates that a mapped superclass using property access and an entity
     * subclass using field access get mapped properly.
     */
    public void testAbstractMappedSuperProperty() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        FieldSub fs = new FieldSub();
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
        Query qry = em.createNamedQuery("FieldSub.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "AbsMappedSuperName");
        qry.setParameter("crtDate", now);
        FieldSub fs2 = 
            (FieldSub)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "AbsMappedSuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("FieldSub.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "AbsMappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"createDate\" in \"FieldSub\"",
                "[crtDate, id, name]");
        }

        em.close();
    }

    /**
     * Validates that an mapped superclass using field access and an 
     * entity subclass using property access get mapped properly.
     * The subclass uses a storage field in the superclass. 
     */
    public void testMappedSuperField() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        PropertySub2 ps = new PropertySub2();
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
        Query qry = em.createNamedQuery("PropertySub2.query");
        qry.setParameter("id", ps.getId());
        qry.setParameter("name", "MappedSuperName");
        qry.setParameter("crtDate", now);
        PropertySub2 ps2 = 
            (PropertySub2)qry.getSingleResult();
        assertEquals(ps, ps2);
        assertEquals(ps2.getName(), "MappedSuperName");
        assertEquals(ps2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("PropertySub2.badQuery");
            qry.setParameter("id", ps.getId());
            qry.setParameter("name", "MappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"PropertySub2\"",
                    "[createDate, id, name]");
        }

        em.close();        
    }

    /**
     * Validates that an mapped superclass using field access and an 
     * entity subclass using property access get mapped properly.
     * The subclass uses a storage field in the superclass. 
     */
    public void testMappedSuperProperty() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        FieldSub2 fs = new FieldSub2();
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
        Query qry = em.createNamedQuery("FieldSub2.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "MappedSuperName");
        qry.setParameter("crtDate", now);
        FieldSub2 fs2 = 
            (FieldSub2)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "MappedSuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("FieldSub2.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "MappedSuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"createDate\" in \"FieldSub2\"",
                    "[crtDate, id, name]");
        }

        em.close();        
    }

    /**
     * Validates that a mix of access types can be used within multiple 
     * persistent classes within an inheritance hierarchy that uses 
     * MappedSuperclass.
     */
    public void testMixedMappedSuper() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        MixedFieldSub fs = new MixedFieldSub();
        // Call super setter with underlying field access
        fs.setName("MixedMappedSuperName");
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
        Query qry = em.createNamedQuery("MixedFieldSub.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "MixedMappedSuperName");
        qry.setParameter("crtDate", now);
        qry.setParameter("myField", "MyFieldName");
        MixedFieldSub fs2 = 
            (MixedFieldSub)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "MixedMappedSuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("MixedFieldSub.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "MixedMappedSuperName");
            qry.setParameter("myField", "MyFieldName");
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"myFieldProp\" in \"MixedFieldSub\"",
                    "[createDate, mid, myField, name]");
        }

        em.close();        
        
    }
    
    /**
     * Validates that a mix of access types can be used within
     * an inheritance hierarchy which uses default Entity inheritance.
     * NOTE: be sure to test with all forms of inheritance.
     */
    public void testEntityFieldDefaultInheritance() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        FieldSub3 fs = new FieldSub3();
        // Call super setter with underlying field access
        fs.setName("EntitySuperName");
        // Call base setter with property access
        Date now = new Date();
        fs.setCreateDate(now);

        SuperPropertyEntity spe = new SuperPropertyEntity();
        spe.setName("SuperPropertyEntity");
        
        em.getTransaction().begin();
        em.persist(fs);
        em.persist(spe);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("FieldSub3.query");
        qry.setParameter("id", fs.getId());
        qry.setParameter("name", "EntitySuperName");
        qry.setParameter("crtDate", now);
        FieldSub3 fs2 = 
            (FieldSub3)qry.getSingleResult();
        assertEquals(fs, fs2);
        assertEquals(fs2.getName(), "EntitySuperName");
        assertEquals(fs2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("FieldSub3.badQuery");
            qry.setParameter("id", fs.getId());
            qry.setParameter("name", "EntitySuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"createDate\" in \"FieldSub3\"",
                    "[crtDate, id, name]");
        }

        qry = em.createNamedQuery("SuperPropertyEntity.query");
        qry.setParameter("id", spe.getId());
        qry.setParameter("name", "SuperPropertyEntity");
        SuperPropertyEntity spe2 = 
            (SuperPropertyEntity)qry.getSingleResult();
        assertEquals(spe, spe2);
        assertEquals(spe2.getName(), "SuperPropertyEntity");

        try {
            // This query ensures that a subclass property didn't somehow get
            // picked up by the superclass while building field metadata using
            // explicit access.
            qry = em.createNamedQuery("SuperPropertyEntity.badQuery");
            qry.setParameter("id", spe.getId());
            qry.setParameter("name", "SuperPropertyEntity");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"SuperPropertyEntity\"",
                    "[id, name]");
        }        
        
        em.close();        
    }

    /**
     * Validates that a mix of access types can be used within
     * an inheritance hierarchy which uses default Entity inheritance.
     * NOTE: be sure to test with all forms of inheritance.
     */
    public void testEntityPropertyDefaultInheritance() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        PropertySub3 ps = new PropertySub3();
        // Call super setter with underlying field access
        ps.setName("EntitySuperName");
        // Call base setter with property access
        Date now = new Date();
        ps.setCreateDate(now);

        SuperFieldEntity sfe = new SuperFieldEntity();
        sfe.setName("SuperFieldEntity");
        
        em.getTransaction().begin();
        em.persist(ps);
        em.persist(sfe);
        em.getTransaction().commit();
        em.clear();
        
        // This value of a persistent field was set using the setter
        // above, but this query will use the property name to verify that
        // propety access is in use.
        Query qry = em.createNamedQuery("PropertySub3.query");
        qry.setParameter("id", ps.getId());
        qry.setParameter("name", "EntitySuperName");
        qry.setParameter("crtDate", now);
        PropertySub3 ps2 = 
            (PropertySub3)qry.getSingleResult();
        assertEquals(ps, ps2);
        assertEquals(ps2.getName(), "EntitySuperName");
        assertEquals(ps2.getCreateDate().toString(), now.toString());

        try {
            qry = em.createNamedQuery("PropertySub3.badQuery");
            qry.setParameter("id", ps.getId());
            qry.setParameter("name", "EntitySuperName");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"PropertySub3\"",
                    "[createDate, id, name]");
        }

        qry = em.createNamedQuery("SuperFieldEntity.query");
        qry.setParameter("id", sfe.getId());
        qry.setParameter("name", "SuperFieldEntity");
        SuperFieldEntity sfe2 = 
            (SuperFieldEntity)qry.getSingleResult();
        assertEquals(sfe2, sfe2);
        assertEquals(sfe2.getName(), "SuperFieldEntity");

        try {
            // This query ensures that a subclass property didn't somehow get
            // picked up by the superclass while building field metadata using
            // explicit access.
            qry = em.createNamedQuery("SuperFieldEntity.badQuery");
            qry.setParameter("id", sfe.getId());
            qry.setParameter("name", "SuperFieldEntity");
            qry.setParameter("crtDate", now);
            qry.getSingleResult();
            fail("Usage of this query should have thrown an exception");
        }
        catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"crtDate\" in \"SuperFieldEntity\"",
                    "[id, name]");
        }        
        em.close();        
    }

    /**
     * Validates an embeddable with field access can be used within an
     * entity with property access
     */
    public void testEmbeddablesField() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        EmbedFieldAccess efa = new EmbedFieldAccess();
        efa.setFirstName("J");
        efa.setLastName("Tolkien");
        
        PropEmbedEntity pe = new PropEmbedEntity();
        pe.setName("PropEmbedEntity");
        pe.setEmbedProp(efa);
        
        em.getTransaction().begin();
        em.persist(pe);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("PropEmbedEntity.query");
        qry.setParameter("id", pe.getId());
        qry.setParameter("name", "PropEmbedEntity");
        qry.setParameter("firstName", "J");
        qry.setParameter("lastName", "Tolkien");
        PropEmbedEntity pe2 = (PropEmbedEntity)qry.getSingleResult();
        assertEquals(pe, pe2);
        assertEquals(efa, pe2.getEmbedProp());

        try {
            qry = em.createNamedQuery("PropEmbedEntity.badQuery");
            qry.setParameter("id", pe.getId());
            qry.setParameter("name", "PropEmbedEntity");
            qry.setParameter("firstName", "J");
            qry.setParameter("lastName", "Tolkien");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"firstName\" in \"EmbedFieldAccess\"",
                    "[fName, lName]");
        }

        em.close();
    }

    /**
     * Validates an embeddable with property access can be used within an
     * entity with field access
     */
    public void testEmbeddablesProperty() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        EmbedPropAccess epa = new EmbedPropAccess();
        epa.setFirstName("Walt");
        epa.setLastName("Whitman");
        
        FieldEmbedEntity fe = new FieldEmbedEntity();
        fe.setName("FieldEmbedEntity");
        fe.setEPA(epa);
        
        em.getTransaction().begin();
        em.persist(fe);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("FieldEmbedEntity.query");
        qry.setParameter("id", fe.getId());
        qry.setParameter("name", "FieldEmbedEntity");
        qry.setParameter("firstName", "Walt");
        qry.setParameter("lastName", "Whitman");
        FieldEmbedEntity fe2 = (FieldEmbedEntity)qry.getSingleResult();
        assertEquals(fe, fe2);
        assertEquals(epa, fe2.getEPA());

        try {
            qry = em.createNamedQuery("FieldEmbedEntity.badQuery");
            qry.setParameter("id", fe.getId());
            qry.setParameter("name", "FieldEmbedEntity");
            qry.setParameter("firstName", "Walt");
            qry.setParameter("lastName", "Whitman");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                    "No field named \"fName\" in \"EmbedPropAccess\"",
                    "[firstName, lastName]");
        }

        em.close();
    }

    /**
     * Validates an embeddable with mixed access can be used within an
     * entity with mixed access
     */
    public void testMixedEmbeddables() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        EmbedMixedAccess ema = new EmbedMixedAccess();
        ema.setFirstName("J");
        ema.setLastName("Tolkien");
        ema.setMiddleName("R");
        
        PropMixedEntity pm = new PropMixedEntity();
        pm.setName("PropMixedEntity");
        pm.setEmbedProp(ema);
        
        em.getTransaction().begin();
        em.persist(pm);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("PropMixedEntity.query");
        qry.setParameter("id", pm.getId());
        qry.setParameter("name", "PropMixedEntity");
        qry.setParameter("firstName", "J");
        qry.setParameter("lastName", "Tolkien");
        qry.setParameter("middleName", "R");
        PropMixedEntity pm2 = (PropMixedEntity)qry.getSingleResult();
        assertEquals(pm, pm2);
        assertEquals(ema, pm2.getEmbedProp());

        try {
            qry = em.createNamedQuery("PropMixedEntity.badQuery");
            qry.setParameter("id", pm.getId());
            qry.setParameter("name", "PropMixedEntity");
            qry.setParameter("firstName", "J");
            qry.setParameter("lastName", "Tolkien");
            qry.setParameter("middleName", "R");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"middleName\" in \"EmbedMixedAccess\"",
                "[firstName, lastName, mName]");
        }

        em.close();
    }

    /**
     * Validates that a mix of access types can be used within
     * an embeddable stack.
     */
    public void testNestedEmbeddables() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        EmbedInnerProp eip = new EmbedInnerProp();
        eip.setInnerName("Inner");
        EmbedOuterField eof = new EmbedOuterField();
        eof.setOuterName("Outer");
        eip.setOuterField(eof);
                
        MixedNestedEmbedEntity pm = new MixedNestedEmbedEntity();
        pm.setName("MixedNestedEmbedEntity");
        pm.setEmbedProp(eip);
        
        em.getTransaction().begin();
        em.persist(pm);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("MixedNestedEmbedEntity.query");
        qry.setParameter("id", pm.getId());
        qry.setParameter("name", "MixedNestedEmbedEntity");
        qry.setParameter("innerName", "Inner");
        qry.setParameter("outerName", "Outer");
        MixedNestedEmbedEntity pm2 = 
            (MixedNestedEmbedEntity)qry.getSingleResult();
        assertEquals(pm, pm2);
        assertEquals(eip, pm2.getEmbedProp());

        try {
            qry = em.createNamedQuery("MixedNestedEmbedEntity.badQuery");
            qry.setParameter("id", pm.getId());
            qry.setParameter("name", "MixedNestedEmbedEntity");
            qry.setParameter("innerName", "Inner");
            qry.setParameter("outerName", "Outer");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"outerName\" in \"EmbedOuterField\"",
                "[outName]");
        }

        em.close();
    }

    /**
     * Validates that a mix of access types can be used by an
     * an entity with mulitple embeddables.
     */
    public void testMultipleEmbeddables() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        EmbedFieldAccess efa = new EmbedFieldAccess();
        efa.setFirstName("First");
        efa.setLastName("Last");
        
        EmbedPropAccess epa = new EmbedPropAccess();
        epa.setFirstName("fname");
        epa.setLastName("lname");
                
        MixedMultEmbedEntity pm = new MixedMultEmbedEntity();
        pm.setName("MixedMultEmbedEntity");
        pm.setEmbedProp(epa);
        pm.setEmbedField(efa);
        
        em.getTransaction().begin();
        em.persist(pm);
        em.getTransaction().commit();
        
        em.clear();
        
        Query qry = em.createNamedQuery("MixedMultEmbedEntity.query");
        qry.setParameter("id", pm.getId());
        qry.setParameter("name", "MixedMultEmbedEntity");
        qry.setParameter("firstName", "fname");
        qry.setParameter("lastName", "lname");
        qry.setParameter("fName", "First");
        qry.setParameter("lName", "Last");
        MixedMultEmbedEntity pm2 = 
            (MixedMultEmbedEntity)qry.getSingleResult();
        assertEquals(pm, pm2);
        assertEquals(epa, pm2.getEmbedProp());
        assertEquals(efa, pm2.getEmbedField());

        try {
            qry = em.createNamedQuery("MixedMultEmbedEntity.badQuery1");
            qry.setParameter("id", pm.getId());
            qry.setParameter("name", "MixedMultEmbedEntity");
            qry.setParameter("epa", epa);
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            assertExceptionMessage(e, ArgumentException.class,
                "No field named \"epa\" in \"MixedMultEmbedEntity\"",
                "[embedField, embedProp, mid, name]");
        }

        try {
            qry = em.createNamedQuery("MixedMultEmbedEntity.badQuery2");
            qry.setParameter("id", pm.getId());
            qry.setParameter("name", "MixedMultEmbedEntity");
            qry.setParameter("epa", epa);
            qry.setParameter("firstName", "First");
            qry.setParameter("lastName", "Last");
            qry.getSingleResult();
            fail("Query execution should have failed.");
        } catch (Exception e) {
            // no support: conditional expressional expression over embeddable
            assertException(e, ArgumentException.class);
        }

        em.close();
    }

    /**
     * Validates explicit property access can be applied to all the access
     * strategies from within an entity with explicit field access (except for
     * the id field, which is field default)
     */
    public void testPropertyAccessStrategies() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        PropAccessFieldStratsEntity pa = new PropAccessFieldStratsEntity();
        
        // Load all persistent fields
        EmbedId eid = new EmbedId();
        eid.setId(new Random().nextInt());
        eid.setCode("IdCode");        
        pa.setEmbedId(eid); // embedded id
        
        Collection<EmbedPropAccess> elc = new ArrayList<EmbedPropAccess>();
        EmbedPropAccess epa1 = new EmbedPropAccess("Abe", "Lincoln");
        EmbedPropAccess epa2 = new EmbedPropAccess("John", "Kennedy");
        elc.add(epa1);
        elc.add(epa2);                
        pa.setElementCollection(elc); // element collection of embeddables
        
        EmbedFieldAccess efa = new EmbedFieldAccess();
        efa.setFirstName("The");
        efa.setLastName("President");
        pa.setEmbedField(efa); // embedded
        
        pa.setName("PropAccessFieldStratsEntity");  // basic
        
        PropAccess propa = new PropAccess();
        propa.setStrProp("PropAccess");
        pa.setManyToOne(propa); // many to one
        
        Collection<FieldAccess> fac = new ArrayList<FieldAccess>();
        FieldAccess fa = new FieldAccess();
        fa.setStrField("FieldAccess");
        fac.add(fa);
        pa.setOneToMany(fac); // one to many
        
        PropAccess pa2 = new PropAccess();
        pa2.setStrProp("PropAccess2");
        pa.setOneToOne(pa2); // one to one
       
        em.getTransaction().begin();
        em.persist(pa);
        em.getTransaction().commit();
        
        em.clear();
        // Verify list of persistent fields
        PropAccessFieldStratsEntity newpa = 
            em.find(PropAccessFieldStratsEntity.class, eid);
        assertNotNull(newpa);
        // simple key validation
        assertEquals(newpa.getEmbedId(), eid);

        // Verify the persistent member names
        MetaDataRepository mdr = 
            em.getConfiguration().getMetaDataRepositoryInstance();
        
        ClassMetaData cmd = mdr.getMetaData(PropAccessFieldStratsEntity.class, 
            null, true);
        // Assert expected persistent fields and properties were created
        assertNotNull(cmd.getField("embedId"));
        assertNotNull(cmd.getField("m2one"));
        assertNotNull(cmd.getField("one2m"));
        assertNotNull(cmd.getField("one2one"));
        assertNotNull(cmd.getField("ecoll"));
        assertNotNull(cmd.getField("embed"));
        assertNotNull(cmd.getField("ver"));
        assertNotNull(cmd.getField("m2m"));

        // Name has a matching getter/setter.  Make sure the access type
        // is field & not property
        assertNotNull(cmd.getField("name"));
        assertTrue((cmd.getField("name").getAccessType() & AccessCode.FIELD) == 
            AccessCode.FIELD);

        // Assert mappings were not created for fields or properties which 
        // should not be persistent
        assertNull(cmd.getField("eid"));
        assertNull(cmd.getField("elementCollection"));
        assertNull(cmd.getField("embedField"));
        assertNull(cmd.getField("version"));
        assertNull(cmd.getField("manyToOne"));
        assertNull(cmd.getField("oneToMany"));
        assertNull(cmd.getField("oneToOne"));
        assertNull(cmd.getField("manyToMany"));
        
        em.close();
    }

    /**
     * Validates explicit field access can be applied to all the access
     * strategies from within an entity with explicit property access (except
     * for the id field, which is property default)
     */
    public void testFieldAccessStrategies() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        FieldAccessPropStratsEntity fa = new FieldAccessPropStratsEntity();
        
        // Load all persistent fields
        EmbedId eid = new EmbedId();
        eid.setId(new Random().nextInt());
        eid.setCode("IdCode");        
        fa.setEmbedId(eid); // embedded id
        
        Collection<EmbedPropAccess> elc = new ArrayList<EmbedPropAccess>();
        EmbedPropAccess epa1 = new EmbedPropAccess("George", "Washington");
        EmbedPropAccess epa2 = new EmbedPropAccess("James", "Carter");
        elc.add(epa1);
        elc.add(epa2);                
        fa.setElementCollection(elc); // element collection of embeddables
        
        EmbedFieldAccess efa = new EmbedFieldAccess();
        efa.setFirstName("The");
        efa.setLastName("President");
        fa.setEmbedField(efa); // embedded
        
        fa.setName("FieldAccessPropStratsEntity");  // basic
        
        PropAccess propa = new PropAccess();
        propa.setStrProp("PropAccess");
        fa.setManyToOne(propa); // many to one
        
        Collection<FieldAccess> fac = new ArrayList<FieldAccess>();
        FieldAccess fae = new FieldAccess();
        fae.setStrField("FieldAccess");
        fac.add(fae);
        fa.setOneToMany(fac); // one to many
        
        PropAccess pa = new PropAccess();
        pa.setStrProp("PropAccess");
        fa.setOneToOne(pa); // one to one
       
        em.getTransaction().begin();
        em.persist(fa);
        em.getTransaction().commit();
        
        em.clear();
        // Verify list of persistent fields
        FieldAccessPropStratsEntity newpa = 
            em.find(FieldAccessPropStratsEntity.class, eid);
        assertNotNull(newpa);
        // simple key validation
        assertEquals(newpa.getEmbedId(), eid);

        // Verify the persistent member names
        MetaDataRepository mdr = 
            em.getConfiguration().getMetaDataRepositoryInstance();
        
        ClassMetaData cmd = mdr.getMetaData(FieldAccessPropStratsEntity.class, 
            null, true);
        // Assert expected persistent fields and properties were created
        assertNotNull(cmd.getField("eid"));
        assertNotNull(cmd.getField("elementCollection"));
        assertNotNull(cmd.getField("embedField"));
        assertNotNull(cmd.getField("version"));
        assertNotNull(cmd.getField("manyToOne"));
        assertNotNull(cmd.getField("oneToMany"));
        assertNotNull(cmd.getField("oneToOne"));
        assertNotNull(cmd.getField("manyToMany"));

        // Name has a matching getter/setter.  Make sure the access type
        // is property & not field
        assertNotNull(cmd.getField("name"));
        assertTrue((cmd.getField("name").getAccessType() & AccessCode.PROPERTY)
            == AccessCode.PROPERTY);

        // Assert mappings were not created for fields or properties which 
        // should not be persistent
        assertNull(cmd.getField("embedId"));
        assertNull(cmd.getField("m2one"));
        assertNull(cmd.getField("one2m"));
        assertNull(cmd.getField("one2one"));
        assertNull(cmd.getField("ecoll"));
        assertNull(cmd.getField("embed"));
        assertNull(cmd.getField("ver"));
        assertNull(cmd.getField("m2m"));
        
        em.close();
    }

    /**
     * Verifies the use of a map of embeddables containing a nested
     * mixed access embeddable. 
     */
    public void testMapWithNestedEmbeddable() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        MenuItem mi = new MenuItem();
        mi.setName("PB & J Sandwich");
        
        Map<String, Ingredient> ingredients = new HashMap<String, Ingredient>();
        mi.setIngredients(ingredients);
        
        Ingredient i1 = new Ingredient("Peanut Butter");
        i1.setDescription("Edible brown paste, made from peanuts");
        Quantity q1 = new Quantity(1.0, "Tbsp");
        i1.setQuantity(q1);
        ingredients.put("Peanut Butter", i1);
        
        Ingredient i2 = new Ingredient("Jelly");
        i2.setDescription("Sweet gel, made from fruit");
        Quantity q2 = new Quantity(1.5, "Tbsp");
        i2.setQuantity(q2);
        ingredients.put("Jelly", i2);

        Ingredient i3 = new Ingredient("Bread");
        i3.setDescription("Baked material, made from flour and water");
        Quantity q3 = new Quantity(2.0, "Slice");
        i3.setQuantity(q3);
        ingredients.put("Bread", i3);

        em.getTransaction().begin();
        em.persist(mi);
        em.getTransaction().commit();
        em.clear();
        
        MenuItem mi2 = em.find(MenuItem.class, mi.getId());
        
        assertEquals(mi2.getId(), mi.getId());
        Map<String, Ingredient> ing2 = mi2.getIngredients();
        assertTrue(ing2.containsKey("Peanut Butter"));
        Quantity q = ing2.get("Peanut Butter").getQuantity();
        assertNotNull(q);
        assertEquals(1.0, q.getAmount());
        assertEquals("Tbsp", q.getUnitOfMeasure());
        assertTrue(ing2.containsKey("Jelly"));
        q = ing2.get("Jelly").getQuantity();
        assertNotNull(q);
        assertEquals(1.5, q.getAmount());
        assertEquals("Tbsp", q.getUnitOfMeasure());
        assertTrue(ing2.containsKey("Bread"));
        q = ing2.get("Bread").getQuantity();
        assertNotNull(q);
        assertEquals(2.0, q.getAmount());
        assertEquals("Slice", q.getUnitOfMeasure());

        em.remove(mi2);
        
        em.close();
    }
    
    /*
     * Simple method to verify if an exception is of the correct type and
     * that it contains the expected message fragments.
     */
    private boolean verifyException(Exception e, Class c, String...strings) {
        if (c.isInstance(e)) {
            String exMessage = e.getMessage();
            for (String msg : strings) {
                if (!exMessage.contains(msg))
                    return false;
            }
            return true;
        }
        return false;
    }
}
