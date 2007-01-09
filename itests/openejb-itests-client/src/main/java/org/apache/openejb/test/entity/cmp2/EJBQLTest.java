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
package org.apache.openejb.test.entity.cmp2;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.openejb.test.entity.ejbql.AHome;
import org.apache.openejb.test.entity.ejbql.ALocalHome;
import org.apache.openejb.test.entity.ejbql.ARemote;
import org.apache.openejb.test.entity.ejbql.ALocal;
import junit.framework.TestCase;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class EJBQLTest extends TestCase {

    private AHome aHome;
    private ALocalHome aLocalHome;

    public void testHomeFindTest() throws Exception {
        ARemote a = aHome.findTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    public void testLocalHomeFindTest() throws Exception {
        ALocal a = aLocalHome.findTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    public void testSelectTest() throws Exception {
        ALocal a = aLocalHome.selectTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    protected void buildDBSchema(Connection c) throws Exception {
        Statement s = c.createStatement();
        try {
            s.execute("DROP TABLE A");
        } catch (SQLException e) {
            // ignore
        }

        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");

        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'test')");
        s.close();
        c.close();
    }

    protected String getEjbJarDD() {
        return "src/test-cmp/ejb-ql/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test-cmp/ejb-ql/openejb-jar.xml";
    }

    protected void setUp() throws Exception {
        super.setUp();
//        aLocalHome = (ALocalHome) kernel.getAttribute(deploymentName, "ejbLocalHome");
//        aHome = (AHome) kernel.getAttribute(deploymentName, "ejbHome");
    }
}