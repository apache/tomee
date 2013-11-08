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
package org.apache.openjpa.persistence.jpql.version;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Verifies that the version field is returned when part of a join statement.
 * See OPENJPA-2343.
 */
public class TestJoinVersionField extends SingleEMFTestCase {

    public void setUp(){
        setUp(CLEAR_TABLES, Author.class,Document.class);
        createTestData();
    }
    
    public void testGetDocuments(){
        EntityManager em = emf.createEntityManager();
        String str = "SELECT doc FROM Document doc JOIN doc.author auth";
        TypedQuery<Document> query = em.createQuery(str, Document.class);
        List<Document> documentList =  query.getResultList();

        for (Document doc : documentList) {        
            assertEquals("Author version field should have a value of 1.",
                1, doc.getAuthor().getVersion());
        }
        
        em.close();
    }
    
    /**
     * Prior to OPENJPA-2343, the version field in the Author entity is returned 
     * as null.
     */
    public void testGetDocumentsByExplicitAttributeSelection(){
        EntityManager em = emf.createEntityManager();
        String str = "SELECT doc.id, auth.id, auth.version FROM Document doc JOIN doc.author auth";
        Query query = em.createQuery(str);
        List<Object[]> objectList = query.getResultList();

        for (Object[] objects : objectList) {
            assertEquals("Author version field should have a value of 1.",1,objects[2]);
        }
    }
    
    public void createTestData() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        
        Author author = new Author();
        author.setId(10);
        em.persist(author);

        Document document = new Document();
        document.setId(2);
        document.setAuthor(author);
        em.persist(document);

        em.getTransaction().commit();
        em.close();
    }
}
