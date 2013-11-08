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
/*
 * TestByteArrayAppId.java
 *
 * Created on October 2, 2006, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


import org.apache.openjpa.persistence.jdbc.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import java.util.*;

import javax.persistence.EntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.kernel.Extent;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.kernel.Query;


public class TestByteArrayAppId extends BaseJDBCTest {
    
    private static boolean _init = false;
    private static int TEST_COUNT = 0;
    private static OpenJPAEntityManagerFactory _pmf = null;
    
    /** Creates a new instance of TestByteArrayAppId */
    public TestByteArrayAppId(String name) 
    {
    	super(name);
    }
    
    public boolean skipTest() {
        return getCurrentPlatform() != AbstractTestCase.Platform.DB2;
    }
    
    @Override
    public void setUp() throws Exception {
        // we have to use getbytes/setbytes for byte arrays to work properly        
        if (!_init) {
            _pmf =(OpenJPAEntityManagerFactory) getEmf(getProps());
            initialize((JDBCConfiguration) ((OpenJPAEntityManagerFactorySPI)
                    OpenJPAPersistence.cast(_pmf)).getConfiguration());
            _init = true;
        }
        EntityManager pm = _pmf.createEntityManager();
        startTx(pm);
        
        deleteAll(ByteArrayPKPC.class,pm);
        endTx(pm);
        pm.close();
        pm = currentEntityManager();
        startTx(pm);
        ByteArrayPKPC2 testBytes = new ByteArrayPKPC2(new byte[]{ 1, 2 },
                "child");
        testBytes.setSubfield("sub");
        testBytes.setParent(new ByteArrayPKPC(new byte[]{ 3, 4 }, "parent"));
        pm.persist(testBytes);
        endTx(pm);
        pm.close();
        TEST_COUNT++;
    }
    
    public void tearDown()
    throws Exception {
        // closing the pmf every time slows things down too much b/c
        // schema reflection is so slow on DB2
        if (TEST_COUNT >= 9) {
            closeEMF(_pmf);
            _pmf = null;
            _init = false;
            super.tearDown();
        }
    }
    
    private void initialize(JDBCConfiguration conf)
    throws Exception {
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        //JDBCConfiguration conf = (JDBCConfiguration) kem.getConfiguration();
        
        MappingTool tool = new MappingTool((JDBCConfiguration)
                ((OpenJPAEntityManagerSPI) kem).getConfiguration(),
                MappingTool.ACTION_REFRESH, false);
        tool.run(ByteArrayPKPC.class);
        tool.run(ByteArrayPKPC2.class);
        tool.record();
    }
    
    /**
     * Tests that application identity classes are returned correctly.
     */
    public void testGetObjectIdClass() {
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager pm = OpenJPAPersistence.cast (em);

        
        assertEquals(ByteArrayPKPCId.class,
                pm.getObjectIdClass(ByteArrayPKPC.class));
        assertEquals(ByteArrayPKPCId.class,
                pm.getObjectIdClass(ByteArrayPKPC2.class));
        pm.close();
        em.close();
    }
    
    /**
     * Tests finding an instance by a manually-created id value.
     */
    public void testGetSubclassObjectById() {
        ByteArrayPKPC2 bytes2 = getChild();
        assertNotNull(bytes2);
        assertEquals(1, bytes2.getPK()[0]);
        assertEquals(2, bytes2.getPK()[1]);
        assertEquals("child", bytes2.getStringField());
        assertEquals("sub", bytes2.getSubfield());
        assertNotNull(bytes2.getParent());
        assertEquals(3, bytes2.getParent().getPK()[0]);
        assertEquals(4, bytes2.getParent().getPK()[1]);
        assertEquals("parent", bytes2.getParent().getStringField());
        assertNull(bytes2.getParent().getParent());
        //FIXME next line commented 
        //JDOHelper.getPersistenceManager(bytes2).close();
    }
    
    /**
     * Tests finding an instance by a manually-created id value.
     */
    public void testGetObjectById() {
        ByteArrayPKPC bytes = getParent();
        assertNotNull(bytes);
        assertEquals(3, bytes.getPK()[0]);
        assertEquals(4, bytes.getPK()[1]);
        assertEquals("parent", bytes.getStringField());
        assertNull(bytes.getParent());
        //FIXME next line commented 
        //JDOHelper.getPersistenceManager(bytes).close();
    }
    
    /**
     * Tests that the oid instances returned from the pm are copied to
     * prevent by-reference modification by the user.
     */
    public void testGetObjectId() {
        
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        if (! ((OpenJPAEntityManagerSPI) kem).getConfiguration()
                .getCompatibilityInstance().getCopyObjectIds())
            return;
        
        ByteArrayPKPCId oid = new ByteArrayPKPCId();
        oid.pk = new byte[]{ 1, 2 };
        ByteArrayPKPC bytes = (ByteArrayPKPC) kem.getObjectId(oid);
        ByteArrayPKPCId oidCopy = (ByteArrayPKPCId) kem.getObjectId(bytes);
        assertTrue("Oid not copied.", oid != oidCopy);
        assertEquals(1, oidCopy.pk[0]);
        assertEquals(2, oidCopy.pk[1]);
        
        em.close();
        kem.close();
    }
    
    /**
     * Tests that changing primary key values will fail.
     */
    public void testChangeIdentity() {
        ByteArrayPKPC2 bytes = getChild();
        OpenJPAEntityManager pm = OpenJPAPersistence.getEntityManager(bytes);
        startTx(pm);
        
        // make sure setting to same value is OK
        bytes.setPK(bytes.getPK());
        try {
            bytes.setPK(new byte[]{ 5, 6 });
            fail("Allowed changing of pk.");
        } catch (Exception je) {
        }
        rollbackTx(pm);
        pm.close();
    }
    
    /**
     * Tests that pk fields are retained on state transition to hollow.
     */
    public void testPKRetain() {
        ByteArrayPKPC2 bytes = getChild();
        OpenJPAEntityManager pm = OpenJPAPersistence.getEntityManager(bytes);
        
        //FIXME next line commented .... need substitute API
        //pm.currentTransaction().setNontransactionalRead(false);
        
        startTx(pm);
        bytes.setParent(null);
        endTx(pm);        
        
        // bytes should still allow access to oid fields
        assertEquals(1, bytes.getPK()[0]);
        assertEquals(2, bytes.getPK()[1]);
        try {
            bytes.getParent();
            fail("Allowed read of non-pk value outside of transaction.");
        } catch (Exception je) {
        }
        try {
            bytes.setPK(new byte[]{ 5, 6 });
            fail("Allowed setting of pk value outside of transaction.");
        } catch (Exception je) {
        }
        pm.close();
    }
    
    public void testDeleteAndInsert() {
        EntityManager em= currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        
        startTx(kem);
       
        for (int i = 0; i < 20; i++) {
            ByteArrayPKPC bytes = new ByteArrayPKPC(new byte[]
            { (byte) (i + 5), (byte) (i + 6) }, String.valueOf(i));
            kem.persist(bytes);
        }
        endTx(kem);        
        
        kem.close();
        
        kem = _pmf.createEntityManager();
        startTx(kem);
        Extent extent = (Extent) kem.createExtent(ByteArrayPKPC.class,false);
        kem.detachAll(extent.list());
        extent.closeAll();
        
        ByteArrayPKPC owner = new ByteArrayPKPC();
        kem.persist(owner);
        owner.setPK(new byte[]{ 100, 101 });
        owner.setStringField("owner");
        
        // make new instances with same oids persistent
        for (int i = 0; i < 20; i++) {
            ByteArrayPKPC bytes = new ByteArrayPKPC(new byte[]
            { (byte) (i + 5), (byte) (i + 6) }, String.valueOf(i));
            kem.persist(bytes);
            assertEquals(bytes,
                    getStateManager(bytes, kem).getManagedInstance());
            owner.getRels().add(bytes);
        }
        
        endTx(kem);  
        
        Object oid = kem.getObjectId(owner);
        assertOwner(kem, oid);
        kem.close();
        
        kem = _pmf.createEntityManager();
        assertOwner(kem, oid);
        em.close();
        kem.close();
    }
    
    public void testQuery() {
        EntityManager pm = _pmf.createEntityManager();
        //FIXME jthomas - partly commented 
        //Query q = pm.newQuery(ByteArrayPKPC.class, "pk == bytes");
        Query q =null;//= pm.newQuery(ByteArrayPKPC.class, "pk == bytes");
        q.declareParameters("byte[] bytes");
        //FIXME jthomas - no execute for byte 
        //Collection results = (Collection) q.execute(new byte[]{ 1, 2 });
        Collection results =null;//= (Collection) q.execute(new byte[]{ 1, 2 });
        assertEquals(1, results.size());
        ByteArrayPKPC2 child = (ByteArrayPKPC2) results.iterator().next();
        assertEquals("child", child.getStringField());
        q.closeAll();
        pm.close();
    }
    
    public void testConflictingIds() {
        OpenJPAEntityManager pm = _pmf.createEntityManager();
        startTx(pm);     
        
        // make a bunch of objects persistent with the same initial pk values
        ByteArrayPKPC owner = new ByteArrayPKPC();
        pm.persist(owner);
        owner.setPK(new byte[]{ 100, 101 });
        owner.setStringField("owner");
        for (int i = 0; i < 20; i++) {
            ByteArrayPKPC bytes = new ByteArrayPKPC();
            pm.persist(bytes);
            assertEquals(bytes, getStateManager(bytes, pm).
                    getManagedInstance());
            bytes.setPK(new byte[]{ (byte) (i + 5), (byte) (i + 6) });
            bytes.setStringField(String.valueOf(i));
            owner.getRels().add(bytes);
        }
        endTx(pm);
        Object oid = pm.getObjectId(owner);
        assertOwner(pm, oid);
        pm.close();
        
        pm = _pmf.createEntityManager();
        assertOwner(pm, oid);
        pm.close();
    }
    
    private void assertOwner(OpenJPAEntityManager pm, Object oid) {
        ByteArrayPKPC owner = (ByteArrayPKPC) pm.getObjectId(oid);
        assertEquals(100, owner.getPK()[0]);
        assertEquals(101, owner.getPK()[1]);
        assertEquals("owner", owner.getStringField());
        List rels = owner.getRels();
        assertEquals(20, rels.size());
        for (int i = 0; i < rels.size(); i++) {
            ByteArrayPKPC bytes = (ByteArrayPKPC) rels.get(i);
            assertEquals(i + 5, bytes.getPK()[0]);
            assertEquals(i + 6, bytes.getPK()[1]);
            assertEquals(String.valueOf(i), bytes.getStringField());
        }
    }
    
    private ByteArrayPKPC getParent() {
        ByteArrayPKPCId oid = new ByteArrayPKPCId();
        oid.pk = new byte[]{ 3, 4 };
        OpenJPAEntityManager pm = _pmf.createEntityManager();
        return (ByteArrayPKPC) pm.getObjectId(oid);
    }
    
    private ByteArrayPKPC2 getChild() {
        ByteArrayPKPCId oid = new ByteArrayPKPCId();
        oid.pk = new byte[]{ 1, 2 };
        OpenJPAEntityManager pm = _pmf.createEntityManager();
        return (ByteArrayPKPC2) pm.getObjectId(oid);
    }
    
    public static void main(String[] args) {
       // main();
    }
    
    private Map getProps() {
        Map props=new HashMap();
        props.put("openjpa.jdbc.DBDictionary", "");
        props.put("UseGetBytesForBlobs", "true");
        props.put("UseSetBytesForBlobs", "true");
        props.put("BatchLimit", "0");
        
        return props;
    }
    
    
}
