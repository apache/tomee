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
package org.apache.openjpa.persistence.query.results;

import java.util.ArrayList;
import java.util.List;
import org.apache.openjpa.lib.rop.ListResultList;
import org.apache.openjpa.lib.rop.ResultList;

import org.apache.openjpa.kernel.DistinctResultList;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.PersistenceExceptions;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

/**
 * Test that the DistinctResultList serializes correctly and without error.
 *
 * @author Dianne Richards
 * @since 2.1.0
 */
public class TestListResultSerialization extends SQLListenerTestCase {
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull(emf);
    }
    
    public void testRoundtrip() {
        List<String> list = new ArrayList<String>();
        list.add("xxx");
        list.add("yyy");
        ResultList resultList = new ListResultList(list);
        EntityManagerImpl em = (EntityManagerImpl)emf.createEntityManager();
        em.close();
        RuntimeExceptionTranslator trans = PersistenceExceptions.getRollbackTranslator(em);
        DistinctResultList distinctResultList = new DistinctResultList(resultList, trans);
        try {
            roundtrip(distinctResultList);
        } catch (Exception e) {
            e.printStackTrace();
            fail("unexpected exception - see stack trace in output");
        }
    }
}
