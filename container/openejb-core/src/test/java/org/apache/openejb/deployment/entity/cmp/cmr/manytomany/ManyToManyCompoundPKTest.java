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
package org.apache.openejb.deployment.entity.cmp.cmr.manytomany;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.transaction.Transaction;

import org.apache.openejb.deployment.entity.cmp.cmr.AbstractCMRTest;
import org.apache.openejb.deployment.entity.cmp.cmr.CompoundPK;

/**
 *
 * @version $Revision$ $Date$
 */
public class ManyToManyCompoundPKTest extends AbstractCMRTest {
    private ALocalHome ahome;
    private ALocal a;
    private BLocalHome bhome;
    private BLocal b;

    public void testAGetBExistingAB() throws Exception {
        Transaction ctx = newTransaction();
        a = ahome.findByPrimaryKey(new CompoundPK(new Integer(1), "value1"));
        Set bSet = a.getB();
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
        completeTransaction(ctx);
    }
    
    public void testBGetAExistingAB() throws Exception {
        Transaction ctx = newTransaction();
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
        completeTransaction(ctx);
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
        Transaction ctx = newTransaction();
        ALocal a = ahome.findByPrimaryKey(new CompoundPK(new Integer(1), "value1"));
        a.setB(new HashSet());
        a = ahome.findByPrimaryKey(new CompoundPK(new Integer(2), "value2"));
        a.setB(new HashSet());
        a = ahome.findByPrimaryKey(new CompoundPK(new Integer(3), "value3"));
        a.setB(new HashSet());
        completeTransaction(ctx);

        assertStateDropExisting();
    }

    public void testBSetADropExisting() throws Exception {
        Transaction ctx = newTransaction();
        BLocal b = bhome.findByPrimaryKey(new Integer(11));
        b.setA(new HashSet());
        b = bhome.findByPrimaryKey(new Integer(22));
        b.setA(new HashSet());
        completeTransaction(ctx);

        assertStateDropExisting();
    }

    private Transaction prepareNewAB() throws Exception {
        CompoundPK pkA = new CompoundPK(new Integer(4), "value4");
        
        Transaction ctx = newTransaction();
        a = ahome.create(pkA);
        b = bhome.create(new Integer(33));
        b.setField2("value33");
        return ctx;
    }

    private void assertStateNewAB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 4 AND fka2 = 'value4' AND fkb1 = 33");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT COUNT(*) FROM A WHERE a1 = 4 AND a2 = 'value4'");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));

        rs = s.executeQuery("SELECT b2 FROM B WHERE b1 = 33");
        assertTrue(rs.next());
        assertEquals("value33", rs.getString(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBNewAB() throws Exception {
        Transaction ctx = prepareNewAB();
        Set bSet = a.getB();
        bSet.add(b);
        completeTransaction(ctx);
        
        assertStateNewAB();
    }

    public void testBSetANewAB() throws Exception {
        Transaction ctx = prepareNewAB();
        Set aSet = b.getA();
        aSet.add(a);
        completeTransaction(ctx);
        
        assertStateNewAB();
    }

    private Transaction prepareExistingBNewA() throws Exception {
        CompoundPK pkA = new CompoundPK(new Integer(4), "value4");
        
        Transaction ctx = newTransaction();
        a = ahome.create(pkA);
        b = bhome.findByPrimaryKey(new Integer(11));
        return ctx;
    }

    private void assertStateExistingBNewA() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM A WHERE a1 = 4 AND a2 = 'value4'");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 4 AND fka2 = 'value4' AND fkb1 = 11");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBExistingBNewA() throws Exception {
        Transaction ctx = prepareExistingBNewA();
        Set bSet = a.getB();
        bSet.add(b);
        completeTransaction(ctx);
        
        assertStateExistingBNewA();
    }

    public void testBSetAExistingBNewA() throws Exception {
        Transaction ctx = prepareExistingBNewA();
        Set aSet = b.getA();
        aSet.add(a);
        completeTransaction(ctx);
        
        assertStateExistingBNewA();
    }

    private Transaction prepareExistingANewB() throws Exception {
        CompoundPK pkA = new CompoundPK(new Integer(1), "value1");
        
        Transaction ctx = newTransaction();
        a = ahome.findByPrimaryKey(pkA);
        b = bhome.create(new Integer(33));
        b.setField2("value33");
        return ctx;
    }
    
    private void assertStateExistingANewB() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT b2 FROM B WHERE b1 = 33");
        assertTrue(rs.next());
        assertEquals("value33", rs.getString(1));
        rs.close();
        
        rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 1 AND fka2 = 'value1' AND fkb1 = 33");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    public void testASetBExistingANewB() throws Exception {
        Transaction ctx = prepareExistingANewB();
        Set bSet = a.getB();
        bSet.add(b);
        completeTransaction(ctx);
        
        assertStateExistingANewB();
    }

    public void testBSetAExistingANewB() throws Exception {
        Transaction ctx = prepareExistingANewB();
        Set aSet = b.getA();
        aSet.add(a);
        completeTransaction(ctx);
        
        assertStateExistingANewB();
    }

    public void testRemoveRelationships() throws Exception {
        Transaction ctx = newTransaction();
        ALocal a = ahome.findByPrimaryKey(new CompoundPK(new Integer(1), "value1"));
        a.remove();
        completeTransaction(ctx);

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM MTM WHERE fka1 = 1 AND fka2 = 'value1'");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        ahome = (ALocalHome) super.ahome;
        bhome = (BLocalHome) super.bhome;
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
        s.execute("CREATE TABLE B(B1 INTEGER, B2 VARCHAR(50))");
        s.execute("CREATE TABLE MTM(FKA1 INTEGER, FKA2 VARCHAR(50), FKB1 INTEGER)");
        
        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'value1')");
        s.execute("INSERT INTO A(A1, A2) VALUES(2, 'value2')");
        s.execute("INSERT INTO A(A1, A2) VALUES(3, 'value3')");
        s.execute("INSERT INTO B(B1, B2) VALUES(11, 'value11')");
        s.execute("INSERT INTO B(B1, B2) VALUES(22, 'value22')");
        s.execute("INSERT INTO MTM(FKA1, FKA2, FKB1) VALUES(1, 'value1', 11)");
        s.execute("INSERT INTO MTM(FKA1, FKA2, FKB1) VALUES(1, 'value1', 22)");
        s.execute("INSERT INTO MTM(FKA1, FKA2, FKB1) VALUES(2, 'value2', 22)");
        s.execute("INSERT INTO MTM(FKA1, FKA2, FKB1) VALUES(3, 'value3', 22)");        
        s.close();
        c.close();
    }

    protected String getEjbJarDD() {
        return "src/test-cmp/manytomany/compoundpk/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test-cmp/manytomany/compoundpk/openejb-jar.xml";
    }

    protected EJBClass getA() {
        return new EJBClass(ABean.class, ALocalHome.class, ALocal.class);
    }

    protected EJBClass getB() {
        return new EJBClass(BBean.class, BLocalHome.class, BLocal.class);
    }
    
}
