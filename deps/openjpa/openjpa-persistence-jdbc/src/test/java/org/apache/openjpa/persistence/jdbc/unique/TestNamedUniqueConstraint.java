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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.jdbc.SQLSniffer;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestNamedUniqueConstraint extends SQLListenerTestCase {

    DBDictionary dict;
    
    @Override
    public void setUp(Object... props) {
        super.setUp(DROP_TABLES, NamedUniqueA.class, NamedUniqueB.class);
    }
    
    public void testMapping() {
        
        // If the database does not support unique constraints, exit
        if (!supportsUniqueConstraints())
            return;
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.getTransaction().commit();
        em.close();
        // The above should trigger schema definition
        
        List<String> sqls = super.sql;
        
        assertSQLFragnments(sqls, "CREATE TABLE N_UNIQUE_A",
            getUniqueConstraint("uca_f1_f2 UNIQUE .*\\(f1, f2\\)"), 
            getUniqueConstraint("uca_f3_f4 UNIQUE .*\\(f3, f4\\).*"));
        assertSQLFragnments(sqls, "CREATE TABLE N_UNIQUE_B",
            getUniqueConstraint("ucb_f1_f2 UNIQUE .*\\(f1, f2\\).*"));
        assertSQLFragnments(sqls, "CREATE TABLE N_UNIQUE_SECONDARY",
            getUniqueConstraint("uca_sf1 UNIQUE .*\\(sf1\\)"));
        assertSQLFragnments(sqls, "CREATE TABLE N_UNIQUE_GENERATOR",
            getUniqueConstraint("ucb_gen1_gen2 UNIQUE .*\\(GEN1, GEN2\\)"));
        assertSQLFragnments(sqls, "CREATE TABLE N_UNIQUE_JOINTABLE",
            getUniqueConstraint("uca_fka_fkb UNIQUE .*\\(FK_A, FK_B\\)"));
        assertSQLFragnments(sqls, "CREATE TABLE N_U_COLL_TBL",
            getUniqueConstraint("ucb_f3 UNIQUE .*\\(f3\\).*"));
    }
        
    private boolean supportsUniqueConstraints() {
        OpenJPAEntityManagerFactorySPI emfs = (OpenJPAEntityManagerFactorySPI)emf;
        JDBCConfiguration jdbccfg = (JDBCConfiguration)emfs.getConfiguration();
        dict = jdbccfg.getDBDictionaryInstance();
        return dict.supportsUniqueConstraints;
    }

    private String getUniqueConstraint(String unique) {
        if (dict instanceof MySQLDictionary || dict instanceof MariaDBDictionary) {
            //CREATE TABLE N_UNIQUE_A (aid INTEGER NOT NULL, f1 INTEGER NOT NULL, f2 INTEGER NOT NULL, 
            //f3 INTEGER NOT NULL, f4 INTEGER NOT NULL, f5 INTEGER, f6 INTEGER, PRIMARY KEY (aid), 
            //UNIQUE U_N_UNQU__F1 (f1), 
            //UNIQUE uca_f1_f2 (f1, f2), 
            //UNIQUE uca_f3_f4 (f3, f4)) TYPE = innodb
            return getUniqueConstraint(dict, unique);
        }
        return unique;
    }
    
    public static String getUniqueConstraint(DBDictionary dict, String unique) {
        List<String> tokens = new ArrayList<String>();
        List<String> tokens1 = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(unique);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        tokens1.add(tokens.get(1));
        tokens1.add(tokens.get(0));
        for (int i = 2; i < tokens.size(); i++) {
            tokens1.add(tokens.get(i));
        }
        StringBuffer buf = new StringBuffer();
        for (String token : tokens1) {
            buf.append(token).append(" ");
        }
        return buf.toString().trim();
    }
    
    void assertSQLFragnments(List<String> list, String... keys) {
        if (SQLSniffer.matches(list, keys))
            return;
        fail("None of the following " + sql.size() + " SQL \r\n" + 
                toString(sql) + "\r\n contains all keys \r\n"
                + toString(Arrays.asList(keys)));
    }
}
