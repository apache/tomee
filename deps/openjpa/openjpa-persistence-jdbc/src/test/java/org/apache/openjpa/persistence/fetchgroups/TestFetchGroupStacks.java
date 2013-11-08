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
package org.apache.openjpa.persistence.fetchgroups;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestFetchGroupStacks extends SingleEMTestCase {

    public void setUp() {
        setUp(FGManager.class, FGDepartment.class, FGEmployee.class,
            FGAddress.class);
    }

    public void testFetchGroupStacks() {
        assertFetchGroups();
        em.getFetchPlan().addFetchGroup("foo");
        assertFetchGroups("foo");
        
        { // add one new fetch group
            em.pushFetchPlan().addFetchGroup("bar"); // push 1
            assertFetchGroups("foo", "bar");

            { // add another one
                em.pushFetchPlan().addFetchGroup("baz"); // push 2
                assertFetchGroups("foo", "bar", "baz");

                { // add a fourth, plus one that's already there
                    em.pushFetchPlan().addFetchGroups("quux", "foo"); // push 3
                    assertFetchGroups("foo", "bar", "baz", "quux");
                    em.popFetchPlan(); // pop 3
                }

                // "foo" should still be there, since it was there before pop 3
                assertFetchGroups("foo", "bar", "baz");
                em.popFetchPlan(); // pop 2
            }

            assertFetchGroups("foo", "bar");
            em.popFetchPlan(); // pop 1
        }
        assertFetchGroups("foo");

        try {
            em.popFetchPlan();
            fail("should be unbalanced");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("stack"));
        }
    }

    private void assertFetchGroups(String... fgs) {
        Set s = new HashSet();
        if (fgs != null)
            s.addAll(Arrays.asList(fgs));
        s.add("default");
        assertEquals(s, em.getFetchPlan().getFetchGroups());
    }
}
