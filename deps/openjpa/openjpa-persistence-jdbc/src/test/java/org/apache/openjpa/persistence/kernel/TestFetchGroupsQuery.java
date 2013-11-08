/*
 * TestFetchGroupsQuery.java
 *
 * Created on October 12, 2006, 10:09 AM
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
package org.apache.openjpa.persistence.kernel;

import java.util.List;



import org.apache.openjpa.persistence.kernel.common.apps.FetchGroupTestObject;
import org.apache.openjpa.persistence.kernel.common.apps.
        FetchGroupTestObjectChild;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestFetchGroupsQuery extends TestFetchGroups {

    /**
     * Creates a new instance of TestFetchGroupsQuery
     */
    public TestFetchGroupsQuery() {
    }

    public TestFetchGroupsQuery(String s) {
        super(s);
    }

    protected FetchGroupTestObject getO1(OpenJPAEntityManager pm) {
//        OpenJPAQuery q = pm.createNativeQuery("a == 5",
//                FetchGroupTestObject.class);
//        return (FetchGroupTestObject)
//        ((Collection) q.getResultList()).iterator().next();

        OpenJPAQuery q = pm.createQuery(
            "SELECT o FROM FetchGroupTestObject o WHERE o.a = 5");
        List l = q.getResultList();

        return (FetchGroupTestObject) (l.iterator().next());
    }

    protected FetchGroupTestObject getO2(OpenJPAEntityManager pm) {
//        OpenJPAQuery q = pm.createNativeQuery("a == 3",
//                FetchGroupTestObject.class);
//        return (FetchGroupTestObject)
//        ((Collection) q.getResultList()).iterator().next();

        OpenJPAQuery q = pm.createQuery(
            "SELECT o FROM FetchGroupTestObject o WHERE o.a = 3");
        List l = q.getResultList();

        return (FetchGroupTestObject) (l.iterator().next());
    }

    protected FetchGroupTestObjectChild getC1(OpenJPAEntityManager pm) {
//        OpenJPAQuery q = pm.createNativeQuery("a == 4",
//                FetchGroupTestObjectChild.class);
//        return (FetchGroupTestObjectChild)
//        ((Collection) q.getResultList()).iterator().next();

        OpenJPAQuery q = pm.createQuery(
            "SELECT o FROM FetchGroupTestObjectChild o WHERE o.a = 4");
        List l = q.getResultList();

        return (FetchGroupTestObjectChild) (l.iterator().next());
    }
}
