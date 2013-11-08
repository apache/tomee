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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.strats.
        RelationRelationMapTableFieldStrategy;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestRelationToRelationMaps
    extends SingleEMTestCase {

    public void setUp() {
        setUp(RelationToRelationMapInstance.class, AllFieldTypes.class,
            CLEAR_TABLES);
    }

    public void testRelationToRelationMaps() {
        ClassMapping cm = (ClassMapping) JPAFacadeHelper.getMetaData(em,
            RelationToRelationMapInstance.class);
        assertEquals(RelationRelationMapTableFieldStrategy.class,
            cm.getFieldMapping("map").getStrategy().getClass());

        RelationToRelationMapInstance o = new RelationToRelationMapInstance();
        AllFieldTypes key = new AllFieldTypes();
        AllFieldTypes val = new AllFieldTypes();
        o.getMap().put(key, val);
        em.getTransaction().begin();
        em.persist(o);
        em.getTransaction().commit();
        em.close();
    }
}
