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
package org.apache.openjpa.persistence.jpql.expressions;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.jpql.entities.IColumnEntity;
import org.apache.openjpa.persistence.jpql.entities.INameEntity;
import org.apache.openjpa.persistence.jpql.entities.IOrderedElements;
import org.apache.openjpa.persistence.jpql.entities.IOrderedEntity;
import org.apache.openjpa.persistence.jpql.entities.OrderedElementEntity;
import org.apache.openjpa.persistence.jpql.entities.OrderedManyToManyEntity;
import org.apache.openjpa.persistence.jpql.entities.OrderedNameEntity;
import org.apache.openjpa.persistence.jpql.entities.OrderedOneToManyEntity;
import org.apache.openjpa.persistence.jpql.entities.UnorderedNameEntity;
import org.apache.openjpa.persistence.jpql.entities.XMLOrderedElementEntity;
import org.apache.openjpa.persistence.jpql.entities.XMLOrderedManyToManyEntity;
import org.apache.openjpa.persistence.jpql.entities.XMLOrderedNameEntity;
import org.apache.openjpa.persistence.jpql.entities.XMLOrderedOneToManyEntity;
import org.apache.openjpa.persistence.jpql.entities.XMLUnorderedNameEntity;
import org.apache.openjpa.persistence.proxy.TreeNode;
import org.apache.openjpa.persistence.test.JPAEntityClassEnum;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test JPQL Index function on O2M, M2M and Element collections using annotations and XML.
 *  
 * @author Catalina Wei, Albert Lee, Donald Woods
 */
public class TestIndex extends SingleEMFTestCase {
    
    private Log log = null;
    
    private enum JPQLIndexEntityClasses implements JPAEntityClassEnum {
        OrderedElementEntity(OrderedElementEntity.class),
        OrderedOneToManyEntity(OrderedOneToManyEntity.class),
        OrderedManyToManyEntity(OrderedManyToManyEntity.class),
        XMLOrderedElementEntity(XMLOrderedElementEntity.class),
        XMLOrderedOneToManyEntity(XMLOrderedOneToManyEntity.class),
        XMLOrderedManyToManyEntity(XMLOrderedManyToManyEntity.class),
        UnorderedNameEntity(UnorderedNameEntity.class),
        XMLUnorderedNameEntity(XMLUnorderedNameEntity.class),
        OrderedNameEntity(OrderedNameEntity.class);

        private Class<?> clazz;
        private String fullEntityName;
        private String entityName;

        JPQLIndexEntityClasses(Class<?> clazz) {
            this.clazz = clazz;
            fullEntityName = clazz.getName();
            entityName = fullEntityName.substring(getEntityClassName()
                .lastIndexOf('.') + 1);
        }

        public Class<?> getEntityClass() {
            return clazz;
        }

        public String getEntityClassName() {
            return fullEntityName;
        }

        public String getEntityName() {
            return entityName;
        }
    }

    private static final String[] Element_Names = { "A_Element", "B_Element",
        "C_Element", "D_Element", "E_Element", "F_Element", };

    @Override
    protected String getPersistenceUnitName() {
        // this sets up the testcase code so our EMF is created and cleaned up for us
        return "JPQLIndex";
    }

    @Override
    public void setUp() {
        super.setUp(CLEAR_TABLES, TreeNode.class, 
            OrderedElementEntity.class, UnorderedNameEntity.class,
            OrderedOneToManyEntity.class, OrderedManyToManyEntity.class,
            OrderedNameEntity.class);
            // XMLOrderedOneToManyEntity.class, XMLOrderedManyToManyEntity.class,
            // XMLOrderedElementEntity.class, XMLUnorderedNameEntity.class,
            // XMLOrderedNameEntity.class);

        log =  emf.getConfiguration().getLog("test");
    }

    // original testcase by Catalina
    public void testO2MTreeQueryIndex() {
        int[] fanOuts = {2,3,4};
        createTreeNodeEntities(fanOuts);
        EntityManager em = emf.createEntityManager();
        String query = "SELECT index(c) from TreeNode t, in (t.childern) c" +
            " WHERE index(c) = 2"; 
        
        List<Object> rs = em.createQuery(query).getResultList();
        for (Object t: rs)
            assertEquals(2, Integer.parseInt(t.toString()));
        
        em.close();                
    }

