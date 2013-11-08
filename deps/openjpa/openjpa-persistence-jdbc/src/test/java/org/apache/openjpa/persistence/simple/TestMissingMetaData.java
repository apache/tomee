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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;

// This test case extends TestCase directly instead of SingleEMTestCase with the
// corresponding setup() method because that scheme goes down a different code
// path and doesn't test the intended code change.
public class TestMissingMetaData extends TestCase {
    private OpenJPAEntityManagerFactory emf;

    public void setUp() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.RuntimeUnenhancedClasses", "supported");
        // This test case uses a different persistence xml file because
        // modifying the current persistence.xml file with a bad class would
        // cause the TestEnhancementWithMultiplePUs test case to fail.
        emf = OpenJPAPersistence.createEntityManagerFactory(
            "test-missing-metadata", "persistence2.xml", props);
    }
    
    public void testMissingMetaData() {
        String msg =
            "No registered metadata for type " +
            "\"class org.apache.openjpa.persistence.simple.Animal\".";
        try {
            emf.createEntityManager();
            fail("didn't receive expected ArgumentException - " + msg);
        } catch (Exception e) {
            assertEquals(ArgumentException.class,e.getClass());
            assertTrue("Unexpected Exception : " + e.getMessage(), e.getMessage().startsWith(msg));
        }
    }
    
    public void tearDown() {
        emf.close();
        emf = null;
    }
    
    public static void main(String[] args) {
        TestRunner.run(TestMissingMetaData.class);

    }

}
