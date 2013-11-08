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

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.jdbc.SQLSniffer;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestNamedUniqueConstraintWithXMLDescriptor extends SQLListenerTestCase {
    DBDictionary dict;

    @Override
    public void setUp(Object... props) {
        super.setUp(DROP_TABLES, NamedUniqueA.class, NamedUniqueB.class);
    }
    
    protected String getPersistenceUnitName() {
        return "NamedUniqueConstraintTest";
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
        
        assertSQLFragnments(sqls, "CREATE TABLE NX_UNIQUE_A",
            getUniqueConstraint("ucxa_f1_f2 UNIQUE .*\\(f1x, f2x\\)"), 
            getUniqueConstraint("ucxa_f3_f4 UNIQUE .*\\(f3x, f4x\\).*"));
        assertSQLFragnments(sqls, "CREATE TABLE NX_UNIQUE_B",
            getUniqueConstraint("ucxb_f1_f2 UNIQUE .*\\(f1x, f2x\\).*"));
        assertSQLFragnments(sqls, "CREATE TABLE NX_UNIQUE_SECONDARY",
            getUniqueConstraint("ucxa_sf1 UNIQUE .*\\(sf1x\\)"));
        assertSQLFragnments(sqls, "CREATE TABLE NX_UNIQUE_GENERATOR",
            getUniqueConstraint("ucxb_gen1_gen2 UNIQUE .*\\(GEN1_XML, GEN2_XML\\)"));
        assertSQLFragnments(sqls, "CREATE TABLE NX_UNIQUE_JOINTABLE",
            getUniqueConstraint("ucxa_fka_fkb UNIQUE .*\\(FK_A_XML, FK_B_XML\\)"));
        assertSQLFragnments(sqls, "CREATE TABLE NX_U_COLL_TBL",
            getUniqueConstraint("ucxb_f3 UNIQUE .*\\(f3x\\).*"));
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
            return TestNamedUniqueConstraint.getUniqueConstraint(dict, unique);
        }
        return unique;
    }
    
    void assertSQLFragnments(List<String> list, String... keys) {
        if (SQLSniffer.matches(list, keys))
            return;
        fail("None of the following " + sql.size() + " SQL \r\n" + 
                toString(sql) + "\r\n contains all keys \r\n"
                + toString(Arrays.asList(keys)));
    }
}
