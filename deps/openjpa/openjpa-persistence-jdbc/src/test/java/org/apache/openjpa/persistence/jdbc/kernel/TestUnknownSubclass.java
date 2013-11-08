/*
 * TestUnknownSubclass.java
 *
 * Created on September 29, 2006, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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
package org.apache.openjpa.persistence.jdbc.kernel;


import java.sql.*;
import javax.sql.*;
import org.apache.openjpa.jdbc.conf.*;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;


import org.apache.openjpa.persistence.jdbc.common.apps.*;
import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAPersistence;


public class TestUnknownSubclass  extends JDBCTestCase {

    private String parentName =
        "openjpa.jdbc.kernel.UnknownSubclassParent";
    private String childName =
        "openjpa.jdbc.kernel.UnknownSubclassChild";

    /** Creates a new instance of TestUnknownSubclass */
    public TestUnknownSubclass() {
    }
 
    public TestUnknownSubclass(String name) {
        super(name);
    }
    public void setUp()
        throws Exception {
        // pcl: in the new (registration-less) system, this should not
        // matter.
        //assertNotRegistered (parentName);
        //assertNotRegistered (childName);

        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        
        
        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerSPI) kem)
            .getConfiguration();
        boolean flat = !isInheritanceStrategyVertical();

        DataSource ds = (DataSource) conf.getConnectionFactory();
        Connection c = ds.getConnection(conf.getConnectionUserName(),
            conf.getConnectionPassword());
        if (c.getAutoCommit())
            c.setAutoCommit(false);

        c.createStatement()
            .executeUpdate("DELETE FROM UNKNOWNSUBCLASSPARENT");
        if (!flat)
            c.createStatement()
                .executeUpdate("DELETE FROM UNKNOWNSUBCLASSCHILD");

        String insert = "INSERT INTO UNKNOWNSUBCLASSPARENT"
            + "(TYP, ID, VERSN) VALUES (";

        c.createStatement().executeUpdate(insert
            + "'" + parentName + "', 1, 1)");
        c.createStatement().executeUpdate(insert
            + "'" + childName + "', 2, 1)");

        if (!flat) {
            insert = "INSERT INTO UNKNOWNSUBCLASSCHILD (ID) VALUES (";
            c.createStatement().executeUpdate(insert + "1)");
            c.createStatement().executeUpdate(insert + "2)");
        }

        c.commit();
        em.close();
        kem.close();
    }

        public void testUnknownSubclass()
        throws Exception {
        // assertNotRegistered (parentName);
        // assertNotRegistered (childName);

        Class c = UnknownSubclassParent.class;

        // assertNotRegistered (childName);

        EntityManager em= currentEntityManager();            
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        
        startTx(em);
        // c = UnknownSubclassChild.class;

        //FIXME jthomas conversion incomplete 
        /*
        assertSize(1, kem.createExtent(UnknownSubclassParent.class, false));
        assertSize(1, kem.newQuery(
            kem.createExtent(UnknownSubclassParent.class, false), ""));

        assertSize(2, kem.createExtent(UnknownSubclassParent.class, true));
        assertSize(2, kem.createQuery(kem.createExtent(
            UnknownSubclassParent.class, true), ""));
        */
        endTx(em);
        
        em.close();
        kem.close();
    }
    
    
}
