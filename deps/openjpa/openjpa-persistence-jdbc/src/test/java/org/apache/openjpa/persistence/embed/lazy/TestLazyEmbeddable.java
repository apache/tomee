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
package org.apache.openjpa.persistence.embed.lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;
import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestLazyEmbeddable extends AbstractPersistenceTestCase {  

    protected List<String> _sql = new ArrayList<String>();

    /*
     * Verifies an entity with annotated (@Persistent) lazy embeddable and xml-tagged
     * lazy embeddable (openjpa:persistent) with a mix of eager and lazy fields are lazily 
     * loaded (or not) as expected.
     */
    public void testLazyEmbeddableFields() throws Exception {
        _sql.clear();
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("openjpa.jdbc.JDBCListeners", 
            new JDBCListener[] { new SQLListener() });
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("LazyEmbedPU",
            "org/apache/openjpa/persistence/embed/lazy/" +
            "embed-lazy-persistence.xml", props);
        
        try {
            EntityManager em = emf1.createEntityManager();
            
            Recliner rec = new Recliner();
            ReclinerId recId = new ReclinerId();
            recId.setColor("Camouflage");
            recId.setId(new Random().nextInt());
            rec.setId(recId);
            rec.setStyle(Style.RETRO);
            
            Guy guy = new Guy();
            guy.setName("Tom");
            guy.setHeight(76);
            guy.setWeight(275);
            rec.setGuy(guy);
            
            BeverageHolder bh = new BeverageHolder();
            bh.setDepth(2);
            bh.setDiameter(3);
            rec.setHolder(bh);

            em.getTransaction().begin();
            em.persist(rec);
            em.getTransaction().commit();
            
            em.clear();
            _sql.clear();
            
            Recliner r2 = em.find(Recliner.class, recId);
            assertNotNull("Find returned null object", r2);
            assertTrue(selectContains("REC_TABLE", _sql, "REC_STYLE", "RECID_ID", "RECID_COLOR"));
            assertFalse(selectContains("REC_TABLE", _sql, "GUY_HEIGHT", "GUY_WEIGHT", "GUY_NAME", 
                "BH_DIAMETER", "BH_DEPTH"));
            em.detach(r2);
            // Lazy embeds should be null after detach.
            assertNull("Embedded field guy is null before getter is called", r2.getGuy());
            assertNull("Embedded field holder is null before getter is called", r2.getHolder());

            // verify lazy embeds will load on access post-detach and merge
            r2 = em.merge(r2);
            verifyLazyLoading(r2);

            em.clear();
            _sql.clear();
            // verify lazy embeds will load on access after find
            r2 = em.find(Recliner.class, recId);
            assertNotNull("Find returned null object", r2);
            assertTrue(selectContains("REC_TABLE", _sql, "REC_STYLE", "RECID_ID", "RECID_COLOR"));
            assertFalse(selectContains("REC_TABLE", _sql, "GUY_HEIGHT", "GUY_WEIGHT", "GUY_NAME", 
                "BH_DIAMETER", "BH_DEPTH"));
            verifyLazyLoading(r2);
        } finally {
            cleanupEMF(emf1);
        }
    }
    


    private void verifyLazyLoading(Recliner r2) {        
        _sql.clear();
        Guy g = r2.getGuy();
        assertNotNull("Guy is not null", g);
        assertTrue(selectContains("REC_TABLE", _sql, "GUY_NAME"));
        assertFalse(selectContains("REC_TABLE", _sql, "GUY_HEIGHT", "GUY_WEIGHT"));
        _sql.clear();
        g.getHeight();
        assertTrue(selectContains("REC_TABLE", _sql, "GUY_HEIGHT"));
        assertFalse(selectContains("REC_TABLE", _sql, "GUY_NAME", "GUY_WEIGHT", "BH_DIAMETER", 
            "BH_DEPTH"));

        _sql.clear();
        BeverageHolder holder = r2.getHolder();
        assertNotNull("Holder is not null", holder);
        assertTrue(selectContains("REC_TABLE", _sql, "BH_DEPTH"));
        assertFalse(selectContains("REC_TABLE", _sql, "BH_DIAMETER"));
        _sql.clear();
        holder.getDiameter();
        assertTrue(selectContains("REC_TABLE", _sql, "BH_DIAMETER"));
        assertFalse(selectContains("REC_TABLE", _sql, "BH_DEPTH"));
    }



    private boolean selectContains(String table, List<String> sql, String...cols) {
        boolean foundSelect = false;
        for (String s: sql) {
            String stmt = s.toUpperCase();
            if (!stmt.startsWith("SELECT") && !stmt.contains(table)) {
                continue;
            }
            foundSelect = true;
            for (String col : cols) {
                String ucol = col.toUpperCase();
                if (!stmt.contains(ucol)) {
                   return false;
                }
            }
        }
        return foundSelect;
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
