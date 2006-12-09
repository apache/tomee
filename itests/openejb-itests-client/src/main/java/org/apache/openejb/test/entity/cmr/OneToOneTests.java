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


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.openejb.test.entity.cmr.onetoone.ALocalHome;
import org.apache.openejb.test.entity.cmr.onetoone.ALocal;
import org.apache.openejb.test.entity.cmr.onetoone.BLocalHome;
import org.apache.openejb.test.entity.cmr.onetoone.BLocal;

/**
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class OneToOneTests extends AbstractCMRTest {
    private ALocalHome ahome;
    private ALocal a;
    private BLocalHome bhome;
    private BLocal b;

    public OneToOneTests() {
        super("OneToOne.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        ahome = (ALocalHome) initialContext.lookup("client/tests/entity/cmr/oneToOne/AHomeLocal");
        bhome = (BLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToOne/BHomeLocal");

        Connection connection = ds.getConnection();
        try {
            buildDBSchema(connection);
        } finally {
            connection.close();
        }
    }

    public void test00_AGetBExistingAB() throws Exception {
        beginTransaction();
        try {
            ALocal a = ahome.findByPrimaryKey(new Integer(1));
            BLocal b = bhome.findByPrimaryKey(new Integer(11));
            assertEquals(new Integer(11), b.getField1());
            assertEquals("value11", b.getField2());
            a.setB(b);
            b = a.getB();
            assertEquals(new Integer(11), b.getField1());
            assertEquals("value11", b.getField2());
        } finally {
            completeTransaction();
        }
    }

    public void test99_AGetBExistingAB() throws Exception {
        beginTransaction();
        try {
            ALocal a = ahome.findByPrimaryKey(new Integer(1));
            BLocal b = a.getB();
            assertNotNull(b);
            assertEquals(new Integer(11), b.getField1());
            assertEquals("value11", b.getField2());
        } finally {
            completeTransaction();
        }
    }

    public void test01_BGetAExistingAB() throws Exception {
        beginTransaction();
        try {
            BLocal b = bhome.findByPrimaryKey(new Integer(11));
            ALocal a = b.getA();
            assertNotNull(a);
            assertEquals(new Integer(1), a.getField1());
            assertEquals("value1", a.getField2());
        } finally {
            completeTransaction();
        }
    }

    private void assertStateDropExisting() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void Xtest02_ASetBDropExisting() throws Exception {
        beginTransaction();
        try {
            ALocal a = ahome.findByPrimaryKey(new Integer(1));
            a.setB(null);
        } finally {
            completeTransaction();
        }

        assertStateDropExisting();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void Xtest03_BSetADropExisting() throws Exception {
        beginTransaction();
        try {
            BLocal b = bhome.findByPrimaryKey(new Integer(11));
            b.setA(null);
        } finally {
            completeTransaction();
        }

        assertStateDropExisting();
    }

    private void prepareNewAB() throws Exception {
        a = ahome.create(new Integer(2));
        a.setField2("value2");
        b = bhome.create(new Integer(22));
        b.setField2("value22");
    }

    private void assertStateNewAB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM OneToOneA WHERE a1 = 2");
        assertTrue(rs.next());
        assertEquals("value2", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM OneToOneB WHERE fka1 = 2");
        assertTrue(rs.next());
        assertEquals(22, rs.getInt(1));
        assertEquals("value22", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }

    public void test04_ASetBNewAB() throws Exception {
        beginTransaction();
        try {
            prepareNewAB();
            a.setB(b);
        } finally {
            completeTransaction();
        }

        assertStateNewAB();
    }

    public void test05_BSetANewAB() throws Exception {
        beginTransaction();
        try {
            prepareNewAB();
            b.setA(a);
        } finally {
            completeTransaction();
        }

        assertStateNewAB();
    }

    private void prepareExistingBNewA() throws Exception {
        a = ahome.create(new Integer(2));
        a.setField2("value2");
        b = bhome.findByPrimaryKey(new Integer(11));
    }

    private void assertStateExistingBNewA() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM OneToOneA WHERE a1 = 2");
        assertTrue(rs.next());
        assertEquals("value2", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM OneToOneB WHERE fka1 = 2");
        assertTrue(rs.next());
        assertEquals(11, rs.getInt(1));
        assertEquals("value11", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }

    public void test06_ASetBExistingBNewA() throws Exception {
        beginTransaction();
        try {
            prepareExistingBNewA();
            a.setB(b);
        } finally {
            completeTransaction();
        }

        assertStateExistingBNewA();
    }

    public void test07_BSetAExistingBNewA() throws Exception {
        beginTransaction();
        try {
            prepareExistingBNewA();
            b.setA(a);
        } finally {
            completeTransaction();
        }

        assertStateExistingBNewA();
    }

    private void prepareExistingANewB() throws Exception {
        a = ahome.findByPrimaryKey(new Integer(1));
        b = bhome.create(new Integer(22));
        b.setField2("value22");
    }

    private void assertStateExistingANewB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT b1, b2 FROM OneToOneB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(22, rs.getInt(1));
        assertEquals("value22", rs.getString(2));
        rs.close();
        s.close();
        c.close();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void Xtest08_ASetBExistingANewB() throws Exception {
        beginTransaction();
        try {
            // The following PrepareStatement does not set to null fka
//          PreparedStatement ps = null;
//          ps = c.prepareStatement("UPDATE B SET value = CASE WHEN ? THEN ? ELSE value END, fka = CASE WHEN ? THEN ? ELSE fka END WHERE b1 = ?");
//          ps.setBoolean(1, false);
//          ps.setString(2, "");
//          ps.setBoolean(3, true);
//          ps.setNull(4);
//          ps.setInt(5, 1);
//          ps.execute();

            prepareExistingANewB();
            a.setB(b);
        } finally {
            completeTransaction();
        }

        assertStateExistingANewB();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void Xtest09_BSetAExistingANewB() throws Exception {
        beginTransaction();
        try {
            prepareExistingANewB();
            b.setA(a);
        } finally {
            completeTransaction();
        }

        assertStateExistingANewB();
    }

    /**
     * TODO Disabled due to an Axion bug. It has been tested with another
     * DB DataSource successfully.
     */
    public void Xtest10_RemoveRelationships() throws Exception {
        beginTransaction();
        try {
            ALocal a = ahome.findByPrimaryKey(new Integer(1));
            a.remove();
        } finally {
            completeTransaction();
        }

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneB WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void test11_CascadeDelete() throws Exception {
        beginTransaction();
        try {
            BLocal b = bhome.findByPrimaryKey(new Integer(11));
            b.remove();
        } finally {
            completeTransaction();
        }

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM OneToOneA WHERE A1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void test12_CMPMappedToForeignKeyColumn() throws Exception {
        beginTransaction();
        try {
            BLocal b = bhome.findByPrimaryKey(new Integer(11));

            Integer field3 = b.getField3();
            assertEquals(b.getA().getPrimaryKey(), field3);
        } finally {
            completeTransaction();
        }
    }

    public void test13_SetCMPMappedToForeignKeyColumn() throws Exception {
        beginTransaction();
        try {
            BLocal b = bhome.findByPrimaryKey(new Integer(11));

            b.setField3(new Integer(2));

            ALocal a = b.getA();
            assertEquals(new Integer(2), a.getField1());
            assertEquals("value2", a.getField2());
        } finally {
            completeTransaction();
        }
    }

    protected void buildDBSchema(Connection c) throws Exception {
        Statement s = c.createStatement();
//        try {
//            s.execute("DROP TABLE A");
//        } catch (SQLException e) {
//            // ignore
//        }
//        try {
//            s.execute("DROP TABLE B");
//        } catch (SQLException e) {
//            // ignore
//        }
//
//        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");
//        s.execute("CREATE TABLE B(B1 INTEGER, B2 VARCHAR(50), FKA1 INTEGER)");

        s.execute("INSERT INTO OneToOneA(A1, A2) VALUES(1, 'value1')");
        s.execute("INSERT INTO OneToOneA(A1, A2) VALUES(2, 'value2')");
        s.execute("INSERT INTO OneToOneB(B1, B2, FKA1) VALUES(11, 'value11', 1)");
        s.close();
        c.close();
    }

}
