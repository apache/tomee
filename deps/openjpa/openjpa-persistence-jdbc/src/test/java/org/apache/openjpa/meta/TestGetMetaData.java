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
package org.apache.openjpa.meta;

import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestGetMetaData extends SingleEMFTestCase {

    public void setUp() {
        setUp(Item.class, Person.class, Artist.class, Painter.class,
            CLEAR_TABLES);
    }

    public void testGetMetaData() {
        assertNotNull(JPAFacadeHelper.getMetaData(emf, Item.class));
        assertNotNull(JPAFacadeHelper.getMetaData(emf, Person.class));
    }
}
