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
package org.apache.openjpa.persistence.query;

import java.util.Collections;
import java.util.Map;

import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.rop.ListResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

/**
 * Tests MethodQL.
 * 
 * The 'user method' is simply echos the parameters set on the query.
 *  
 * @author Pinaki Poddar
 *
 */
public class TestMethodQLQuery
    extends SingleEMTestCase {

    @Override
    public void setUp() {
        setUp(SimpleEntity.class);
    }

    OpenJPAQuery createMethodQuery(String method) {
        String methodName = getClass().getName()+ "." + method;
        return em.createQuery(QueryLanguages.LANG_METHODQL, methodName);
    }
    
    public void testMethodQLWithParameters() {
        OpenJPAQuery q = createMethodQuery("echo");
        Query kernelQ = q.unwrap(Query.class);
        kernelQ.declareParameters("String firstName, String lastName");
        q.setParameter("firstName", "Fred").setParameter("lastName", "Lucas");
        Object result = q.getResultList().get(0);
        assertTrue(result instanceof Map);
        Map params = (Map)result;
        assertEquals("Fred", params.get("firstName"));
        assertEquals("Lucas", params.get("lastName"));
    }

    public void testMethodQLWithoutParametersDeclared() {
        OpenJPAQuery q = createMethodQuery("echo");
        Object result = q.getResultList().get(0);
        assertTrue(result instanceof Map);
        assertTrue(((Map)result).isEmpty());
    }

    public void testInvalidMethodReturnType() {
        OpenJPAQuery q = createMethodQuery("invalidReturnMeth");
        try {
            q.getResultList().get(0);
            fail("should have gotten an exception since method is invalid");
        } catch (ArgumentException ae) {
            // expected
        }
    }

    public void testVoidMethodReturnType() {
        OpenJPAQuery q = createMethodQuery("voidMeth");
        try {
            q.getResultList().get(0);
            fail("should have gotten an exception since method is invalid");
        } catch (ArgumentException ae) {
            // expected
        }
    }
    
    /**
     * Returns the list whose element is the Map of input parameters.
     * @return
     */
    public static ResultObjectProvider echo(StoreContext ctx,
        ClassMetaData meta, boolean subs, Map params, FetchConfiguration conf) {
        return new ListResultObjectProvider(Collections.singletonList(params));
    }

    public static void voidMeth(StoreContext ctx,
        ClassMetaData meta, boolean subs, Map params, FetchConfiguration conf) {
    }

    public static Object invalidReturnMeth(StoreContext ctx,
        ClassMetaData meta, boolean subs, Map params, FetchConfiguration conf) {
        return null;
    }
}
