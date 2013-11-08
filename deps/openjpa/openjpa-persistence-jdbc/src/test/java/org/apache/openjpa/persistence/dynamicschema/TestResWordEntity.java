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
package org.apache.openjpa.persistence.dynamicschema;

import java.util.Random;
import javax.persistence.EntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <b>TestResWordEntity</b> is used to test the case where reserved words are 
 * translated when multiple schema factories are invoked. In this test the 
 * SynchronizeMapping setting will cause the DynamicSchemaFactory to be 
 * invoked, and the SchemaFactory setting will cause the LazySchemaFactory to
 * be invoked. 
 */
public class TestResWordEntity extends SingleEMFTestCase {

    public void setUp() {
    }

    public void testMultipleSchemafactories1() {
        OpenJPAEntityManagerFactorySPI emf = 
            createEMF(ResWordEntity.class,
            "openjpa.jdbc.SynchronizeMappings","buildSchema(ForeignKeys=true)", 
            "openjpa.jdbc.SchemaFactory","native(ForeignKeys=true)");

        EntityManager em = emf.createEntityManager();
        ResWordEntity pe = new ResWordEntity();

        pe.setId(new Random().nextInt());
        pe.setAlias("AliasVal");

        em.getTransaction().begin();
        em.persist(pe);
        em.getTransaction().commit();

        em.clear();
        ResWordEntity re = em.find(ResWordEntity.class, pe.getId());
        em.clear();
        re = em.find(ResWordEntity.class, pe.getId());

        closeEM(em);
        closeEMF(emf);
    }

    public void testMultipleSchemafactories2() {
        OpenJPAEntityManagerFactorySPI emf = 
            createEMF(ResWordEntity.class,
            "openjpa.jdbc.SchemaFactory","native(ForeignKeys=true)",
            "openjpa.jdbc.SynchronizeMappings","buildSchema(ForeignKeys=true)");

        EntityManager em = emf.createEntityManager();
        ResWordEntity pe = new ResWordEntity();

        pe.setId(new Random().nextInt());
        pe.setAlias("AliasVal");

        em.getTransaction().begin();
        em.persist(pe);
        em.getTransaction().commit();

        em.clear();
        ResWordEntity re = em.find(ResWordEntity.class, pe.getId());
        em.clear();
        re = em.find(ResWordEntity.class, pe.getId());

        closeEM(em);
        closeEMF(emf);
    }

    public void testMultipleSchemafactories3() {
        OpenJPAEntityManagerFactorySPI emf = 
            createEMF(ResWordEntity.class,
            "openjpa.jdbc.SynchronizeMappings","buildSchema(ForeignKeys=true)");

        EntityManager em = emf.createEntityManager();
        ResWordEntity pe = new ResWordEntity();

        pe.setId(new Random().nextInt());
        pe.setAlias("AliasVal");

        em.getTransaction().begin();
        em.persist(pe);
        em.getTransaction().commit();

        em.clear();
        ResWordEntity re = em.find(ResWordEntity.class, pe.getId());
        em.clear();
        re = em.find(ResWordEntity.class, pe.getId());

        closeEM(em);
        closeEMF(emf);
    }

    public void testMultipleSchemafactories4() {
        OpenJPAEntityManagerFactorySPI emf = 
            createEMF(ResWordEntity.class,
            "openjpa.jdbc.SchemaFactory","native(ForeignKeys=true)");

        EntityManager em = emf.createEntityManager();
        ResWordEntity pe = new ResWordEntity();

        pe.setId(new Random().nextInt());
        pe.setAlias("AliasVal");

        em.getTransaction().begin();
        em.persist(pe);
        em.getTransaction().commit();

        em.clear();
        ResWordEntity re = em.find(ResWordEntity.class, pe.getId());
        em.clear();
        re = em.find(ResWordEntity.class, pe.getId());

        closeEM(em);
        closeEMF(emf);
    }
}
