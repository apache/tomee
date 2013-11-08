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
package org.apache.openjpa.persistence.cascade;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestMultiCascadePersist extends SingleEMFTestCase {    

    @Override
    public void setUp() throws Exception {
      setUp(DROP_TABLES, Vertex.class, VertexType.class, Edge.class);
    }
    
    public void testSingleTransaction() {
        OpenJPAEntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        //The flush is important to the rest of the test.  If this
        //is removed, the test works as expected.  While the flush
        //at this point in the test may seem odd/unnecessary, it
        //is more clear to perform a flush directly rather than
        //something (e.g. query) which would cause a flush under
        //the covers.  See OPENJPA-2051 for more details.
        em.flush();

        VertexType defaultType = new VertexType( "default" );
        VertexType specialType = new VertexType( "special" );

        em.persist(defaultType);
        em.persist(specialType);

        Vertex src = new Vertex( defaultType );
        Vertex target = new Vertex( specialType );
        
        Edge t = src.newEdge( target );
        assertNotNull( t );

        em.persist(src);

        tx.commit();

        TypedQuery<Edge> q = em.createQuery( "SELECT t FROM Edge t", Edge.class );
        List<Edge> resultList = q.getResultList();

        assertEquals( 1, resultList.size() );
        assertEquals( 2, findAllVertexType(em).size() );
        if (emf.getConfiguration().getCompatibilityInstance().getResetFlushFlagForCascadePersist()){
        	assertEquals( 2, findAllVertex(em).size() );
        }
        else{
        	//There *should* be 2 Vertex....but by default we can not fix this without a
        	//compatibility flag.
       	    assertEquals( 1, findAllVertex(em).size() );
        }
    }

    public VertexType findVertexTypeByName(EntityManager em, String name ) {
        try {
            TypedQuery<VertexType> query = em.createNamedQuery( "VertexType.findByName",
                    VertexType.class );
            query.setParameter( 1, name );
            return query.getSingleResult();
        } catch ( NoResultException nre ) {
            return null;
        }
    }
    
    public List<VertexType> findAllVertexType(EntityManager em) {
        try {
            TypedQuery<VertexType> query = em.createNamedQuery( "VertexType.findAll",
                    VertexType.class );
            return query.getResultList();
        } catch ( NoResultException nre ) {
            return Collections.emptyList();
        }
    }
    
    public List<Vertex> findAllVertex(EntityManager em) {
        try {
            TypedQuery<Vertex> query = em.createNamedQuery( "Vertex.findAll",
                    Vertex.class );
            return query.getResultList();
        } catch ( NoResultException nre ) {
            return Collections.emptyList();
        }
    }    
}



