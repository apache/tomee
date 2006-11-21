/**
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

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class DerbyTestDatabase implements TestDatabase{

    protected Database database;
    protected InitialContext initialContext;

    private static String _createAccount = "CREATE TABLE account ( ssn VARCHAR(25), first_name VARCHAR(256), last_name VARCHAR(256), balance integer)";

    private static String _dropAccount = "DROP TABLE account";

    private static String _createEntity = "CREATE TABLE entity ( id integer GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), first_name VARCHAR(256), last_name VARCHAR(256) )";

    private static String _dropEntity = "DROP TABLE entity";

    static{
        System.setProperty("noBanner", "true");
    }


    public void createEntityTable() throws java.sql.SQLException {
        try{
            try{
                database.execute(DerbyTestDatabase._dropEntity);
            } catch (Exception e){
                // not concerned
            }
            database.execute(DerbyTestDatabase._createEntity);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create entity table: "+re.getMessage(), DerbyTestDatabase._createEntity);
            }
        }
    }
    public void dropEntityTable() throws java.sql.SQLException {
        try {
            database.execute(DerbyTestDatabase._dropEntity);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Unable to drop entity table: "+re.getMessage(), DerbyTestDatabase._dropEntity);
            }
        }
    }


    public void createAccountTable() throws java.sql.SQLException {
        try{
            try{
                database.execute(DerbyTestDatabase._dropAccount);
            } catch (Exception e){
                // not concerned
            }
            database.execute(DerbyTestDatabase._createAccount);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create account table: "+re.getMessage(), DerbyTestDatabase._createAccount);
            }
        }
    }

    public void dropAccountTable() throws java.sql.SQLException {
        try {
            database.execute(DerbyTestDatabase._dropAccount);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Cannot drop account table: "+re.getMessage(), DerbyTestDatabase._dropAccount);
            }
        }
    }

    public void start() throws IllegalStateException {
        try {
            Properties properties = TestManager.getServer().getContextEnvironment();
            initialContext = new InitialContext(properties);
        } catch (Exception e){
            throw (IllegalStateException) new IllegalStateException("Cannot create initial context: "+e.getClass().getName()+" "+e.getMessage()).initCause(e);
        }

    Object obj =null;
    DatabaseHome databaseHome =null;
        try {
            /* Create database */
            obj = initialContext.lookup("client/tools/DatabaseHome");
            databaseHome = (DatabaseHome)javax.rmi.PortableRemoteObject.narrow( obj, DatabaseHome.class);
        } catch (Exception e){
            throw new IllegalStateException("Cannot find 'client/tools/DatabaseHome': "+e.getClass().getName()+" "+e.getMessage());
        }
        try {
            database = databaseHome.create();
        } catch (Exception e){
            throw new IllegalStateException("Cannot start database: "+e.getClass().getName()+" "+e.getMessage());
        }
    }


    public void stop() throws IllegalStateException {
    }

    public void init(Properties props) throws IllegalStateException {
    }
}
