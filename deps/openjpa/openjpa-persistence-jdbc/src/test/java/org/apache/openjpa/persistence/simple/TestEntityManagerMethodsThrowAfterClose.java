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
package org.apache.openjpa.persistence.simple;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

/**
 * Negative test case to verify that EntityManager throws required exceptions
 * after close.
 *
 * @author Craig Russell
 */
public class TestEntityManagerMethodsThrowAfterClose
    extends SingleEMTestCase {

    private AllFieldTypes aft = new AllFieldTypes();

    public void setUp() {
        setUp(AllFieldTypes.class);
        close();
    }

    public void testPersistAfterClose() {
        try {
            em.persist(aft);
            fail("Expected exception not thrown " +
                    "when calling em.persist " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testMergeAfterClose() {
        try {
            em.merge(aft);
            fail("Expected exception not thrown " +
                    "when calling em.merge " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testRemoveAfterClose() {
        try {
            em.remove(aft);
            fail("Expected exception not thrown " +
                    "when calling em.remove " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testFindAfterClose() {
        try {
            em.find(AllFieldTypes.class, Integer.valueOf(1));
            fail("Expected exception not thrown " +
                    "when calling em.find " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testGetReferenceAfterClose() {
        try {
            em.getReference(AllFieldTypes.class, Integer.valueOf(1));
            fail("Expected exception not thrown " +
                    "when calling em.getReference " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testFlushAfterClose() {
        try {
            em.flush();
            fail("Expected exception not thrown " +
                    "when calling em.flush " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testSetFlushModeAfterClose() {
        try {
            em.setFlushMode(FlushModeType.AUTO);
            fail("Expected exception not thrown " +
                    "when calling em.setFlushMode " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testGetFlushModeAfterClose() {
        try {
            em.getFlushMode();
            fail("Expected exception not thrown " +
                    "when calling em.getFlushMode " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testLockAfterClose() {
        try {
            em.lock(aft, LockModeType.WRITE);
            fail("Expected exception not thrown " +
                    "when calling em.lock " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testRefreshAfterClose() {
        try {
            em.refresh(aft);
            fail("Expected exception not thrown " +
                    "when calling em.refresh " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testClearAfterClose() {
        try {
            em.clear();
            fail("Expected exception not thrown " +
                    "when calling em.clear " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testContainsAfterClose() {
        try {
            em.contains(aft);
            fail("Expected exception not thrown " +
                    "when calling em.contains " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCreateQueryAfterClose() {
        try {
            em.createQuery("SELECT Object(aft) FROM AllFieldTypes aft");
            fail("Expected exception not thrown " +
                    "when calling em.createQuery " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCreateNamedQueryAfterClose() {
        try {
            em.createNamedQuery("NamedQuery");
            fail("Expected exception not thrown " +
                    "when calling em.createNamedQuery " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCreateNativeQueryAfterClose() {
        try {
            em.createNativeQuery("SELECT NOTHINK FROM NOBODYZ");
            fail("Expected exception not thrown " +
                    "when calling em.createNativeQuery " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCreateNativeQueryWithMappingAfterClose() {
        try {
            em.createNativeQuery("SELECT NOTHINK FROM NOBODYZ", 
                    AllFieldTypes.class);
            fail("Expected exception not thrown " +
                    "when calling em.createNativeQuery " +
                    "with Mapping after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testJoinTransactionAfterClose() {
        try {
            em.joinTransaction();
            fail("Expected exception not thrown " +
                    "when calling em.joinTransaction " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testGetDelegateAfterClose() {
        try {
            em.getDelegate();
            fail("Expected exception not thrown " +
                    "when calling em.getDelegate " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCloseAfterClose() {
        try {
            em.close();
            fail("Expected exception not thrown when calling em.close " +
                    "after calling em.close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public static void main(String[] args) {
        TestRunner.run(TestEntityManagerMethodsThrowAfterClose.class);
    }
}

