/*
 * TestIrregularJoins.java
 *
 * Created on October 3, 2006, 12:47 PM
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
package org.apache.openjpa.persistence.jdbc.meta;

import java.util.*;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestIrregularJoins
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {

    private Object _nonpk = null;
    private Object _nonpk3 = null;
    private Object _partial = null;
    private Object _partial3 = null;
    private Object _constant2 = null;
    private Object _constant3 = null;

    public TestIrregularJoins(String test) {
        super(test);
    }
    
    /** Creates a new instance of TestIrregularJoins */
    public TestIrregularJoins() {
    }

    public void setUp()
        throws Exception {
        // make sure these classes are registered since we're using metadata
        // values for the class indicator
        new ConstantJoinPC();
        new ConstantJoinPC2();
        new ConstantJoinPC3();

       deleteAll(PartialJoinPC.class);
       deleteAll(NonPKJoinPC.class);
       deleteAll(ConstantJoinPC.class);

        PartialJoinPC partial = new PartialJoinPC();
        partial.setPk1(1);
        partial.setPk2(2);
        PartialJoinPC2 partial2 = new PartialJoinPC2();
        partial2.setPk1(2);
        partial2.setPk2(3);
        PartialJoinPC3 partial3 = new PartialJoinPC3();
        partial3.setPk1(3);
        partial3.setPk2(4);

        NonPKJoinPC nonpk = new NonPKJoinPC();
        nonpk.setId1(1);
        nonpk.setId2(2);
        NonPKJoinPC2 nonpk2 = new NonPKJoinPC2();
        nonpk2.setId1(2);
        nonpk2.setId2(3);
        NonPKJoinPC3 nonpk3 = new NonPKJoinPC3();
        nonpk3.setId1(3);
        nonpk3.setId2(4);

        partial.setNonPK(nonpk);
        partial.getNonPKs().add(nonpk);
        nonpk.setPartial(partial);
        nonpk.getPartials().add(partial);

        partial3.setNonPK(nonpk);
        partial3.setNonPK3(nonpk3);
        partial3.getNonPKs().add(nonpk);
        partial3.getNonPK2s().add(nonpk2);
        nonpk3.setPartial(partial);
        nonpk3.setPartial3(partial3);
        nonpk3.getPartials().add(partial);
        nonpk3.getPartial2s().add(partial2);

        ConstantJoinPC2 constant2 = new ConstantJoinPC2();
        constant2.setPk1(1);
        constant2.setPk2(2);
        constant2.setNonPK(nonpk3);
        constant2.setNonPK2(nonpk3);

        // set the object's inverse on diff objects so we can be sure
        // that its 1-many includes only the object with the right constant
        nonpk.setConstant(constant2);
        nonpk2.setConstant(constant2);
        nonpk3.setConstant(constant2);

        ConstantJoinPC3 constant3 = new ConstantJoinPC3();
        constant3.setPk1(1);
        constant3.setPk2(3);

        OpenJPAEntityManager pm =(OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        
        pm.persist(partial);
        pm.persist(partial3);
        pm.persist(constant2);
        pm.persist(constant3);
        endTx(pm);
        _partial = pm.getObjectId(partial);
        _partial3 = pm.getObjectId(partial3);
        _nonpk = pm.getObjectId(nonpk);
        _nonpk3 = pm.getObjectId(nonpk3);
        _constant2 = pm.getObjectId(constant2);
        _constant3 = pm.getObjectId(constant3);
        pm.close();
    }

    public void testNonPKOneOne() {
        
        nonPKOneOne((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerNonPKOneOne() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nonPK");
        nonPKOneOne(pm);
    }

    private void nonPKOneOne(OpenJPAEntityManager pm) {
        PartialJoinPC partial = (PartialJoinPC) pm.getObjectId(_partial);
        NonPKJoinPC nonpk = partial.getNonPK();
        assertEquals(1, nonpk.getId1());
        assertEquals(2, nonpk.getId2());
        pm.close();
    }

    public void testPartialOneOne() {
        partialOneOne((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerPartialOneOne() {
        
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("partial");
        partialOneOne(pm);
    }

    private void partialOneOne(OpenJPAEntityManager pm) {
        NonPKJoinPC nonpk = (NonPKJoinPC) pm.getObjectId(_nonpk);
        PartialJoinPC partial = nonpk.getPartial();
        assertEquals(1, partial.getPk1());
        assertEquals(2, partial.getPk2());
        pm.close();
    }

    public void testVerticalNonPKOneOne() {
        verticalNonPKOneOne((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerVerticalNonPKOneOne() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nonPK3");
        verticalNonPKOneOne(pm);
    }

    private void verticalNonPKOneOne(OpenJPAEntityManager pm) {
        PartialJoinPC3 partial3 = (PartialJoinPC3) pm.getObjectId(_partial3);
        NonPKJoinPC nonpk = partial3.getNonPK();
        assertEquals(1, nonpk.getId1());
        assertEquals(2, nonpk.getId2());
        NonPKJoinPC3 nonpk3 = partial3.getNonPK3();
        assertEquals(3, nonpk3.getId1());
        assertEquals(4, nonpk3.getId2());
        pm.close();
    }

    public void testVerticalPartialOneOne() {
        verticalPartialOneOne((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerVerticalPartialOneOne() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("partial3");
        verticalPartialOneOne(pm);
    }

    private void verticalPartialOneOne(OpenJPAEntityManager pm) {
        NonPKJoinPC3 nonpk3 = (NonPKJoinPC3) pm.getObjectId(_nonpk3);
        PartialJoinPC partial = nonpk3.getPartial();
        assertEquals(1, partial.getPk1());
        assertEquals(2, partial.getPk2());
        PartialJoinPC3 partial3 = nonpk3.getPartial3();
        assertEquals(3, partial3.getPk1());
        assertEquals(4, partial3.getPk2());
        pm.close();
    }

    public void testNonPKManyMany() {
        nonPKManyMany((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerNonPKManyMany() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nonPKs");
        nonPKManyMany(pm);
    }

    private void nonPKManyMany(OpenJPAEntityManager pm) {
        PartialJoinPC partial = (PartialJoinPC) pm.getObjectId(_partial);
        Collection nonpks = partial.getNonPKs();
        assertEquals(1, nonpks.size());
        NonPKJoinPC nonpk = (NonPKJoinPC) nonpks.iterator().next();
        assertEquals(1, nonpk.getId1());
        assertEquals(2, nonpk.getId2());
        pm.close();
    }

    public void testPartialManyMany() {
        partialManyMany((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerPartialManyMany() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("partials");
        partialManyMany(pm);
    }

    private void partialManyMany(OpenJPAEntityManager pm) {
        NonPKJoinPC nonpk = (NonPKJoinPC) pm.getObjectId(_nonpk);
        Collection partials = nonpk.getPartials();
        assertEquals(1, partials.size());
        PartialJoinPC partial = (PartialJoinPC) partials.iterator().next();
        assertEquals(1, partial.getPk1());
        assertEquals(2, partial.getPk2());
        pm.close();
    }

    public void testVerticalNonPKManyMany() {
        verticalNonPKManyMany((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerVerticalNonPKManyMany() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nonPK2s");
        verticalNonPKManyMany(pm);
    }

    private void verticalNonPKManyMany(OpenJPAEntityManager pm) {
        PartialJoinPC3 partial3 = (PartialJoinPC3) pm.getObjectId(_partial3);
        Collection nonpks = partial3.getNonPKs();
        assertEquals(1, nonpks.size());
        NonPKJoinPC nonpk = (NonPKJoinPC) nonpks.iterator().next();
        assertEquals(1, nonpk.getId1());
        assertEquals(2, nonpk.getId2());
        Collection nonpk2s = partial3.getNonPK2s();
        assertEquals(1, nonpk2s.size());
        NonPKJoinPC2 nonpk2 = (NonPKJoinPC2) nonpk2s.iterator().next();
        assertEquals(2, nonpk2.getId1());
        assertEquals(3, nonpk2.getId2());
        pm.close();
    }

    public void testVerticalPartialManyMany() {
        verticalPartialManyMany((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerVerticalPartialManyMany() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("partial2s");
        verticalPartialManyMany(pm);
    }

    private void verticalPartialManyMany(OpenJPAEntityManager pm) {
        NonPKJoinPC3 nonpk3 = (NonPKJoinPC3) pm.getObjectId(_nonpk3);
        Collection partials = nonpk3.getPartials();
        assertEquals(1, partials.size());
        PartialJoinPC partial = (PartialJoinPC) partials.iterator().next();
        assertEquals(1, partial.getPk1());
        assertEquals(2, partial.getPk2());
        Collection partial2s = nonpk3.getPartial2s();
        assertEquals(1, partial2s.size());
        PartialJoinPC2 partial2 = (PartialJoinPC2) partial2s.iterator().next();
        assertEquals(2, partial2.getPk1());
        assertEquals(3, partial2.getPk2());
        pm.close();
    }

    public void testVerticalConstant() {
        
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        ConstantJoinPC2 constant2 =
            (ConstantJoinPC2) pm.getObjectId(_constant2);
        assertEquals(1, constant2.getPk1());
        assertEquals(2, constant2.getPk2());
        ConstantJoinPC3 constant3 =
            (ConstantJoinPC3) pm.getObjectId(_constant3);
        assertEquals(1, constant3.getPk1());
        assertEquals(3, constant3.getPk2());
    }

    public void testConstantOneOne() {
        constantOneOne((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerConstantOneOne() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nonPK");
        constantOneOne(pm);
    }

    private void constantOneOne(OpenJPAEntityManager pm) {
        ConstantJoinPC2 constant2 =
            (ConstantJoinPC2) pm.getObjectId(_constant2);

        assertEquals(_nonpk3, pm.getObjectId(constant2.getNonPK()));
        assertEquals(_nonpk3, pm.getObjectId(constant2.getNonPK2()));
        pm.close();
    }

    public void testConstantOneMany() {
        constantOneMany((OpenJPAEntityManager)currentEntityManager());
    }

    public void testEagerConstantOneMany() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().addFetchGroup("nonPKs");
        constantOneMany(pm);
    }

    private void constantOneMany(OpenJPAEntityManager pm) {
        ConstantJoinPC2 constant2 =
            (ConstantJoinPC2) pm.getObjectId(_constant2);

        Collection nonpks = constant2.getNonPKs();
        assertEquals(1, nonpks.size());
        assertEquals(_nonpk, pm.getObjectId
            (nonpks.iterator().next()));
        pm.close();
    }
    
    
    
}
