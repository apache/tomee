/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test;

import org.apache.openejb.test.beans.Database;
import org.apache.openejb.test.beans.DatabaseHome;

import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.Properties;

public class HsqldbTestDatabase implements TestDatabase {

    protected Database database;
    protected InitialContext initialContext;

    private static String _createAccount = "CREATE TABLE account ( ssn VARCHAR(255), first_name VARCHAR(255), last_name VARCHAR(255), balance integer)";

    private static String _dropAccount = "DROP TABLE account";

    private static String _createEntity = "CREATE TABLE entity ( id IDENTITY, first_name VARCHAR(255), last_name VARCHAR(255) )";

    private static String _dropEntity = "DROP TABLE entity";

    // CmrMapping
    private static final String CREATE_ONE_OWNING = "CREATE TABLE oneowning (col_id INTEGER, col_field1 INTEGER)";
    private static final String DROP_ONE_OWNING = "DROP TABLE oneowning";
    private static final String CREATE_ONE_INVERSE = "CREATE TABLE oneinverse (col_id INTEGER)";
    private static final String DROP_ONE_INVERSE = "DROP TABLE oneinverse";
    private static final String CREATE_MANY_OWNING = "CREATE TABLE manyowning (col_id INTEGER, col_field1 INTEGER)";
    private static final String DROP_MANY_OWNING = "DROP TABLE manyowning";

    // Automatically created tables - these must be dropped before the tests run or you will get a duplicate key exception
    private static final String[] AUTO_CREATED_TABLES = new String[] {
            "BasicCmpBean",
            "BasicCmpBeanX",
            "ComplexCmpBean",
            "ComplexCmpBeanX",
            "UnknownCmpBean",
            "UnknownCmpBeanX",
            "BasicCmp2Bean",
            "ComplexCmp2Bean",
            "UnknownCmp2Bean",
            "AOBasicCmpBean",
            "AllowedOperationsCmp2Bean",
            "EncCmpBean",
            "EncCmp2Bean",
            "ContextLookupCmpBean",
            "Cmp_RMI_IIOP_Bean",
            "RmiIiopCmp2Bean",
            "Person",
            "License",
            "ComplexPerson",
            "ComplexLicense",
            "Artist",
            "Song",
            "ComplexArtist",
            "Complexsong",
            "Game",
            "Platform",
            "ComplexGame",
            "ComplexPlatform",
            "Query",
            "QueryData",
    };

    static {
        System.setProperty("noBanner", "true");
    }


    public void createEntityTable() throws java.sql.SQLException {
        createTable(_createEntity, _dropEntity);
        createTable(CREATE_ONE_OWNING, DROP_ONE_OWNING);
        createTable(CREATE_ONE_INVERSE, DROP_ONE_INVERSE);
        createTable(CREATE_MANY_OWNING, DROP_MANY_OWNING);
        clearTables(AUTO_CREATED_TABLES);
    }

    public void dropEntityTable() throws java.sql.SQLException {
        dropTable(_dropEntity);
        dropTable(DROP_ONE_OWNING);
        dropTable(DROP_ONE_INVERSE);
        dropTable(DROP_MANY_OWNING);
        clearTables(AUTO_CREATED_TABLES);
    }

    private void createTable(String create, String drop) throws java.sql.SQLException {
        try {
            try {
                database.execute(drop);
            } catch (Exception e) {
                // not concerned
            }
            database.execute(create);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create table: " + re.getMessage(), create);
            }
        }
    }

    private void clearTables(String... autoCreatedTables) {
        for (String tableName : autoCreatedTables) {
            try {
                database.execute("DELETE FROM " + tableName);
            } catch (Exception e) {
                // not concerned
            }
        }
    }

    private void dropTable(String drop) throws java.sql.SQLException {
        try {
            database.execute(drop);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Unable to drop table: " + re.getMessage(), drop);
            }
        }
    }

    public void createAccountTable() throws java.sql.SQLException {
        try {
            try {
                database.execute(_dropAccount);
            } catch (Exception e) {
                // not concerned
            }
            database.execute(_createAccount);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create account table: " + re.getMessage(), _createAccount);
            }
        }
    }

    public void dropAccountTable() throws java.sql.SQLException {
        try {
            database.execute(_dropAccount);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot drop account table: " + re.getMessage(), _dropAccount);
            }
        }
    }

    public void start() throws IllegalStateException {
        try {
            TestServer server = TestManager.getServer();
            Properties properties = server.getContextEnvironment();
            initialContext = new InitialContext(properties);
        } catch (Exception e) {
            throw (IllegalStateException) new IllegalStateException("Cannot create initial context: " + e.getClass().getName() + " " + e.getMessage()).initCause(e);
        }

        final String databaseHomeJndiName = "client/tools/DatabaseHome";
        
        Object obj = null;
        DatabaseHome databaseHome = null;
        try {
            /* Create database */
            obj = initialContext.lookup(databaseHomeJndiName);
            databaseHome = (DatabaseHome) javax.rmi.PortableRemoteObject.narrow(obj, DatabaseHome.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot find " + databaseHomeJndiName + ": " + e.getClass().getName() + " " + e.getMessage());
        }
        try {
            database = databaseHome.create();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot start database: " + e.getClass().getName() + " " + e.getMessage());
        }
    }


    public void stop() throws IllegalStateException {
    }

    public void init(Properties props) throws IllegalStateException {
    }
}
