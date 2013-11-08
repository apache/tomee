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
package org.apache.openjpa.persistence.embed.attrOverrides;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;
import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.jdbc.SQLSniffer;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestAssocOverridesXML  extends AbstractPersistenceTestCase{ 

    protected List<String> _sql = new ArrayList<String>();

    /**
     * Test association overrides defined within an embedded of an element
     * collection
     */
    public void testElementCollectionAssocOverrides() {

        _sql.clear();
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("openjpa.jdbc.JDBCListeners", 
            new JDBCListener[] { new SQLListener() });
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("AssocOverPU",
            "org/apache/openjpa/persistence/embed/" +
            "embed-persistence.xml", props);

        EntityManager em = emf1.createEntityManager();

        XMLAssocOverEntityA ea = new XMLAssocOverEntityA();
        XMLAssocOverEntityB eb = new XMLAssocOverEntityB();
        XMLAssocOverEntityB meb = new XMLAssocOverEntityB();
        XMLAssocOverEmbed emb = new XMLAssocOverEmbed();
        eb.setName("XMLAssocOverEntityB");
        meb.setName("XMLAssocOverEntityBM21");
        List<XMLAssocOverEntityA> eaList1 = new ArrayList<XMLAssocOverEntityA>();
        eaList1.add(ea);
        List<XMLAssocOverEntityA> eaList2 = new ArrayList<XMLAssocOverEntityA>();        
        eaList2.add(ea);
        eb.setEaList(eaList1);
        meb.setEaList(eaList2);
        emb.setName("XMLAssocOverEmbed");
        emb.setEb(eb);
        emb.setMeb(meb);
        List<XMLAssocOverEmbed> embList = new ArrayList<XMLAssocOverEmbed>();
        embList.add(emb);
        ea.setEmbA(embList);
        
        em.getTransaction().begin();
        em.persist(ea);
        em.getTransaction().commit();
        em.close();
        try {
            assertSQLFragnments(_sql, "CREATE TABLE XML_EMBALIST .*" +
                " .*emba_entb.*emba_mentb");
        
            assertSQLFragnments(_sql, "CREATE TABLE XML_EMBAMAP_3 .*" +
                " .*key_emba_entb.*key_emba_mentb" + 
                " .*value_emba_entb.*value_emba_mentb");
        } 
        finally {
            try {
                if (emf1 != null)
                    cleanupEMF(emf1);
                _sql.clear();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    void assertSQLFragnments(List<String> list, String... keys) {
        if (SQLSniffer.matches(list, keys))
            return;
        fail("None of the following " + list.size() + " SQL \r\n" + 
                toString(list) + "\r\n contains all keys \r\n"
                + toString(Arrays.asList(keys)));
    }

    private static String toString(List<String> list) {
        StringBuffer buf = new StringBuffer();
        for (String s : list)
            buf.append(s).append("\r\n");
        return buf.toString();
    }
    
    /**
     * Closes a specific entity manager factory and cleans up 
     * associated tables.
     */
    private void cleanupEMF(OpenJPAEntityManagerFactorySPI emf1) 
      throws Exception {

        if (emf1 == null)
            return;

        try {
            clear(emf1);
        } catch (Exception e) {
            // if a test failed, swallow any exceptions that happen
            // during tear-down, as these just mask the original problem.
            if (testResult.wasSuccessful())
                throw e;
        } finally {
            closeEMF(emf1);
        }
    }    

    public class SQLListener
        extends AbstractJDBCListener {

        @Override
        public void beforeExecuteStatement(JDBCEvent event) {
            if (event.getSQL() != null && _sql != null) {
                _sql.add(event.getSQL());
        }
    }
}
}
