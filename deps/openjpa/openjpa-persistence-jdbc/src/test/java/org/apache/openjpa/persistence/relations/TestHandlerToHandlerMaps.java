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
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.HandlerHandlerMapTableFieldStrategy;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestHandlerToHandlerMaps
    extends SingleEMTestCase {

    public void setUp() {
        setUp(HandlerToHandlerMapInstance.class,
            CLEAR_TABLES);
    }

    public void testHandlerToHandlerMaps() {
        ClassMapping cm = (ClassMapping) JPAFacadeHelper.getMetaData(em,
            HandlerToHandlerMapInstance.class);
        FieldMapping fm = cm.getFieldMapping("map");
        assertEquals(HandlerHandlerMapTableFieldStrategy.class,
            fm.getStrategy().getClass());
        assertEquals("NONSTD_MAPPING_MAP", fm.getTable().getName());

        HandlerToHandlerMapInstance o = new HandlerToHandlerMapInstance();
        o.getMap().put("foo", "bar");
        em.getTransaction().begin();
        em.persist(o);
        em.getTransaction().commit();
        em.close();
    }
}
