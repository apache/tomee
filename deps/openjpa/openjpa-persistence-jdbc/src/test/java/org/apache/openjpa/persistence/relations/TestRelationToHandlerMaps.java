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
package org.apache.openjpa.persistence.relations;

import java.net.MalformedURLException;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.strats.RelationHandlerMapTableFieldStrategy;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestRelationToHandlerMaps
    extends SingleEMTestCase {

    public void setUp() {
        setUp(RelationToHandlerMapInstance.class, AllFieldTypes.class,
            CLEAR_TABLES);
    }

    public void testRelationToHandlerMaps() throws MalformedURLException {
        ClassMapping cm = (ClassMapping) JPAFacadeHelper.getMetaData(em,
            RelationToHandlerMapInstance.class);
        assertEquals(RelationHandlerMapTableFieldStrategy.class,
            cm.getFieldMapping("aftMap").getStrategy().getClass());

        RelationToHandlerMapInstance o = new RelationToHandlerMapInstance();
        AllFieldTypes key = new AllFieldTypes();
        o.getMap().put(key, "foo");
        em.getTransaction().begin();
        em.persist(o);
        em.getTransaction().commit();
        em.close();
    }
}
