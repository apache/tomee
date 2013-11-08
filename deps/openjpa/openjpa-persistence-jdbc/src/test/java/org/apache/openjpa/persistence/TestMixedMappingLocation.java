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
package org.apache.openjpa.persistence;

import org.apache.openjpa.persistence.entity.MixedMappingLocation;
import org.apache.openjpa.persistence.entity.MixedMappingLocationEmbeddedId;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

// org.apache.openjpa.persistence.TestMixedMappingLocation
public class TestMixedMappingLocation extends SingleEMFTestCase {

    protected String getPersistenceUnitName() {
        return "xml-persistence-unit";
    }
    
    public void setUp() {
        setUp(DROP_TABLES,MixedMappingLocationEmbeddedId.class, MixedMappingLocation.class);
    }
    /**
     * Testcase for added OPENJPA859.
     * 
     * This scenario is testing whether the default annotations are being generated for a class that
     * isn't annotated with a persistence class type (ie: @Entity, @MappedSuperclass, @Embeddable),
     * but it is in a mapping file.
     * 
     * @throws Exception
     */
    public void testMixedOrmAnno() throws Exception {
        OpenJPAEntityManagerSPI  em = emf.createEntityManager();
        em.close();
    }
}
