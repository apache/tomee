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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.openejb.test.entity.cmr.manytomany.ALocalHome;
import org.apache.openejb.test.entity.cmr.manytomany.ALocal;
import org.apache.openejb.test.entity.cmr.manytomany.BLocalHome;
import org.apache.openejb.test.entity.cmr.manytomany.BLocal;

/**
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class ManyToManyTests extends AbstractCMRTest {
    private ALocalHome ahome;
    private ALocal a;
    private BLocalHome bhome;
    private BLocal b;

    public ManyToManyTests() {
        super("ManyToMany.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        ahome = (ALocalHome) initialContext.lookup("client/tests/entity/cmr/manyToMany/ALocalHome");
        bhome = (BLocalHome) initialContext.lookup("client/tests/entity/cmr/manyToMany/BLocalHome");
    }

    public void testAGetBExistingAB() throws Exception {
        beginTransaction();
        a = ahome.findByPrimaryKey(new Integer(1));
        Set<BLocal> bSet = a.getB();
        assertEquals(2, bSet.size());
        for (Iterator iter = bSet.iterator(); iter.hasNext();) {
            b = (BLocal) iter.next();
            if ( b.getField1().equals(new Integer(11)) ) {
                assertEquals("value11", b.getField2());
            } else if ( b.getField1().equals(new Integer(22)) ) {
                assertEquals("value22", b.getField2());
            } else {
                fail();
            }
        }
        completeTransaction();
    }

    public void testBGetAExistingAB() throws Exception {
        beginTransaction();
        BLocal b = bhome.findByPrimaryKey(new Integer(22));
        Set aSet = b.getA();
        assertEquals(3, aSet.size());
        for (Iterator iter = aSet.iterator(); iter.hasNext();) {
            a = (ALocal) iter.next();
            if ( a.getField1().equals(new Integer(1)) ) {
                assertEquals("value1", a.getField2());
            } else if ( a.getField1().equals(new Integer(2)) ) {
                assertEquals("value2", a.getField2());
            } else if ( a.getField1().equals(new Integer(3)) ) {
                assertEquals("value3", a.getField2());
            } else {
                fail();
            }
        }
        completeTransaction();
    }

    private void assertStateDropExisting() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBDropExisting() throws Exception {
        beginTransaction();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        a.setB(new HashSet<BLocal>());
        a = ahome.findByPrimaryKey(new Integer(2));
        a.setB(new HashSet<BLocal>());
        a = ahome.findByPrimaryKey(new Integer(3));
        a.setB(new HashSet<BLocal>());
        completeTransaction();

        assertStateDropExisting();
    }

    public void testBSetADropExisting() throws Exception {
        beginTransaction();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));
        b.setA(new HashSet<ALocal>());
        b = bhome.findByPrimaryKey(new Integer(22));
        b.setA(new HashSet<ALocal>());
        completeTransaction();

        assertStateDropExisting();
    }

    private void prepareNewAB() throws Exception {
        beginTransaction();
        a = ahome.create(new Integer(4));
        a.setField2("value4");
        b = bhome.create(new Integer(33));
        b.setField2("value33");
    }

    private void assertStateNewAB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 4 AND fkb1 = 33");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT a2 FROM A WHERE a1 = 4");
        assertTrue(rs.next());
        assertEquals("value4", rs.getString(1));

        rs = s.executeQuery("SELECT b2 FROM B WHERE b1 = 33");
        assertTrue(rs.next());
        assertEquals("value33", rs.getString(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBNewAB() throws Exception {
        prepareNewAB();
        Set<BLocal> bSet = a.getB();
        bSet.add(b);
        completeTransaction();

        assertStateNewAB();
    }

    public void testBSetANewAB() throws Exception {
        prepareNewAB();
        Set<ALocal> aSet = b.getA();
        aSet.add(a);
        completeTransaction();

        assertStateNewAB();
    }

    private void prepareExistingBNewA() throws Exception {
        beginTransaction();
        a = ahome.create(new Integer(4));
        a.setField2("value4");
        b = bhome.findByPrimaryKey(new Integer(11));
    }

    private void assertStateExistingBNewA() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT a2 FROM A WHERE a1 = 4");
        assertTrue(rs.next());
        assertEquals("value4", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 4 AND fkb1 = 11");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBExistingBNewA() throws Exception {
        prepareExistingBNewA();
        Set<BLocal> bSet = a.getB();
        bSet.add(b);
        completeTransaction();

        assertStateExistingBNewA();
    }

    public void testBSetAExistingBNewA() throws Exception {
        prepareExistingBNewA();
        Set<ALocal> aSet = b.getA();
        aSet.add(a);
        completeTransaction();

        assertStateExistingBNewA();
    }

    private void prepareExistingANewB() throws Exception {
        beginTransaction();
        a = ahome.findByPrimaryKey(new Integer(1));
        b = bhome.create(new Integer(33));
        b.setField2("value33");
    }

    private void assertStateExistingANewB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT b2 FROM B WHERE b1 = 33");
        assertTrue(rs.next());
        assertEquals("value33", rs.getString(1));
        rs.close();

        rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 1 AND fkb1 = 33");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testASetBExistingANewB() throws Exception {
        prepareExistingANewB();
        Set<BLocal> bSet = a.getB();
        bSet.add(b);
        completeTransaction();

        assertStateExistingANewB();
    }

    public void testBSetAExistingANewB() throws Exception {
        prepareExistingANewB();
        Set<ALocal> aSet = b.getA();
        aSet.add(a);
        completeTransaction();

        assertStateExistingANewB();
    }

    public void testRemoveRelationships() throws Exception {
        beginTransaction();
        ALocal a = ahome.findByPrimaryKey(new Integer(1));
        a.remove();
        completeTransaction();

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    protected void buildDBSchema(Connection c) throws Exception {
        Statement s = c.createStatement();
        try {
            s.execute("DROP TABLE A");
        } catch (SQLException e) {
            // ignore
        }
        try {
            s.execute("DROP TABLE B");
        } catch (SQLException e) {
            // ignore
        }
        try {
            s.execute("DROP TABLE MTM");
        } catch (SQLException e) {
            // ignore
        }

        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");
        s.execute("CREATE TABLE B(B1 INTEGER, B2 VARCHAR(50), FKA1 INTEGER)");
        s.execute("CREATE TABLE MTM(FKA1 INTEGER, FKB1 INTEGER)");

        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'value1')");
        s.execute("INSERT INTO A(A1, A2) VALUES(2, 'value2')");
        s.execute("INSERT INTO A(A1, A2) VALUES(3, 'value3')");
        s.execute("INSERT INTO B(B1, B2) VALUES(11, 'value11')");
        s.execute("INSERT INTO B(B1, B2) VALUES(22, 'value22')");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(1, 11)");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(1, 22)");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(2, 22)");
        s.execute("INSERT INTO MTM(FKA1, FKB1) VALUES(3, 22)");
        s.close();
        c.close();
    }

}