    // Testcases added by Donald with code reused from annonxml tests by Albert
    public void testO2MQueryIndex() {
        createEntities(JPQLIndexEntityClasses.OrderedOneToManyEntity, UnorderedNameEntity.class);
        verifyEntities(JPQLIndexEntityClasses.OrderedOneToManyEntity, UnorderedNameEntity.class);
    }

    public void testO2MXMLQueryIndex() {
        createEntities(JPQLIndexEntityClasses.XMLOrderedOneToManyEntity, XMLUnorderedNameEntity.class);
        verifyEntities(JPQLIndexEntityClasses.XMLOrderedOneToManyEntity, XMLUnorderedNameEntity.class);
    }

    public void testM2MQueryIndex() {
        createEntities(JPQLIndexEntityClasses.OrderedManyToManyEntity, OrderedNameEntity.class);
        verifyEntities(JPQLIndexEntityClasses.OrderedManyToManyEntity, OrderedNameEntity.class);
    }

    public void testM2MXMLQueryIndex() {
        createEntities(JPQLIndexEntityClasses.OrderedManyToManyEntity, XMLOrderedNameEntity.class);
        verifyEntities(JPQLIndexEntityClasses.OrderedManyToManyEntity, XMLOrderedNameEntity.class);
    }

    public void testElementQueryIndex() {
        createEntities(JPQLIndexEntityClasses.OrderedElementEntity, String.class);
        verifyEntities(JPQLIndexEntityClasses.OrderedElementEntity, String.class);
    }

    public void testElementXMLQueryIndex() {
        createEntities(JPQLIndexEntityClasses.XMLOrderedElementEntity, String.class);
        verifyEntities(JPQLIndexEntityClasses.XMLOrderedElementEntity, String.class);
    }

    /**
     * Create and persist a uniform OneToMany tree with given fan out.
     */
    private TreeNode createTreeNodeEntities(int[] original) {
        TreeNode root = new TreeNode();
        root.createTree(original);
        assertArrayEquals(original, root.getFanOuts());
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(root);
        em.getTransaction().commit();
        em.clear();
        
        return root;
    }

    /**
     *  Asserts the given arrays have exactly same elements at the same index.
     */
    private void assertArrayEquals(int[] a, int[] b) {
        assertEquals(a.length, b.length);
        for (int i = 0; i<a.length; i++)
            assertEquals(a[i], b[i]);
    }
    
    private <C,E> void createEntities(JPQLIndexEntityClasses entityType, Class<E> elementClass)
    {
        if (IOrderedEntity.class.isAssignableFrom(entityType.getEntityClass())) {
            if (INameEntity.class.isAssignableFrom(elementClass)) {
                log.trace("** Test INameEntity modifications on IOrderedEntity.");
                createO2MEntities(entityType, (Class<INameEntity>)elementClass);
            } else if (IColumnEntity.class.isAssignableFrom(elementClass)) {
                log.trace("** Test IColumnEntity modifications on IOrderedEntity.");
                createM2MEntities(entityType, (Class<IColumnEntity>)elementClass);
            } else {
                fail("createEntities(IOrderedEntity) - Unexpected elementClass=" + elementClass.getSimpleName());
            }
        } else if (IOrderedElements.class.isAssignableFrom(entityType.getEntityClass())) {
            if (String.class.isAssignableFrom(elementClass)) {
                log.trace("** Test String modifications on IOrderedElements.");
                createOrderedElements(entityType);
            } else {
                fail("createEntities(IOrderedElements) - Unexpected elementClass=" + elementClass.getSimpleName());
            }
        } else {
            fail("createEntities() - Unexpected entityType=" + entityType.getEntityName());            
        }
    }
        
