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
package org.apache.openjpa.persistence.jdbc.unique;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.SQLSniffer;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests unique constraints specified via annotations for primary/secondary
 * table, sequence generator, join tables have been defined on database by
 * examining DDL statements.
 * 
 * @see resources/org/apache/openjpa/persistence/jdbc/unique/orm.xml defines
 * the ORM mapping.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestUniqueConstraint extends SQLListenerTestCase {
    @Override
    public void setUp(Object... props) {
        super.setUp(DROP_TABLES, UniqueA.class, UniqueB.class);
    }
    
	public void testMapping() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.getTransaction().commit();
		em.close();
		// The above should trigger schema definition
		
		List<String> sqls = super.sql;
		
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_A",
				"UNIQUE .*\\(f1, f2\\)", 
				"UNIQUE .*\\(f3, f4\\).*");
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_B",
				"UNIQUE .*\\(f1, f2\\).*");
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_SECONDARY",
				"UNIQUE .*\\(sf1\\)");
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_GENERATOR",
				"UNIQUE .*\\(GEN1, GEN2\\)");
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_JOINTABLE",
				"UNIQUE .*\\(FK_A, FK_B\\)");
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_A",
			    "UNIQUE .*\\(f1\\)");
		assertSQLFragnments(sqls, "CREATE TABLE UNIQUE_B",
			    "UNIQUE .*\\(f1\\)");
	}
		
	void assertSQLFragnments(List<String> list, String... keys) {
		if (SQLSniffer.matches(list, keys))
			return;
		fail("None of the following " + sql.size() + " SQL \r\n" + 
				toString(sql) + "\r\n contains all keys \r\n"
				+ toString(Arrays.asList(keys)));
	}
}
