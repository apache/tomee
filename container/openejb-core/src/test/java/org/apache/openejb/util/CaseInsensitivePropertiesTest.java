/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import junit.framework.TestCase;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class CaseInsensitivePropertiesTest extends TestCase {

    public void test() throws Exception {
        Properties p = new CaseInsensitiveProperties();

        assertEquals(0, p.size());

        p.setProperty("FoO", "true");

        assertEquals(1, p.size());

        p.setProperty("Foo", "false");

        // should still be size 1
        assertEquals(1, p.size());

        assertEquals("false", p.getProperty("FoO"));
        assertEquals("false", p.getProperty("Foo"));

        assertTrue(p.containsKey("Foo"));

    }
}