    private void createO2MEntities(JPQLIndexEntityClasses entityType, Class<INameEntity> elementClass)
    {
        EntityManager em = null;
        
        try {
            Class<IOrderedEntity> entityClass =
                (Class<IOrderedEntity>)Class.forName(entityType.getEntityClassName());
            String entityClassName = entityType.getEntityName();
            String elementClassName = elementClass.getName().substring(
                elementClass.getName().lastIndexOf('.') + 1);
            Integer entityId = 1;
            
            // create the entity
            IOrderedEntity newEntity = (IOrderedEntity)constructNewEntityObject(entityType);
            newEntity.setId(entityId);
            
            // create the elements to add
            Constructor<INameEntity> elementConstrctor = elementClass.getConstructor(String.class);
            List<INameEntity> newElements = new ArrayList<INameEntity>();
            for (int i=0; i<Element_Names.length; i++) {
                newElements.add(elementConstrctor.newInstance(Element_Names[i]));
            }
            
            // add the entities
            em = emf.createEntityManager();
            em.getTransaction().begin();
            for (INameEntity newElement : newElements)
            {
                em.persist(newElement);
                newEntity.addEntity((INameEntity)newElement);
            }
            em.persist(newEntity);
            em.getTransaction().commit();
            em.clear();

            // verify the entity was stored
            IOrderedEntity findEntity = em.find(entityClass, entityId);
            assertNotNull("Found entity just created", findEntity);
            assertEquals("Verify entity id = " + entityId, entityId.intValue(), findEntity.getId());
            assertEquals("Verify entity name = " + entityClass.getName(), entityClass.getName(),
                findEntity.getClass().getName());

        } catch (Throwable t) {
            log.error(t);
            throw new RuntimeException(t);
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
                em = null;
            }
        }
    }
    
    private void createM2MEntities(JPQLIndexEntityClasses entityType, Class<IColumnEntity> elementClass)
    {
        EntityManager em = null;
        
        try {
            Class<IOrderedEntity> entityClass =
                (Class<IOrderedEntity>)Class.forName(entityType.getEntityClassName());
            String entityClassName = entityType.getEntityName();
            String elementClassName = elementClass.getName().substring(
                elementClass.getName().lastIndexOf('.') + 1);
            
            // create the EM and transaction
            em = emf.createEntityManager();
            em.getTransaction().begin();

            // create and persist the entities
            List<IOrderedEntity> newEntities = new ArrayList<IOrderedEntity>();
            for (int i=0; i<3; i++) {
                IOrderedEntity newEntity = (IOrderedEntity)constructNewEntityObject(entityType);
                newEntity.setId(i);
                em.persist(newEntity);
                newEntities.add(newEntity);
            }
            
            // create and persist the elements
            Constructor<IColumnEntity> elementConstrctor = elementClass.getConstructor(String.class);
            List<INameEntity> newElements = new ArrayList<INameEntity>();
            IColumnEntity newElement;
            for (int i=0; i<Element_Names.length; i++) {
                newElement = elementConstrctor.newInstance(Element_Names[i]);
                // add parent relationships
                newElement.setEntities(newEntities);
                em.persist(newElement);
                newElements.add(newElement);
            }
            
            // update entities with elements
            for (IOrderedEntity newEntity : newEntities) {
                newEntity.setEntities(newElements);
                em.persist(newEntity);
            }
            em.getTransaction().commit();
            em.clear();

            // verify the entities were stored
            for (int i=0; i<Element_Names.length; i++) {
                IOrderedEntity findEntity = em.find(entityClass, i);
                assertNotNull("Found entity just created", findEntity);
                assertEquals("Verify entity id = " + i, i, findEntity.getId());
                assertEquals("Verify entity name = " + entityClass.getName(), entityClass.getName(),
                    findEntity.getClass().getName());
            }
        } catch (Throwable t) {
            log.error(t);
            throw new RuntimeException(t);
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
                em = null;
            }
        }
    }
    
    private void createOrderedElements(JPQLIndexEntityClasses entityType)
    {
        EntityManager em = null;
        
        try {
            Class<IOrderedElements> entityClass =
                (Class<IOrderedElements>)Class.forName(entityType.getEntityClassName());
            String entityClassName = entityType.getEntityName();
            Integer entityId = 1;
            
            IOrderedElements newEntity = (IOrderedElements)constructNewEntityObject(entityType);
            newEntity.setId(entityId);
            List<String> namesList = new ArrayList<String>();
            for (int i=0; i<Element_Names.length; i++) {
                namesList.add(Element_Names[i]);
            }
            newEntity.setListElements(namesList);
            // add the entity
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(newEntity);
            em.getTransaction().commit();
            em.clear();

            // verify the entity was stored
            log.trace("Verifing the entity was stored");
            IOrderedElements findEntity = em.find(entityClass, entityId);
            assertNotNull("Found entity just created", findEntity);
            assertEquals("Verify entity id = " + entityId, entityId.intValue(), findEntity.getId());
            assertEquals("Verify entity name = " + entityClass.getName(), entityClass.getName(),
                findEntity.getClass().getName());

        } catch (Throwable t) {
            log.error(t);
            throw new RuntimeException(t);
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
                em = null;
            }
        }
    }
    
    private <C,E> void verifyEntities(JPQLIndexEntityClasses entityType, Class<E> elementClass)
    {
        if (IOrderedEntity.class.isAssignableFrom(entityType.getEntityClass())) {
            if (INameEntity.class.isAssignableFrom(elementClass)) {
                log.trace("** Verify INameEntity modifications on IOrderedEntity.");
                verifyO2MEntities(entityType, (Class<INameEntity>)elementClass);
            } else if (IColumnEntity.class.isAssignableFrom(elementClass)) {
                log.trace("** Verify IColumnEntity modifications on IOrderedEntity.");
                verifyM2MEntities(entityType, (Class<IColumnEntity>)elementClass);
            } else {
                fail("verifyEntities(IOrderedEntity) - Unexpected elementClass=" + elementClass.getSimpleName());
            }
        } else if (IOrderedElements.class.isAssignableFrom(entityType.getEntityClass())) {
            if (String.class.isAssignableFrom(elementClass)) {
                log.trace("** Test String modifications on IOrderedElements.");
                verifyOrderedElements(entityType);
            } else {
                fail("verifyEntities(IOrderedElements) - Unexpected elementClass=" + elementClass.getSimpleName());
            }
        } else {
            fail("verifyEntities() - Unexpected entityType=" + entityType.getEntityName());            
        }
    }
    
    private <E> void verifyO2MEntities(JPQLIndexEntityClasses entityType, Class<INameEntity> elementClass)
    {
        EntityManager em = null;
        
        try {
            Class<IOrderedEntity> entityClass = (Class<IOrderedEntity>)Class.forName(entityType.getEntityClassName());
            String entityClassName = entityType.getEntityName();
            entityClassName = entityClassName.substring(entityClassName.lastIndexOf('.') + 1);

            if (log.isTraceEnabled()) {
                log.trace("Query " + entityClassName + " and verify 'entities' collection has "
                    + Element_Names.length + " elements in this order: "
                    + Arrays.toString(Element_Names));
            }
            
            em = emf.createEntityManager();
            em.clear();
            int idx = 0;
            for (String expectedEntityName : Element_Names) {
                Query q = em.createQuery("select w from " + entityClassName
                    + " o join o.entities w where index(w) = " + idx);
                List<E> res = (List<E>)q.getResultList();
                assertEquals("  Verify query returns 1 element for index " + idx, 1, res.size());
                if (res.size() == 1) {
                    Object oo = res.get(0);
                    assertEquals("  Verify element type is " + elementClass.getName(), elementClass.getName(),
                        oo.getClass().getName());
                    try {
                        String name = (String) elementClass.getMethod("getName").invoke(oo);
                        assertEquals("  Verify element value is '"
                            + expectedEntityName + "'", expectedEntityName, name);
                    } catch (Exception e) {
                        log.error("  Caught unexpected exception:" + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
                ++idx;
            }
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            if (em != null) {
                em.close();
                em = null;
            }
        }
    }
    
    private <E> void verifyM2MEntities(JPQLIndexEntityClasses entityType, Class<IColumnEntity> elementClass)
    {
        EntityManager em = null;
        
        try {
            Class<IOrderedEntity> entityClass = (Class<IOrderedEntity>)Class.forName(entityType.getEntityClassName());
            String entityClassName = entityType.getEntityName();
            entityClassName = entityClassName.substring(entityClassName.lastIndexOf('.') + 1);
            String elementClassName = entityType.getEntityName();
            elementClassName = elementClassName.substring(elementClassName.lastIndexOf('.') + 1);

            if (log.isTraceEnabled()) {
                log.trace("Query " + entityClassName + " and verify 'entities' collection has "
                    + Element_Names.length + " elements in this order: "
                    + Arrays.toString(Element_Names));
            }
            
            em = emf.createEntityManager();
            em.clear();
            int idx = 0, idx2 = 0;
            for (String expectedEntityName : Element_Names) {
                Query q = em.createQuery("select w from " + entityClassName
                    + " o join o.entities w where index(w) = " + idx);
                List<E> res = (List<E>)q.getResultList();
                assertEquals("  Verify query returns 1 element for index " + idx, 1, res.size());
                if (res.size() == 1) {
                    Object oo = res.get(0);
                    assertEquals("  Verify element type is " + elementClass.getName(), elementClass.getName(),
                        oo.getClass().getName());
                    try {
                        String name = (String) elementClass.getMethod("getName").invoke(oo);
                        assertEquals("  Verify element value is '"
                            + expectedEntityName + "'", expectedEntityName, name);
                        
                        if (log.isTraceEnabled()) {
                            log.trace("Query " + elementClassName + " and verify 'entities' collection content");
                        }
                        Query q2 = em.createQuery("select w from " + elementClassName
                            + " o join o.entities w where index(w) = " + idx2);
                        List<E> res2 = (List<E>)q.getResultList();
                        assertEquals("  Verify query returns 1 entity for index " + idx2, 1, res2.size());
                        if (res2.size() == 1) {
                            Object oo2 = res2.get(0);
                            assertEquals("  Verify entity type is " + entityClass.getName(), entityClass.getName(),
                                oo2.getClass().getName());
                            try {
                                String name2 = (String) entityClass.getMethod("getName").invoke(oo2);
                                assertEquals("  Verify entity value is '"
                                    + expectedEntityName + "'", expectedEntityName, name);
                            } catch (Exception e) {
                                log.error("  Caught unexpected exception:" + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (Exception e) {
                        log.error("  Caught unexpected exception:" + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
                ++idx;
            }
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            if (em != null) {
                em.close();
                em = null;
            }
        }
    }
    
    private <E> void verifyOrderedElements(JPQLIndexEntityClasses entityType)
    {
        EntityManager em = null;
        
        try {
            Class<IOrderedEntity> entityClass = (Class<IOrderedEntity>)Class.forName(entityType.getEntityClassName());
            String entityClassName = entityType.getEntityName();
            entityClassName = entityClassName.substring(entityClassName.lastIndexOf('.') + 1);

            if (log.isTraceEnabled()) {
                log.trace("Query " + entityClassName + " and verify 'elements' collection has "
                    + Element_Names.length + " elements in this order: "
                    + Arrays.toString(Element_Names));
            }
            
            em = emf.createEntityManager();
            em.clear();
            int idx = 0;
            for (String expectedEntityName : Element_Names) {
                Query q = em.createQuery("select w from " + entityClassName
                    + " o join o.elements w where index(w) = " + idx);
                List<E> res = (List<E>)q.getResultList();
                assertEquals("  Verify query returns 1 element for index " + idx, 1, res.size());
                if (res.size() == 1) {
                    Object oo = res.get(0);
                    assertEquals("  Verify element type is String", String.class.getName(),
                        oo.getClass().getName());
                    String name;
                    try {
                        name = (String) oo.toString();
                        assertEquals("  Verify element value is '"
                            + expectedEntityName + "'", expectedEntityName, name);
                    } catch (Exception e) {
                        log.error("  Caught unexpected exception:" + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
                ++idx;
            }
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            if (em != null) {
                em.close();
                em = null;
            }
        }
    }
    
}
