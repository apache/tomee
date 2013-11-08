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
package org.apache.openjpa.jdbc.sql;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class TestDictionaryFactory extends TestCase {

    private String[][] testMatrix =  {
        // Generic url
        {"jdbc:mysql:",                                     
         "jdbc:mysql:" },
        {"jdbc:cloudscape:database",
         "jdbc:cloudscape:" },           
        {"jdbc:as400://systemname",
         "jdbc:as400:" },
        {"jdbc:Cache://host:1234/db",
         "jdbc:Cache:" },
        {"jdbc:FrontBase://host.domain.com/mydb",
         "jdbc:FrontBase:" },
        {"jdbc:sqlserver://server\\instance:1234;p1=v1",
         "jdbc:sqlserver:"},
        {"jdbc:interbase://win32:3/C:/fire/test.gdb",
         "jdbc:interbase:"},
        {"jdbc:firebirdsql://unix/fire/test.gdb",
         "jdbc:firebirdsql:"},
        {"jdbc:db2://localhost:5000/db2",
         "jdbc:db2:" },
         
        // Oracle - Thin and OCI
        {"jdbc:oracle:thin:@host:1234:database_sid",
         "jdbc:oracle:thin:"},
        {"jdbc:oracle:oci://host:port/service", 
         "jdbc:oracle:oci:"},
        {"jdbc:oracle:thin:host:1234:sid",      
         "jdbc:oracle:thin:"},
        {"jdbc:oracle:oci:TNSName",       
         "jdbc:oracle:oci:"},
         
        // MS SQLServer 2000 Driver
        {"jdbc:microsoft:sqlserver://host:123;dbname=name;usr=u;pwd=p",
         "jdbc:microsoft:sqlserver:"},
        // MS SQLServer (Sprinta)
        {"jdbc:inetdae7:host:1234?database=DB", 
         "jdbc:inetdae7:host:"},
        // MS Access
        {"jdbc:odbc:Driver=Microsoft Access Driver (*.mdb);DBQ=c:\\db_file;",
         "jdbc:odbc:Driver=Microsoft Access Driver (*.mdb);DBQ=c:"},
         
        // jTDS JDBC Driver
        {"jdbc:jtds:microsoft:host:1234/database",          
         "jdbc:jtds:microsoft:"},
        {"jdbc:jtds:sqlserver://server:1234/database;p=v1",
         "jdbc:jtds:sqlserver:"},
         
        // mySQL
        {"jdbc:mysql://host,failoverhost:1234/database", 
         "jdbc:mysql:"},
        {"jdbc:mysql://host1:1,host2:2/database?p1=v1&p2=v2",
         "jdbc:mysql:"},
         
        // PostgreSQL
        {"jdbc:postgresql:database",
         "jdbc:postgresql:"},

        // JBOSS
        {"jdbc:AvenirDriver://127.0.0.1:1433/master;uid=sa;pwd=sa",
         "jdbc:AvenirDriver:"},
        {"jdbc:merant:sqlserver://suresh:1433",
         "jdbc:merant:sqlserver:"},
        {"jdbc:JSQLConnect://localhost/database=Master&user=sa&password=sa",
         "jdbc:JSQLConnect:"},
        {"jdbc:weblogic:mssqlserver4:Master@suresh:1433?user=sa&password=sa",
         "jdbc:weblogic:mssqlserver4:"},

        // SyBase
        {"jdbc:sybase:Tds:host:1234?ServiceName=database_name",  
         "jdbc:sybase:Tds:"},
          
        // Hypersonic SQL (in-process)
        {"jdbc:hsqldb:file:/opt/db/testdb",
         "jdbc:hsqldb:file:"},
        // Hypersonic SQL (in-memory)
        {"jdbc:hsqldb:mem:aname",
         "jdbc:hsqldb:mem:"},
        //Hypersonic SQL (server)
        {"jdbc:hsqldb:database",
         "jdbc:hsqldb:"},
        {"jdbc:hsqldb:hsql://host:1234",
         "jdbc:hsqldb:hsql:"},

        // Informix
        {"jdbc:informix-sqli://host:1234/database:informixserver=dbserver.com",
         "jdbc:informix-sqli:"},

        // Derby
        {"jdbc:derby:net://host:1527/<databaseName",
         "jdbc:derby:net:"},

        // Cloudscape
        {"jdbc:cloudscape:MyDataBase",
         "jdbc:cloudscape:"},

        // PointBase
         {"jdbc:pointbase:embedded:PBPUBLIC",
          "jdbc:pointbase:embedded:"},
         {"jdbc:pointbase:server://host:1234/database",
          "jdbc:pointbase:server:"},

        // Interbase
        {"jdbc:interbase:jndi:LDAP_hostname:1234/db",
         "jdbc:interbase:jndi:"},
        
        // Borland JDataStore
        {"jdbc:borland:dsremote://hostName/path/storeName.jds",
         "jdbc:borland:dsremote:"},
        {"jdbc:borland:dslocal:storeName.jds",
         "jdbc:borland:dslocal:"},
        
        // EasySoft
        {"jdbc:easysoft://server/datasource:logonuser=user:logonpassword=pwd",
         "jdbc:easysoft:"},
         
        // PointBase
        {"jdbc:empress:DATABASE=db",
         "jdbc:empress:"},
        {"jdbc:empress://SERVER=localhost;PORT=6322",
         "jdbc:empress:"},
        {"jdbc:pointbase:embedded:sample",
         "jdbc:pointbase:embedded:"},

        // Interbase (Java driver) / FireBird
        {"jdbc:firebirdsql:host/1234:database",
         "jdbc:firebirdsql:"},
        {"jdbc:firebirdsql:localhost/3050:/firebird/test.gdb",
         "jdbc:firebirdsql:"},
        
        // H2 Database (embedded)
        {"jdbc:h2:test",
         "jdbc:h2:"},
        // H2 Database (Client Server)
        {"jdbc:h2:tcp://localhost:9092/test",
         "jdbc:h2:tcp:"},
        // H2 Database (In Memory)
        {"jdbc:h2:mem:name;key=value",
         "jdbc:h2:mem:"},
        {"jdbc:h2:file:fileName;key=value",
         "jdbc:h2:file:"},
         
        // Error case
         {"file://c:/",
          null},
    };
    
    private static Method getProtocol;

    public void setUp() {
        try {
            getProtocol = DBDictionaryFactory.class.getDeclaredMethod(
                "getProtocol", String.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * <p>
     * Tests calculateDBDictionary for the supported database and validate the
     * proper dictionary is selected based on the jdbc url.
     * </p>
     * 
     * @author Albert Lee
     */
    public void testUrl2Dictionary() {
        for (int i = 0; i < testMatrix.length; ++i) {
            try {
                String dbType = (String) getProtocol.invoke(null,
                    testMatrix[i][0]);
                assertEquals(dbType, testMatrix[i][1]);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }
}
