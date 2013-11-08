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
package org.apache.openjpa.persistence.inheritance.polymorphic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests a domain model with following characteristics:
 * a) A typical bidirectional ManyToOne/OneToMany relation 
 *    EntityA references a single instance of EntityB
 *    EntityB references a collection of EntityA
 * b) EntityB itself is abstract 
 * c) Many concrete subclasses of EntityB exist
 * d) EntityB uses TABLE_PER_CLASS inheritance strategy, hence no mapping table 
 *    exists for EntityB itself.
 * e) Relation field in EntityA is declared as abstract type EntityB (for which
 * f) all the domain classes i.e. EntityA, EntityB and all its subclasses is
 *    derived from an abstract MappedSuperClass which holds primary key and
 *    version fields.
 *    
 *  The test addresses a reported error [1] in mapping the above domain model.
 *  The test verifies basic persist, query and delete operations on the domain
 *  model.
 *  
 *  [1] <A HREF="https://issues.apache.org/jira/browse/OPENJPA-602">
 *  OPENJPA-602</A>}
 *        
 * @author Pinaki Poddar
 *
 */
public class TestTablePerClassInheritanceWithAbstractRoot extends
		SingleEMFTestCase {
	Class<?>[] UNJOINED_SUBCLASSES = {
			EnglishParagraph.class, 
			FrenchParagraph.class, 
			GermanParagraph.class};
	
    public void setUp() {
        setUp(CLEAR_TABLES, 
        		Translation.class, BaseEntity.class,
        		EnglishParagraph.class, FrenchParagraph.class, 
        		GermanParagraph.class, Translatable.class);
    }

    public void populate() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        /**
         * Aggregate query operations can not be performed on unjoined 
         * subclasses. Hence all concrete subclasses of abstract base
         * class is counted separately to count all Translatable instances.
         */
        
        EnglishParagraph english = new EnglishParagraph();
        FrenchParagraph french   = new FrenchParagraph();
        GermanParagraph german   = new GermanParagraph();
        
        Translation translation1 = new Translation(); 
        Translation translation2 = new Translation(); 
        Translation translation3 = new Translation(); 
        Translation translation4 = new Translation(); 
        
        english.setContent("Hello");
        french.setContent("Bon jour");
        german.setContent("Guten Tag");

        
        translation1.setTranslatable(english);
        translation2.setTranslatable(english);
        translation3.setTranslatable(french);
        translation4.setTranslatable(german);
        
        english.addTranslation(translation1);
        english.addTranslation(translation2);
        french.addTranslation(translation3);
        german.addTranslation(translation4);
        
        em.persist(translation1);
        em.persist(translation2);
        em.persist(translation3);
        em.persist(translation4);
        em.getTransaction().commit();
        em.close();
    }

	@SuppressWarnings("unchecked")
    public void testConsistency() {
        OpenJPAEntityManager em = emf.createEntityManager();

        int nTranslatableBefore = count(UNJOINED_SUBCLASSES);
        int nTranslationBefore = count(Translation.class);

        populate();
		int nTranslatableAfter = count(UNJOINED_SUBCLASSES);
		int nTranslationAfter  = count(Translation.class);
		
		assertEquals(nTranslatableBefore+3, nTranslatableAfter);
		assertEquals(nTranslationBefore+4, nTranslationAfter);
		
		/**
         * Verify that if A refers to B then A must be a member of the set
		 * referred by B
		 */
		em.getTransaction().begin();
        List<Translation> result = em.createQuery("SELECT p FROM Translation p")
			.getResultList();
		assertTrue(!result.isEmpty());
		for (Translation translation : result) {
			assertTrue(translation.getTranslatable()
                    .getTranslations().contains(translation));
		}
		em.getTransaction().rollback();
		em.close();
	}
	
	
    void linkConsistently(Translation translation, Translatable translatable) {
		translatable.addTranslation(translation);
		translation.setTranslatable(translatable);
	}
	
	/**
     * Count the number of instances in the given class by aggregate JPQL query.
	 */
	public int count(Class c) {
		OpenJPAEntityManager em = emf.createEntityManager();
		Number n = ((Number) em.createQuery("SELECT COUNT(p) FROM " + 
                c.getSimpleName() + " p").getSingleResult()).intValue();
		closeEM(em);
		return n.intValue();
	}
	
	/**
     * Count total number of instances of all the given classes by separate JPQL
	 * aggregate query. Useful when a base class has unjoined subclasses.
	 */
	public int count(Class<?>... classes) {
		int total = 0;
		for (Class<?> c:classes) {
			total += count(c);
		}
		return total;
	}

    public void testEntityTypeForTablePerClassInheritance() {
        populate();
        OpenJPAEntityManager em = emf.createEntityManager();
        String query = "select tr from Translatable tr join tr.translations t where " +
                "TYPE(tr) = EnglishParagraph";
        List rs = em.createQuery(query).getResultList();
        assertEquals(2, rs.size());
        for (int i=0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof EnglishParagraph);
        
        query = "select distinct tr from Translatable tr join tr.translations t where " +
            "TYPE(tr) = EnglishParagraph or TYPE(tr) = FrenchParagraph";
        rs = em.createQuery(query).getResultList();
        assertEquals(2, rs.size());
        for (int i=0; i < rs.size(); i++)
            assertTrue(!(rs.get(i) instanceof GermanParagraph));
        
        query = "select distinct tr from Translatable tr join tr.translations t where " +
            "TYPE(tr) in (?1, ?2)";
        try {
            rs = em.createQuery(query).setParameter(1, EnglishParagraph.class).
            setParameter(2, FrenchParagraph.class).getResultList();
        } catch(ArgumentException e) {
            // as expected
            //System.out.println(e.getMessage());
        }
        
        query = "select tr from Translatable tr join tr.translations t where " +
            "TYPE(tr) <> EnglishParagraph";
        try {
            rs = em.createQuery(query).getResultList();
        } catch(ArgumentException e) {
            // as expected
            //System.out.println(e.getMessage());
        }

        String query1 = "select t from Translation t where TYPE(t.translatable) = EnglishParagraph";
        try {
            rs = em.createQuery(query1).getResultList();
        } catch(ArgumentException e) {
            // as expected
            //System.out.println(e.getMessage());
        }

        query1 = "select tr from Translatable tr join tr.translations t where " +
                "TYPE(tr) in ?1";
        Collection<Class <?>> params = new ArrayList(2);
        params.add(EnglishParagraph.class);
        params.add(FrenchParagraph.class);
        try {
            rs = em.createQuery(query).setParameter(1, params).getResultList();
        } catch(ArgumentException e) {
            // as expected
            //System.out.println("e.getMessages()");
        } finally {
            em.close();
        }
    }
}
