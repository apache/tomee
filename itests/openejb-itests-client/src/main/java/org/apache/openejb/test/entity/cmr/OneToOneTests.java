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
package org.apache.openejb.test.entity.cmr;


import org.apache.openejb.test.entity.cmr.onetoone.ALocal;
import org.apache.openejb.test.entity.cmr.onetoone.ALocalHome;
import org.apache.openejb.test.entity.cmr.onetoone.BLocal;
import org.apache.openejb.test.entity.cmr.onetoone.BLocalHome;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class OneToOneTests extends AbstractCMRTest {
    private ALocalHome ahome;
    private BLocalHome bhome;

    public OneToOneTests() {
        super("OneToOne.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        ahome = (ALocalHome) initialContext.lookup("client/tests/entity/cmr/oneToOne/AHomeLocal");
        bhome = (BLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToOne/BHomeLocal");
    }

    public void test00_AGetBExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            BLocal b = a.getB();
            assertNotNull(b);
            assertEquals(new Integer(11), b.getField1());
            assertEquals("value11", b.getField2());
        } finally {
            completeTransaction();
        }
    }

    public void test01_BGetAExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            BLocal b = findB(11);
            ALocal a = b.getA();
            assertNotNull(a);
            assertEquals(new Integer(1), a.getField1());
            assertEquals("value1", a.getField2());
        } finally {
            completeTransaction();
        }
    }

    public void test02_ASetBDropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            a.setB(null);
        } finally {
            completeTransaction();
        }

        assertUnlinked(1);
    }

    public void test03_BSetADropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            BLocal b = findB(11);
            b.setA(null);
        } finally {
            completeTransaction();
        }

        assertUnlinked(1);
    }

    public void test04_ASetBNewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = createB(22);
            a.setB(b);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 22);
    }

    public void test05_BSetANewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = createB(22);
            b.setA(a);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 22);
    }

    public void test06_ASetBExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = findB(11);
            a.setB(b);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 11);
    }

    public void test07_BSetAExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = createA(2);
            BLocal b = findB(11);
            b.setA(a);
        } finally {
            completeTransaction();
        }
        assertLinked(2, 11);
    }

    public void test09_BSetAExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            BLocal b = createB(22);
            b.setA(a);
        } finally {
            completeTransaction();
        }
        assertLinked(1, 22);
    }

    public void test10_RemoveRelationships() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            a.remove();
        } finally {
            completeTransaction();
        }

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        close(rs);
        rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }

    public void test11_CascadeDelete() throws Exception {
        resetDB();
        beginTransaction();
        try {
            BLocal b = findB(11);
            b.remove();
        } finally {
            completeTransaction();
        }

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneA WHERE A1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }

    // todo enable these when field to fk is implemented
    public void Xtest12_CMPMappedToForeignKeyColumn() throws Exception {
        resetDB();
        beginTransaction();
        try {
            BLocal b = findB(11);

            Integer field3 = b.getField3();
            assertEquals(b.getA().getPrimaryKey(), field3);
        } finally {
            completeTransaction();
        }
    }

    // todo enable these when field to fk is implemented
    public void Xtest13_SetCMPMappedToForeignKeyColumn() throws Exception {
        resetDB();
        beginTransaction();
        try {
            BLocal b = findB(11);

            b.setField3(new Integer(2));

            ALocal a = b.getA();
            assertEquals(new Integer(2), a.getField1());
            assertEquals("value2", a.getField2());
        } finally {
            completeTransaction();
        }
    }

    private ALocal createA(int aPk) throws CreateException {
        ALocal a = ahome.create(new Integer(aPk));
        a.setField2("value" + aPk);
        return a;
    }

    private ALocal findA(int aPk) throws FinderException {
        return ahome.findByPrimaryKey(new Integer(aPk));
    }

    private BLocal createB(int bPk) throws CreateException {
        BLocal b = bhome.create(new Integer(bPk));
        b.setField2("value" + bPk);
        return b;
    }
    private BLocal findB(int bPk) throws FinderException {
        return bhome.findByPrimaryKey(new Integer(bPk));
    }


    private void assertLinked(int aPk, int bPk) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM OneToOneA WHERE a1 = " + aPk);
        assertTrue(rs.next());
        assertEquals("value" + aPk, rs.getString("a2"));
        close(rs);

        rs = s.executeQuery("SELECT b1, b2 FROM OneToOneB WHERE fka1 = " + aPk);
        assertTrue(rs.next());
        assertEquals(bPk, rs.getInt("b1"));
        assertEquals("value" + bPk, rs.getString("b2"));
        close(rs);
        close(s);
        close(c);
    }

    private void assertUnlinked(int aPk) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB WHERE fka1 = " + aPk);
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }


    private void resetDB() throws Exception {
        Connection connection = ds.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();

            statement.execute("DELETE FROM OneToOneA");
            statement.execute("DELETE FROM OneToOneB");

            statement.execute("INSERT INTO OneToOneA(A1, A2) VALUES(1, 'value1')");
            statement.execute("INSERT INTO OneToOneA(A1, A2) VALUES(2, 'value2')");
            statement.execute("INSERT INTO OneToOneB(B1, B2, FKA1) VALUES(11, 'value11', 1)");
        } finally {
            close(statement);
            close(connection);
        }
    }

    protected void dump() throws Exception {
        dumpTable(ds, "OneToOneA");
        dumpTable(ds, "OneToOneB");
    }
}
