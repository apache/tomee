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

package org.apache.openjpa.persistence.jdbc;

import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestIsolationFecthHint extends SQLListenerTestCase{
	
	public void setUp(){
		setUp(AllFieldTypes.class, CLEAR_TABLES);
		
	}	

    public void testFetchPlanIsolationURHint(){
    	OpenJPAEntityManagerSPI em = emf.createEntityManager();     
        try {
        	DBDictionary dict = ((JDBCConfiguration) em.getConfiguration())
                    .getDBDictionaryInstance();
        	 if (dict instanceof DB2Dictionary) {
        		 AllFieldTypes allFieldTypes = new AllFieldTypes();
            	allFieldTypes.setStringField("testString");
            	allFieldTypes.setIntField(2012);
            	
            	em.getTransaction().begin();
            	em.persist(allFieldTypes);
            	Query query = em.createQuery("select e from AllFieldTypes e where e.stringField = ?1");
            	query.setParameter(1, "testString");
            	query.setHint("openjpa.FetchPlan.Isolation", "READ_UNCOMMITTED");
            	assertEquals(1, query.getResultList().size());
            	assertContainsSQL("FOR READ ONLY WITH UR");
            	em.getTransaction().rollback();
    		}
        } finally {
        	em.close();
        }
    }
}
