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
package org.apache.openjpa.persistence.query;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * This test was added for OPENJPA-1999. 'Add' support for allowing positional parameters to start at something other
 * than 1 and allow for missing parameters.
 */
public class TestQueryConvertPositionalParameters extends SingleEMFTestCase {
    EntityManager _em;
    long _id1;
    String _name1;
    String _val1;
    long _id2;
    String _name2;
    String _val2;

    @Override
    public void setUp() {
        super.setUp(SimpleEntity.class, "openjpa.Compatibility", "ConvertPositionalParametersToNamed=true"
//        ,"openjpa.Log","SQL=trace"    
        );
        _em = emf.createEntityManager();
        
        _em.getTransaction().begin();
        SimpleEntity se1 = new SimpleEntity();
        _name1 = "name1";
        _val1 = "val1";
        se1.setName(_name1);
        se1.setValue(_val1);
        _em.persist(se1);
        _id1 = se1.getId();
        _em.getTransaction().commit();
        
        _em.getTransaction().begin();
        SimpleEntity se2 = new SimpleEntity();
        _name2 = "name2";
        _val2 = "val2";
        se2.setName(_name2);
        se2.setValue(_val2);
        _em.persist(se2);
        _id2 = se2.getId();
        _em.getTransaction().commit();
        _em.clear();
    }

    @Override
    public void tearDown() throws Exception {
        if (_em.getTransaction().isActive()) {
            _em.getTransaction().rollback();
        }
        _em.close();
        // TODO Auto-generated method stub
        super.tearDown();
    }

    public void testNamedPositionalStartAtNonOne() {
        SimpleEntity se =
            _em.createNamedQuery("SelectWithPositionalParameterNonOneStart", SimpleEntity.class)
                .setParameter(900, _id1).setParameter(2, _name1).setParameter(54, _val1).getSingleResult();
        assertNotNull(se);
    }

    public void testJPQLPositionalStartAtNonOne() {
        int idPos = 7;
        int namePos = 908;
        int valPos = 578;
        SimpleEntity se =
            _em.createQuery(
                "Select s FROM simple s where s.id=?" + idPos + " and s.name=?" + namePos + " and s.value=?" + valPos,
                SimpleEntity.class).setParameter(idPos, _id1).setParameter(namePos, _name1).setParameter(valPos, _val1)
                .getSingleResult();
        assertNotNull(se);
    }

    public void testJPQLWithSubQueryPositionalStartAtNonOne() {
        int idPos = 7;
        int namePos = 908;
        int valPos = 578;
        SimpleEntity se =
            _em.createQuery(
                "Select s FROM simple s where s.id = ?" + idPos
                    + " and (SELECT se.value FROM simple se where se.name=?" + namePos + ")=?" + valPos,
                SimpleEntity.class).setParameter(idPos, _id1).setParameter(namePos, _name1).setParameter(valPos, _val1)
                .getSingleResult();
        assertNotNull(se);
    }

    public void testPreparedQueryPositionalStartAtNonOne() {
        int idPos = 54;
        int namePos = 23;
        int valPos = 42;
        String jpql =
            "Select s FROM simple s where s.id=?" + idPos + " and s.name=?" + namePos + " and s.value=?" + valPos;

        Query q =
            _em.createQuery(jpql).setParameter(idPos, _id1).setParameter(namePos, _name1).setParameter(valPos, _val1);
        SimpleEntity se = (SimpleEntity) q.getSingleResult();
        assertNotNull(se);
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q).getLanguage());

        Query q2 =
            _em.createQuery(jpql).setParameter(idPos, _id2).setParameter(namePos, _name2).setParameter(valPos, _val2);
        SimpleEntity se2 = (SimpleEntity) q2.getSingleResult();
        assertNotNull(se2);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());
    }

    public void testPreparedQueryWithSubQueryPositionalStartAtNonOne() {
        int idPos = 7;
        int namePos = 908;
        int valPos = 352;
        String jpql =
            "Select s FROM simple s where s.id = ?" + idPos + " and (SELECT se.value FROM simple se where se.name=?"
                + namePos + ")=?" + valPos;
        Query q =
            _em.createQuery(jpql, SimpleEntity.class).setParameter(idPos, _id1).setParameter(namePos, _name1)
                .setParameter(valPos, _val1);

        SimpleEntity se = (SimpleEntity) q.getSingleResult();
        assertNotNull(se);
        assertEquals(JPQLParser.LANG_JPQL, OpenJPAPersistence.cast(q).getLanguage());

        Query q2 =
            _em.createQuery(jpql, SimpleEntity.class).setParameter(idPos, _id2).setParameter(namePos, _name2)
                .setParameter(valPos, _val2);
        SimpleEntity se2 = (SimpleEntity) q2.getSingleResult();
        assertNotNull(se2);
        assertEquals(QueryLanguages.LANG_PREPARED_SQL, OpenJPAPersistence.cast(q2).getLanguage());

    }
}
