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
package org.apache.openjpa.persistence.dynamicschema;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;


/**
 * <b>TestDynamicSchemas</b> is used to create dynamic schemas for the various 
 * database dictionaries and validate them to ensure they are created 
 * correctly as specified in their dictionary. The following variables of each 
 * dictionary are used for validation:<p>
 *
 * <ol>
 *     <li>maxTableNameLength           
 *     <li>maxColumnNameLength          
 *     <li>reservedWordsSet
 * </ol>
 * 
 * <b>Note(s):</b> 
 * <ul>
 *     <li>To minimize the running time of these testcases there are no
 *     connections made to any of the databases
 *     <li>This is accomplished by passing the "export" SchemaAction to the
 *     MappingTool, and using the RETAIN_DATA option to prevent SQL commands
 *     from getting executed
 * </ul>
 * 
 * @author Tim McConnell
 * @since 2.0.0
 */
public class TestDynamicSchemas extends SingleEMFTestCase {

    public void setUp() {
    }


    public void testDerbyDynamicSchema() {
        OpenJPAEntityManagerFactorySPI derbyEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:derby:net://host:1527/databaseName",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( derbyEMF );
        closeEMF(derbyEMF);
    }


    public void testDB2DynamicSchema() {
        OpenJPAEntityManagerFactorySPI db2EMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:db2://localhost:5000/db2",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( db2EMF );
        closeEMF(db2EMF);
    }


    public void testOracleDynamicSchema() {
        OpenJPAEntityManagerFactorySPI oracleEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:oracle:thin:@host:1234:database_sid",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( oracleEMF );
        closeEMF(oracleEMF);
    }


    public void testAccessDynamicSchema() {
        OpenJPAEntityManagerFactorySPI accessEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:odbc:Driver=Microsoft Access Driver (*.mdb);DBQ=c:",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( accessEMF );
        closeEMF(accessEMF);
    }


    public void testSQLServerDynamicSchema() {
        OpenJPAEntityManagerFactorySPI sqlserverEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:microsoft:sqlserver:",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( sqlserverEMF );
        closeEMF(sqlserverEMF);
    }

    
    public void testMariaDBDynamicSchema() {
        OpenJPAEntityManagerFactorySPI mysqlEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:mariadb://host1:1,host2:2/database?p1=v1&p2=v2",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( mysqlEMF );
        closeEMF(mysqlEMF);
    }
    

    public void testMySQLDynamicSchema() {
        OpenJPAEntityManagerFactorySPI mysqlEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:mysql://host1:1,host2:2/database?p1=v1&p2=v2",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( mysqlEMF );
        closeEMF(mysqlEMF);
    }


    public void testPostgresDynamicSchema() {
        OpenJPAEntityManagerFactorySPI postgresEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:postgresql:database",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( postgresEMF );
        closeEMF(postgresEMF);
    }


    public void testInformixDynamicSchema() {
        OpenJPAEntityManagerFactorySPI informixEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:informix-sqli:",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( informixEMF );
        closeEMF(informixEMF);
    }


    public void testSybaseDynamicSchema() {
        OpenJPAEntityManagerFactorySPI sybaseEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:sybase:Tds:host:1234?ServiceName=db",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( sybaseEMF );
        closeEMF(sybaseEMF);
    }


    public void testFirebirdDynamicSchema() {
        OpenJPAEntityManagerFactorySPI firebirdEMF = 
            createEMF(EntityVeryLongNames.class, EntityReservedWords.class,
                "openjpa.ConnectionURL", 
                "jdbc:firebirdsql:host/1234:database",
                "openjpa.jdbc.SynchronizeMappings", "export", 
                "openjpa.jdbc.SchemaFactory", "dynamic", RETAIN_DATA);
        validateTableName( firebirdEMF );
        closeEMF(firebirdEMF);
    }


    /**
     * Derby tests run with a DerbyDictionary-specific property, so clear it out here.
     * Otherwise, all tests except testDerbyDynamicSchema will fail.
     */
    @Override
    protected OpenJPAEntityManagerFactorySPI createEMF(Object... props) {
        int propsLength = props.length;
        Object[] newProps = new Object[propsLength + 2];
        System.arraycopy(props, 0, newProps, 0, propsLength);
        newProps[propsLength] = "openjpa.jdbc.DBDictionary";
        newProps[propsLength + 1] = "";
        return super.createEMF(newProps);
    }


    private void validateTableName(OpenJPAEntityManagerFactorySPI emf) {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        ClassMapping mapping =  (ClassMapping)conf.
            getMetaDataRepositoryInstance().
            getMetaData(EntityVeryLongNames.class,getClass().
                getClassLoader(), true);
        Table table = mapping.getTable();
        assertTrue(table.getName().length() > 0);
        assertTrue(table.getName().length() <= dict.maxTableNameLength);
        validateColumnNames(table, dict);
        mapping =  (ClassMapping)conf.
            getMetaDataRepositoryInstance().
            getMetaData(EntityReservedWords.class,getClass().
                getClassLoader(), true);
        table = mapping.getTable();
        assertTrue(table.getName().length() > 0);
        assertTrue(table.getName().length() <= dict.maxTableNameLength);
        validateColumnNames(table, dict);
    }


    private void validateColumnNames(Table table, DBDictionary dict) {
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            assertTrue(column.getName().length() > 0);
            assertTrue(column.getName().length() <= dict.maxColumnNameLength);
            assertFalse(dict.getInvalidColumnWordSet().
                contains(column.getName().toUpperCase()));
        }
    }
}
