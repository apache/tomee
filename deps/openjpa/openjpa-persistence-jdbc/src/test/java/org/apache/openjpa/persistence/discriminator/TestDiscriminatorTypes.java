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
package org.apache.openjpa.persistence.discriminator;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDiscriminatorTypes extends SingleEMFTestCase {

    public void setUp() {
        super.setUp(CharAbstractEntity.class, CharLeafEntity.class,
                CharRootEntity.class, IntegerAbstractEntity.class,
                IntegerLeafEntity.class, IntegerRootEntity.class,
                StringAbstractEntity.class, StringLeafEntity.class,
                StringRootEntity.class, CLEAR_TABLES);
    }

    public void testCharDiscriminators() {
        EntityManager em = emf.createEntityManager(); // load types

        Discriminator discrim = getMapping("CharAbstractEntity")
                .getDiscriminator();
        assertEquals(new Character('C'), discrim.getValue()); // Generated
        assertEquals(JavaTypes.CHAR, discrim.getJavaType());

        discrim = getMapping("chrLeaf").getDiscriminator();
        assertEquals(new Character('c'), discrim.getValue());
        assertEquals(JavaTypes.CHAR, discrim.getJavaType());

        discrim = getMapping("CharRootEntity").getDiscriminator();
        assertEquals(new Character('R'), discrim.getValue());
        assertEquals(JavaTypes.CHAR, discrim.getJavaType());
        
        CharLeafEntity leaf = new CharLeafEntity();
        CharRootEntity root = new CharRootEntity();
        em.getTransaction().begin();
        em.persist(leaf);
        em.persist(root);
        em.getTransaction().commit();
        
        em.refresh(leaf);
        em.refresh(root);
        
        em.clear();
        
        CharLeafEntity leaf2 = em.find(CharLeafEntity.class, leaf.getId());
        CharRootEntity root2 = em.find(CharRootEntity.class, root.getId());
        
        assertNotNull(leaf2);
        assertNotNull(root2);
        em.close();
    }

    public void testIntDiscriminators() {
        EntityManager em = emf.createEntityManager(); // load the types

        Discriminator discrim = getMapping("IntegerAbstractEntity")
                .getDiscriminator();
        assertEquals(new Integer("IntegerAbstractEntity".hashCode()), discrim
                .getValue()); // Generated value
        assertEquals(JavaTypes.INT, discrim.getJavaType());

        discrim = getMapping("intLeaf").getDiscriminator();
        assertEquals(new Integer("intLeaf".hashCode()), discrim.getValue());
        assertEquals(JavaTypes.INT, discrim.getJavaType());

        discrim = getMapping("IntegerRootEntity").getDiscriminator();
        assertEquals(new Integer(10101), discrim.getValue());
        assertEquals(JavaTypes.INT, discrim.getJavaType());

        IntegerLeafEntity leaf = new IntegerLeafEntity();
        IntegerRootEntity root = new IntegerRootEntity();
        em.getTransaction().begin();
        em.persist(leaf);
        em.persist(root);
        em.getTransaction().commit();
        
        em.refresh(leaf);
        em.refresh(root);
        
        em.clear();

        IntegerLeafEntity leaf2 =
                em.find(IntegerLeafEntity.class, leaf.getId());
        IntegerRootEntity root2 =
                em.find(IntegerRootEntity.class, root.getId());
        
        assertNotNull(leaf2);
        assertNotNull(root2);
        em.close();
    }

    public void testStringDiscriminators() {
        EntityManager em = emf.createEntityManager(); // load the types
        Discriminator discrim = getMapping("StringAbstractEntity")
                .getDiscriminator();
        assertEquals("StringAbstractEntity", discrim.getValue()); // Generated
        assertEquals(JavaTypes.STRING, discrim.getJavaType());

        discrim = getMapping("strLeaf").getDiscriminator();
        assertEquals("strLeaf", discrim.getValue());
        assertEquals(JavaTypes.STRING, discrim.getJavaType());

        discrim = getMapping("StringRootEntity").getDiscriminator();
        assertEquals("StringRoot", discrim.getValue());
        assertEquals(JavaTypes.STRING, discrim.getJavaType());
        
        StringLeafEntity leaf = new StringLeafEntity();
        StringRootEntity root = new StringRootEntity();
        em.getTransaction().begin();
        em.persist(leaf);
        em.persist(root);
        em.getTransaction().commit();
        
        em.refresh(leaf);
        em.refresh(root);
        
        em.clear();
        
        StringLeafEntity leaf2 = em.find(StringLeafEntity.class, leaf.getId());
        StringRootEntity root2 = em.find(StringRootEntity.class, root.getId());
        
        assertNotNull(leaf2);
        assertNotNull(root2);
        em.close();
    }

    public void testExistsQuery() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        StringRootEntity e = new StringRootEntity();
        e.setName("foo");
        em.persist(e);

        e = new StringRootEntity();
        e.setName("foo");
        em.persist(e);

        e = new StringRootEntity();
        e.setName("bar");
        em.persist(e);

        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        Query q = em.createQuery("select o from StringAbstractEntity o " +
            "where exists (select o2 from StringLeafEntity o2)");
        List<StringAbstractEntity> list = q.getResultList();
        assertEquals(0, list.size());
        for (StringAbstractEntity entity : list)
            assertTrue(entity instanceof StringLeafEntity);
        em.close();
    }
}
