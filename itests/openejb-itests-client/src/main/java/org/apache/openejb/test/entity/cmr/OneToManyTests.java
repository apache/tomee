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

import org.apache.openejb.test.entity.cmr.onetomany.ALocal;
import org.apache.openejb.test.entity.cmr.onetomany.ALocalHome;
import org.apache.openejb.test.entity.cmr.onetomany.BLocal;
import org.apache.openejb.test.entity.cmr.onetomany.BLocalHome;

import javax.ejb.FinderException;
import javax.ejb.CreateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class OneToManyTests extends AbstractCMRTest {
    private ALocalHome ahome;
    private BLocalHome bhome;

    public OneToManyTests() {
        super("OneToMany.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        ahome = (ALocalHome) initialContext.lookup("client/tests/entity/cmr/oneToMany/AHomeLocal");
        bhome = (BLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToMany/BHomeLocal");
    }

    public void test00_AGetBExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            Set bSet = a.getB();
            assertEquals(2, bSet.size());
            for (Object value : bSet) {
                BLocal b = (BLocal) value;
                if (b.getField1().equals(new Integer(11))) {
                    assertEquals("value11", b.getField2());
                } else if (b.getField1().equals(new Integer(22))) {
                    assertEquals("value22", b.getField2());
                } else {
                    fail();
                }
            }
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

            b = findB(22);
            a = b.getA();
            assertNotNull(a);
            assertEquals(new Integer(1), a.getField1());
            assertEquals("value1", a.getField2());
        } finally {
            completeTransaction();
        }
    }

    public void testASetBDropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            a.setB(new HashSet<BLocal>());
        } finally {
            completeTransaction();
        }
        assertUnlinked(1);
    }

    public void testBSetADropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            BLocal b = findB(11);
            b.setA(null);
            b = findB(22);
            b.setA(null);
        } finally {
            completeTransaction();
        }

        assertUnlinked(1);
    }


    public void testASetBNewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = findB(22);
            Set<BLocal> bSet = new HashSet<BLocal>();
            bSet.add(b);
            a.setB(bSet);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 22);
    }

    public void testBSetANewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = findB(22);
            b.setA(a);
        } finally {
            completeTransaction();
        }
        assertLinked(2, 22);
    }

    public void testASetBExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = findB(11);
            Set<BLocal> bSet = a.getB();
            bSet.add(b);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 11);
    }

    public void testBSetAExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(2);
            BLocal b = findB(11);
            b.setA(a);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 11);
    }

    public void testASetBExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            BLocal b = createB(33);
            Set<BLocal> bSet = a.getB();
            bSet.add(b);
        } finally {
            completeTransaction();
        }
        assertLinked(1, 11, 22, 33);
    }

    public void testBSetAExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            ALocal a = findA(1);
            BLocal b = createB(33);
            b.setA(a);
        } finally {
            completeTransaction();
        }

        assertLinked(1, 11, 22, 33);
    }

    public void testRemoveRelationships() throws Exception {
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
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToManyB");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        rs.close();
        rs = s.executeQuery("SELECT COUNT(*) FROM OneToManyB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    // uncomment when cmp to cmr is supported
    public void TODO_testCMPMappedToForeignKeyColumn() throws Exception {
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

    // uncomment when cmp to cmr is supported
    public void TODO_testSetCMPMappedToForeignKeyColumn() throws Exception {
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

    // todo cascade delete isn't working
    public void TODO_testCascadeDelete() throws Exception {
        resetDB();

        beginTransaction();
        try {
            ALocal a = findA(1);
            a.remove();
        } finally {
            completeTransaction();
        }
        System.out.println();
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToManyB");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

//    private ALocal createA(int aPk) throws CreateException {
//        ALocal a = ahome.create(new Integer(aPk));
//        a.setField2("value" + aPk);
//        return a;
//    }

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

    private void assertLinked(int aPk, int... bPks) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM OneToManyA WHERE a1 = " + aPk);
        assertTrue(rs.next());
        assertEquals("value" + aPk, rs.getString("a2"));
        close(rs);

        // assert that there we are looking for the same number of linked beans
        rs = s.executeQuery("SELECT COUNT(*) FROM OneToManyB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(bPks.length, rs.getInt(1));
        rs.close();

        // assert each of the listed b pks is linked to a
        for (int bPk : bPks) {
            rs = s.executeQuery("SELECT b2, fka1 FROM OneToManyB WHERE b1 = " + bPk);
            assertTrue(rs.next());
            assertEquals("value" + bPk, rs.getString("b2"));
            assertEquals(aPk, rs.getInt("fka1"));
            close(rs);
        }
        close(s);
        close(c);
    }

    private void assertUnlinked(int aPk) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToManyB WHERE fka1 = " + aPk);
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

            statement.execute("DELETE FROM OneToManyA");
            statement.execute("DELETE FROM OneToManyB");

            statement.execute("INSERT INTO OneToManyA(A1, A2) VALUES(1, 'value1')");
            statement.execute("INSERT INTO OneToManyA(A1, A2) VALUES(2, 'value2')");
            statement.execute("INSERT INTO OneToManyB(B1, B2, FKA1) VALUES(11, 'value11', 1)");
            statement.execute("INSERT INTO OneToManyB(B1, B2, FKA1) VALUES(22, 'value22', 1)");
        } finally {
            close(statement);
            close(connection);
        }
    }

    private void dump() throws SQLException {
        dumpTable(ds, "OneToManyA");
        dumpTable(ds, "OneToManyb");
    }
}
