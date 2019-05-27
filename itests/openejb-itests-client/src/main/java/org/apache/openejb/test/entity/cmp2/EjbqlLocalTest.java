/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.cmp2;

import org.apache.openejb.test.entity.ejbql.QueryDataHome;
import org.apache.openejb.test.entity.ejbql.QueryDataLocal;
import org.apache.openejb.test.entity.ejbql.QueryDataRemote;
import org.apache.openejb.test.entity.ejbql.QueryHome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@SuppressWarnings("UnusedDeclaration")
public class EjbqlLocalTest extends Cmp2TestClient {
    private QueryHome queryHome;

    public EjbqlLocalTest() {
        super("EJBQLTest.");
    }

    protected void setUp() throws Exception {
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/ejbql/Query");
        queryHome = (QueryHome) obj;
        obj = initialContext.lookup("client/tests/entity/ejbql/QueryData");
        final QueryDataHome queryDataHome = (QueryDataHome) obj;

        queryDataHome.create(0);
        queryDataHome.create(1);
        queryDataHome.create(2);
        queryDataHome.create(3);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Select a single string field
     */
    public void testSelectSingleStringField() throws Exception {
        final String result = queryHome.selectSingleStringField("2");
        assertEquals("2", result);
    }

    /**
     * Select a single boolean field
     */
    public void testSelectSingleBooleanField() throws Exception {
        final boolean result = queryHome.selectSingleBooleanField(true);
        assertEquals(true, result);
    }

    /**
     * Select a single char field
     */
    public void testSelectSingleCharField() throws Exception {
        final char result = queryHome.selectSingleCharField('2');
        assertEquals('2', result);
    }

    /**
     * Select a single byte field
     */
    public void testSelectSingleByteField() throws Exception {
        final byte result = queryHome.selectSingleByteField((byte) 2);
        assertEquals(2, result);
    }

    /**
     * Select a single short field
     */
    public void testSelectSingleShortField() throws Exception {
        final short result = queryHome.selectSingleShortField((short) 2);
        assertEquals(2, result);
    }

    /**
     * Select a single int field
     */
    public void testSelectSingleIntField() throws Exception {
        final int result = queryHome.selectSingleIntField(2);
        assertEquals(2, result);
    }

    /**
     * Select a single long field
     */
    public void testSelectSingleLongField() throws Exception {
        final long result = queryHome.selectSingleLongField(2);
        assertEquals(2, result);
    }

    /**
     * Select a single float field
     */
    public void testSelectSingleFloatField() throws Exception {
        final float result = queryHome.selectSingleFloatField(2);
        assertEquals((float) 2.0, result, 1e-15);
    }

    /**
     * Select a single double field
     */
    public void testSelectSingleDoubleField() throws Exception {
        final double result = queryHome.selectSingleDoubleField(2);
        assertEquals(2.0, result, 1e-15);
    }

    /**
     * Select a collection string field
     */
    public void testSelectCollectionStringField() throws Exception {
        final Collection result = queryHome.selectCollectionStringField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, "0", "1", "2", "3");
    }

    /**
     * Select a collection boolean field
     */
    public void testSelectCollectionBooleanField() throws Exception {
        final Collection result = queryHome.selectCollectionBooleanField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, true, false);
    }

    /**
     * Select a collection char field
     */
    public void testSelectCollectionCharField() throws Exception {
        final Collection result = queryHome.selectCollectionCharField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, '0', '1', '2', '3');
    }

    /**
     * Select a collection byte field
     */
    public void testSelectCollectionByteField() throws Exception {
        final Collection result = queryHome.selectCollectionByteField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, (byte) 0, (byte) 1, (byte) 2, (byte) 3);
    }

    /**
     * Select a collection short field
     */
    public void testSelectCollectionShortField() throws Exception {
        final Collection result = queryHome.selectCollectionShortField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, (short) 0, (short) 1, (short) 2, (short) 3);
    }

    /**
     * Select a collection int field
     */
    public void testSelectCollectionIntField() throws Exception {
        final Collection result = queryHome.selectCollectionIntField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, 0, 1, 2, 3);
    }

    /**
     * Select a collection long field
     */
    public void testSelectCollectionLongField() throws Exception {
        final Collection result = queryHome.selectCollectionLongField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, (long) 0, (long) 1, (long) 2, (long) 3);
    }

    /**
     * Select a collection float field
     */
    public void testSelectCollectionFloatField() throws Exception {
        final Collection result = queryHome.selectCollectionFloatField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, (float) 0, (float) 1, (float) 2, (float) 3);
    }

    /**
     * Select a collection double field
     */
    public void testSelectCollectionDoubleField() throws Exception {
        final Collection result = queryHome.selectCollectionDoubleField();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());
        assertCollection(result, 0.0, 1.0, 2.0, 3.0);
    }

    /**
     * Select a single local ejb
     */
    public void testSelectSingleLocalEjb() throws Exception {
        final Object result = queryHome.selectSingleLocalEjb(2);
        assertNotNull("result is null", result);
        assertTrue("result should be an instance of QueryDataLocal", result instanceof QueryDataLocal);
        final QueryDataLocal queryData = (QueryDataLocal) result;
        assertEquals(2, queryData.getIntField());
    }

    /**
     * Select a single remote ejb
     */
    public void testSelectSingleRemoteEjb() throws Exception {
        final Object result = queryHome.selectSingleRemoteEjb(2);
        assertNotNull("result is null", result);
        assertTrue("result should be an instance of QueryDataRemote", result instanceof QueryDataRemote);
        final QueryDataRemote queryData = (QueryDataRemote) result;
        assertEquals(2, queryData.getIntField());
    }

    /**
     * Select a collection local ejb
     */
    public void testSelectCollectionLocalEjb() throws Exception {
        final Collection result = queryHome.selectCollectionLocalEjb();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());

        final List<Integer> values = new ArrayList<Integer>();
        for (final Object object : result) {
            assertTrue("result item should be an instance of QueryDataLocal but is instance of " + Arrays.toString(object.getClass().getInterfaces()), object instanceof QueryDataLocal);
            final QueryDataLocal queryData = (QueryDataLocal) object;
            values.add(queryData.getIntField());
        }
        assertCollection(values, 0, 1, 2, 3);
    }

    /**
     * Select a collection remote ejb
     */
    public void testSelectCollectionRemoteEjb() throws Exception {
        final Collection result = queryHome.selectCollectionRemoteEjb();
        assertNotNull("result is null", result);
        assertEquals("result.size()", 4, result.size());

        final List<Integer> values = new ArrayList<Integer>();
        for (final Object object : result) {
            assertTrue("result item should be an instance of QueryDataRemote", object instanceof QueryDataRemote);
            final QueryDataRemote queryData = (QueryDataRemote) object;
            values.add(queryData.getIntField());
        }
        assertCollection(values, 0, 1, 2, 3);
    }

    @SuppressWarnings({"unchecked"})
    private static <E> void assertCollection(final Collection collection, final E... values) {
        assertEquals(new TreeSet(Arrays.asList(values)), new TreeSet(collection));
    }
}
