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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test;

import org.apache.openejb.test.beans.Database;
import org.apache.openejb.test.beans.DatabaseHome;

import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class PostgreSqlTestDatabase implements TestDatabase {

    protected Database database;
    protected InitialContext initialContext;


    private static String _createAccount = "CREATE TABLE account ( ssn CHAR(11), first_name CHAR(20), last_name CHAR(20), balance INT, Constraint \"account_pkey\" Primary Key (\"ssn\"))";
    private static String _dropAccount = "DROP TABLE account";
    //private static String _createEntity  = "CREATE TABLE entity ( id INT NOT NULL, first_name CHAR(20), last_name CHAR(20), Constraint \"entity_pkey\" Primary Key (\"id\") )";
    private static String _createEntity = "CREATE TABLE entity ( id INT DEFAULT nextval('entity_id_seq') , first_name CHAR(20), last_name CHAR(20), Constraint \"entity_pkey\" Primary Key (\"id\") )";
    private static String _dropEntity = "DROP TABLE entity";

    public void createEntityTable() throws java.sql.SQLException {
        try {
            database.execute("DROP SEQUENCE entity_id_seq");
        } catch (Exception e) {
            // not concerned
        }
        try {
            database.execute(_dropEntity);
        } catch (Exception e) {
            // not concerned
        }
        try {
            database.execute("CREATE SEQUENCE entity_id_seq");
        } catch (Exception e) {
            // not concerned
        }
        try {
            database.execute(_createEntity);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create entity table: " + re.getMessage(), _createEntity);
            }
        }
    }

    public void dropEntityTable() throws java.sql.SQLException {
        try {
            database.execute("DROP SEQUENCE entity_id_seq");
        } catch (Exception e) {
            // not concerned
        }
        try {
            database.execute(_dropEntity);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Unable to drop entity table: " + re.getMessage(), _dropEntity);
            }
        }
    }


    public void createAccountTable() throws java.sql.SQLException {
        try {
            database.execute("DROP SEQUENCE account_id_seq");
        } catch (Exception e) {
            // not concerned
        }
        try {
            database.execute("DROP TABLE account");
        } catch (Exception e) {
            // not concerned
        }
        try {
            database.execute("CREATE SEQUENCE account_id_seq");
        } catch (Exception e) {
            // not concerned
        }
        try {
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
            try {
                database.execute("DROP SEQUENCE account_id_seq");
            } catch (Exception e) {
                // not concerned
            }
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
            Properties properties = TestManager.getServer().getContextEnvironment();
            initialContext = new InitialContext(properties);

            /* Create database */
            Object obj = initialContext.lookup("client/tools/DatabaseHome");
            DatabaseHome databaseHome = (DatabaseHome) javax.rmi.PortableRemoteObject.narrow(obj, DatabaseHome.class);
            database = databaseHome.create();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot start database: " + e.getClass().getName() + " " + e.getMessage());
        }
    }

    public void stop() throws IllegalStateException {
    }

    public void init(Properties props) throws IllegalStateException {
    }

    public static void main(String[] args) {
        System.out.println("Checking if driver is registered with DriverManager.");
        try {
            ClassLoader cl = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
            Class.forName("org.postgresql.Driver", true, cl);
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find the driver!");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Registered the driver, so let's make a connection.");

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/openejbtest", "openejbuser", "javaone");
        } catch (SQLException e) {
            System.out.println("Couldn't connect.");
            e.printStackTrace();
            System.exit(1);
        }

        if (conn == null) {
            System.out.println("No connection!");
        }

        Statement stmt = null;

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            System.out.println("Couldn't create a statement.");
            e.printStackTrace();
            System.exit(1);
        }

        ResultSet rs = null;

        try {
            stmt.execute("DROP TABLE entity");
        } catch (SQLException e) {
        }

        System.out.println("Creating entity table.");
        try {
            stmt.execute(_createEntity);
        } catch (SQLException e) {
            System.out.println("Couldn't create the entity table");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Inserting record.");
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into entity (id, first_name, last_name) values (?,?,?)");
            pstmt.setInt(1, 101);
            pstmt.setString(2, "Bunson");
            pstmt.setString(3, "Honeydew");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Couldn't create the entity table");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Selecting the record.");
        try {
            PreparedStatement pstmt = conn.prepareStatement("select id from entity where first_name = ? AND last_name = ?");
            pstmt.setString(1, "Bunson");
            pstmt.setString(2, "Honeydew");
            ResultSet set = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Couldn't select the entry");
            e.printStackTrace();
            System.exit(1);
        }


        System.out.println("Dropping the entity table.");
        try {
            stmt.execute(_dropEntity);
        } catch (SQLException e) {
            System.out.println("Couldn't drop the entity table");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("Couldn't create the sequense");
            e.printStackTrace();
            System.exit(1);
        }

    }
}
