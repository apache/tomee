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
package org.apache.openejb.config;

import java.util.Arrays;

import junit.framework.TestCase;

public class SunQueryFilterTest extends TestCase {
    private SunConversion sunConversion = new SunConversion();
    public void testTrivial() {
        String ejbQl = sunConversion.convertToEjbQl("item", null, null);
        assertEquals("SELECT OBJECT(o) FROM item AS o", ejbQl);
        ejbQl = sunConversion.convertToEjbQl("item", null, "true");
        assertEquals("SELECT OBJECT(o) FROM item AS o", ejbQl);
    }

    public void testParameterReplacement() {
        String ejbQl = sunConversion.convertToEjbQl("item", Arrays.asList("NAME"), "java.lang.String name", "NAME == name");
        assertEquals("SELECT OBJECT(o) FROM item AS o WHERE o.NAME = ?1", ejbQl);
    }

    public void testSymbolReplacement() {
        String ejbQl = sunConversion.convertToEjbQl("item", Arrays.asList("PRICE"), "float start, float end", "start <= PRICE && PRICE >= end");
        assertEquals("SELECT OBJECT(o) FROM item AS o WHERE ?1 <= o.PRICE and o.PRICE >= ?2", ejbQl);
    }

    public void testAllSymbolReplacement() {
        String ejbQl = sunConversion.convertToEjbQl("item", null, "&& || ! == !=");
        assertEquals("SELECT OBJECT(o) FROM item AS o WHERE and or not = <>", ejbQl);
    }

    public void testNoWhiteSpace() {
        String ejbQl = sunConversion.convertToEjbQl("item", Arrays.asList("PRICE"), "float start, float end", "start<=PRICE&&PRICE>=end");
        assertEquals("SELECT OBJECT(o) FROM item AS o WHERE ?1 <= o.PRICE and o.PRICE >= ?2", ejbQl);
    }
}
