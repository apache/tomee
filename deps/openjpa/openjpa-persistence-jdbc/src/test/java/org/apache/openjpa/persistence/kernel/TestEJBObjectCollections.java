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

import java.util.Arrays;
import java.util.HashSet;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.AllFieldsTypeTest;
import org.apache.openjpa.persistence.kernel.common.apps.ObjectCollectionHolder;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

/**
 * Tests that Collections of type {@link Object} can hold all
 * sorts of stuff (heterogeneous classes, persistent classes).
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
public class TestEJBObjectCollections extends AbstractTestCase {

    public TestEJBObjectCollections(String name) {
        super(name, "kernelcactusapp");
    }

    public void testHashSet() {
        assertEquals(1, add(new Object[]{ "Foo" }, 1).getHashSet().size());
        assertEquals(1, add(new Object[]{ "Foo" }, 2).getHashSet().size());

        assertEquals(2, add(
            new Object[]{ "Foo", new AllFieldsTypeTest() }, 3)
            .getHashSet().size());
    }

    public void setUp() {
        deleteAll(ObjectCollectionHolder.class);
        deleteAll(AllFieldsTypeTest.class);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("unchecked")
    public ObjectCollectionHolder add(Object[] objects, int id) {
        EntityManager pm = currentEntityManager();
        startTx(pm);
        ObjectCollectionHolder holder = new ObjectCollectionHolder();
        holder.setId(id);
        pm.persist(holder);
        holder.setHashSet(new HashSet(Arrays.asList(objects)));
        endTx(pm);
        endEm(pm);

        pm = currentEntityManager();
        startTx(pm);
        return (ObjectCollectionHolder) pm
            .find(ObjectCollectionHolder.class, id);
    }
}
