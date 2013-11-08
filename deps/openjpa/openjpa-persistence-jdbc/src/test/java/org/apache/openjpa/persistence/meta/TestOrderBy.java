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
package org.apache.openjpa.persistence.meta;

import java.util.Arrays;
import java.util.List;


import org.apache.openjpa.persistence.meta.common.apps.OrderByPC;
import org.apache.openjpa.persistence.meta.common.apps.OrderByPCRel;
import org.apache.openjpa.persistence.meta.common.apps.OrderByPCRelSub;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

/**
 * <p>Test the <code>order-by</code> field extension.</p>
 *
 * @author Abe White
 */
public class TestOrderBy
    extends AbstractTestCase {

    public TestOrderBy(String test) {
        super(test, "metacactusapp");
    }

    public void setUp() {
        deleteAll(OrderByPCRel.class);
        deleteAll(OrderByPC.class);
    }

    public void testStringList() {
        stringListTest(false, false);
    }

    public void testEagerParallelStringList() {
        stringListTest(true, true);
    }

    public void testEagerJoinStringList() {
        stringListTest(true, false);
    }

    private void stringListTest(boolean eager, boolean parallel) {
        String[] strs = new String[]{
            "9", "0", "5", "1", "3", "7", "8", "2", "6", "4",
        };
        OrderByPC pc = new OrderByPC();
        pc.setId(1L);
        pc.getStringListAsc().addAll(Arrays.asList(strs));

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        if (eager)
            ((FetchPlan) pm.getFetchPlan()).addField(OrderByPC.class,
                "stringListAsc");
        if (parallel) {

            OpenJPAQuery q = pm.createQuery(
                "select a FROM " + OrderByPC.class.getSimpleName());
            List res = (List) q.getResultList();
            assertEquals(1, res.size());
            pc = (OrderByPC) res.get(0);
        } else
            pc = (OrderByPC) pm.find(OrderByPC.class, oid);
        List stringList = pc.getStringListAsc();
        for (int i = 0; i < 10; i++)
            assertEquals(String.valueOf(i), stringList.get(i));
        endEm(pm);
    }

    public void testIntArray() {
        int[] ints = new int[]{ 9, 0, 5, 1, 3, 7, 8, 2, 6, 4, };
        OrderByPC pc = new OrderByPC();
        pc.setId(1L);
        pc.setIntArrayDesc(ints);

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (OrderByPC) pm.find(OrderByPC.class, oid);
        int[] intArray = pc.getIntArrayDesc();
        for (int i = 0; i < 10; i++)
            assertEquals(9 - i, intArray[i]);
        endEm(pm);
    }

    public void testOrderByPK() {
        orderByPKTest(false, false, false);
    }

    public void testEagerJoinOrderByPK() {
        orderByPKTest(true, false, false);
    }

    public void testEagerParallelOrderByPK() {
        orderByPKTest(true, true, false);
    }

    public void testOneToManyOrderByPK() {
        orderByPKTest(false, false, true);
    }

    public void testEagerJoinOneToManyOrderByPK() {
        orderByPKTest(true, false, true);
    }

    public void testEagerParallelOneToManyOrderByPK() {
        orderByPKTest(true, true, true);
    }

    private void orderByPKTest(boolean eager, boolean parallel,
        boolean oneToMany) {
        long[] ids = new long[]{ 9, 0, 5, 1, 3, 7, 8, 2, 6, 4, };
        OrderByPC pc = new OrderByPC();
        pc.setId(1L);
        OrderByPC pc2 = new OrderByPC();
        pc2.setId(2L);
        for (int i = 0; i < ids.length; i++) {
            OrderByPCRel rel = (i % 2 == 0) ? new OrderByPCRel()
                : new OrderByPCRelSub();
            rel.setId(ids[i]);
            if (oneToMany) {
                pc.getOneToManyAsc().add(rel);
                rel.setToOne(pc);
            } else
                pc.getOrderByPKAsc().add(rel);

            if (parallel)
                pc2.getOrderByPKAsc().add(rel);
        }
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        if (eager)
            ((FetchPlan) pm.getFetchPlan()).addField(OrderByPC.class,
                "orderByPKAsc");
        if (parallel) {

            OpenJPAQuery q = pm.createQuery("select a FROM " +
                OrderByPC.class.getSimpleName() + " select order by id asc");
            pc = (OrderByPC) ((List) q.getResultList()).get(0);
            q.closeAll();
        } else
            pc = (OrderByPC) pm.find(OrderByPC.class, oid);

        List orderByPK = (oneToMany) ? pc.getOneToManyAsc()
            : pc.getOrderByPKAsc();
        for (int i = 0; i < 10; i++)
            assertEquals(i, ((OrderByPCRel) orderByPK.get(i)).getId());
        endEm(pm);
    }

    public void testOrderByRelatedField() {
        String[] strs = new String[]{
            "9", "0", "5", "1", "3", "7", "8", "2", "6", "4",
        };
        OrderByPC pc = new OrderByPC();
        pc.setId(1L);
        for (int i = 0; i < strs.length; i++) {
            OrderByPCRel rel = (i % 2 == 0) ? new OrderByPCRel()
                : new OrderByPCRelSub();
            rel.setId(i);
            rel.setString(strs[i]);
            pc.getOrderByStringDesc().add(rel);
        }
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        pc = (OrderByPC) pm.find(OrderByPC.class, oid);
        List orderByRelField = pc.getOrderByStringDesc();
        for (int i = 0; i < 10; i++)
            assertEquals(String.valueOf(9 - i), ((OrderByPCRel)
                orderByRelField.get(i)).getString());
        endEm(pm);
    }

    public void testOrderByPKAndRelatedField() {
        orderByPKAndRelatedFieldTest(false, false);
    }

    public void testEagerJoinOrderByPKAndRelatedField() {
        orderByPKAndRelatedFieldTest(true, false);
    }

    public void testEagerParallelOrderByPKAndRelatedField() {
        orderByPKAndRelatedFieldTest(true, true);
    }

    private void orderByPKAndRelatedFieldTest(boolean eager, boolean parallel) {
        OrderByPC pc = new OrderByPC();
        pc.setId(1L);
        OrderByPC pc2 = new OrderByPC();
        pc.setId(2L);

        OrderByPCRel rel1 = new OrderByPCRel();
        rel1.setId(1L);
        rel1.setString("1");
        OrderByPCRel rel2 = new OrderByPCRelSub();
        rel2.setId(2L);
        rel2.setString("1");
        OrderByPCRel rel3 = new OrderByPCRel();
        rel3.setId(3L);
        rel3.setString("2");
        OrderByPCRel rel4 = new OrderByPCRelSub();
        rel4.setId(4L);
        rel4.setString("2");
        pc.getOrderByStringAndPKDesc().addAll(Arrays.asList
            (new Object[]{ rel1, rel2, rel4, rel3, }));
        if (parallel)
            pc2.getOrderByStringAndPKDesc().addAll(Arrays.asList
                (new Object[]{ rel1, rel2, rel4, rel3, }));

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        pm.persist(pc2);
        endTx(pm);
        Object oid = pm.getObjectId(pc);
        endEm(pm);

        pm = (OpenJPAEntityManager) currentEntityManager();
        if (eager)
            ((FetchPlan) pm.getFetchPlan()).addField(OrderByPC.class,
                "orderByStringAndPKDesc");
        if (parallel) {
            OpenJPAQuery q = pm.createQuery("select a FROM " +
                OrderByPC.class.getSimpleName() + " select order by id asc");
            pc = (OrderByPC) ((List) q.getResultList()).get(0);
            q.closeAll();
        } else
            pc = (OrderByPC) pm.find(OrderByPC.class, oid);
        List multiOrder = pc.getOrderByStringAndPKDesc();
        for (int i = 0; i < 4; i++)
            assertEquals(4 - i, ((OrderByPCRel) multiOrder.get(i)).getId());
        endEm(pm);
    }
}
