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
package org.apache.openjpa.lib.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test UUID generation.
 *
 * @author Abe White
 */
public class TestUUIDGenerator extends TestCase {

    public void testUniqueString() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++)
            assertTrue(seen.add(
                UUIDGenerator.nextString(UUIDGenerator.TYPE1)));
    }

    public void testUniqueHex() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++)
            assertTrue(seen.add(
                UUIDGenerator.nextHex(UUIDGenerator.TYPE1)));
    }

    public void testUniqueType4String() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++)
            assertTrue(seen.add(
                UUIDGenerator.nextString(UUIDGenerator.TYPE4)));
    }

    public void testUniqueType4Hex() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++)
            assertTrue(seen.add(
                UUIDGenerator.nextHex(UUIDGenerator.TYPE4)));
    }

    public void testUniqueMixedTypesHex() {
        Set seen = new HashSet();
        for (int i = 0; i < 10000; i++) {
            int type = (i % 2 == 0) ? 
                UUIDGenerator.TYPE4 : UUIDGenerator.TYPE1;
            assertTrue(seen.add(
                UUIDGenerator.nextHex(type)));
        }
    }

    public void testGetTime() {
        long time = 0;
        for (int i = 0; i < 10000; i++) {
            long newTime = UUIDGenerator.getTime();
            assertTrue(newTime != time);
            time = newTime;
        }
    }
    
    public void testInitType1MultiThreaded() throws Exception {
        // This test method depends IP and RANDOM in UUIDGenerator to be null
        // and type1Initialized to be false. Using reflection to ensure that
        // those fields are null. Wrap this  method in doPrivledgedAction so it
        // doesn't fail when running with security.
        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                Class uuid = UUIDGenerator.class;
                Field[] fields = uuid.getDeclaredFields();
                for (Field f : fields) {
                    if (f.getName().equals("type1Initialized")) {
                        f.setAccessible(true);
                        f.set(null, false);
                    } else if (f.getName().equals("IP") || f.getName().equals("RANDOM")) {
                        f.setAccessible(true);
                        f.set(null, null);
                    }
                }
                Thread t = new Thread() {
                    public void run() {
                        UUIDGenerator.createType1();
                    }
                };

                t.start();
                UUIDGenerator.createType1();
                return null;
            }
        });
    }// end testInitType1MultiThreaded
}
