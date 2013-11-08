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
package org.apache.openjpa.persistence.simple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestFlushBeforeDetach extends SQLListenerTestCase {

    private int _id;
    
    public void setUp() {
      setUp(Item.class,"openjpa.Compatibility", 
                "default(flushBeforeDetach=false,copyOnDetach=true)");
        persistSampleEntity();
    }
    
    private void persistSampleEntity() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Item i = new Item();
        em.persist(i);
        em.getTransaction().commit();
        em.refresh(i);
        _id = i.getItemId();
        em.close();
    }

    public void testClear() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Item i = em.find(Item.class, _id);

        i.setItemData("ABCD");

        em.clear();
        em.getTransaction().rollback();
        assertNotSQL("UPDATE ITEM.*");
        em.close();
    }

    public void testDetach() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Item i = em.find(Item.class, _id);

        i.setItemData("EFGH");

        OpenJPAPersistence.cast(em).detach(i);
        em.getTransaction().rollback();
        assertNotSQL("UPDATE ITEM SET.*");
        em.close();
    }
    
    public void testDetachAll() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Item i = em.find(Item.class, _id);

        i.setItemData("IJKL");

        OpenJPAPersistence.cast(em).detachAll(i);
        em.getTransaction().rollback();
        assertNotSQL("UPDATE ITEM SET.*");
        em.close();
    }
    
    public void testDetachAllCollection() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Item i = em.find(Item.class, _id);

        i.setItemData("MNOP");

        Collection<Item> c = new ArrayList<Item>();
        c.add(i);
        OpenJPAPersistence.cast(em).detachAll(c);
        em.getTransaction().rollback();
        assertNotSQL("UPDATE ITEM SET.*");
        em.close();
    }

    public void testSerialize() throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Item i = em.find(Item.class, _id);

        i.setItemData("QRSTU");

        serializeObject(i);

        em.getTransaction().rollback();
        assertNotSQL("UPDATE ITEM SET.*");
        em.close();
    }

    /**
     * Helper to serialize an object to a byte[]
     */
    private Object serializeObject(Object orig) throws Exception {
        Object deserialized = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(orig);

            ByteArrayInputStream bais =
                    new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);

            deserialized = ois.readObject();
            return deserialized;
        } finally {
            oos.close();
            ois.close();
        }
    }
}
