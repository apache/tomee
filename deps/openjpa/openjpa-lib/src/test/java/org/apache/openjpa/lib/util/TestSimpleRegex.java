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

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests simple regex for use in in-memory query execution.
 *
 * @author Greg Campbell
 */
public class TestSimpleRegex extends TestCase {

    private boolean matchExpr(String target, String expr, boolean caseInsens) {
        SimpleRegex re = new SimpleRegex(expr, caseInsens);
        return re.matches(target);
    }

    public void testWildcards() {
        assertTrue(matchExpr("Hello", "Hello", false));
        assertFalse(matchExpr("Hello", "Bye", false));
        assertFalse(matchExpr("Hello", "ByeBye", false));
        assertFalse(matchExpr("Hello", "Hellooo", false));
        assertFalse(matchExpr("Hello", "HHello", false));
        assertTrue(matchExpr("Hello", "H.llo", false));
        assertTrue(matchExpr("Hello", "Hell.*", false));
        assertTrue(matchExpr("Yo Hello", ".*ello", false));
        assertTrue(matchExpr("Hello", ".*ello", false));
        assertTrue(matchExpr("Hello", ".*ell.*", false));
        assertTrue(matchExpr("Hellow", ".*ell.*", false));
        assertTrue(matchExpr("Hello", "Hel.*lo", false));
        assertTrue(matchExpr("HelYolo", "Hel.*lo", false));
        assertTrue(matchExpr("Hello", "H.*lo", false));
        assertFalse(matchExpr("Hellowe", "H.*lo", false));
        assertTrue(matchExpr("Hello", "h.*lo", true));
        assertFalse(matchExpr("Hello", "h.*lo", false));
        assertTrue(matchExpr("The quick brown fox jumped over the lazy dog",
            "The .*brown.*dog", false));
        assertTrue(matchExpr("The quick brown fox jumped over the lazy dog",
            "The .*br.wn.*d.g", false));
        assertTrue(matchExpr("the quick BRown fox jumped over the lazy dog",
            "The .*br.wn.*d.g", true));
        assertFalse(matchExpr("The quick brown fox jumped over the lazy dog",
            "The .*brown.*dogg", false));
        assertFalse(matchExpr("The quick brown fox jumped over the lazy dog",
            "TThe .*brown.*dogg", false));

        assertFalse(matchExpr("Yo Hellow", ".*ello", false));
        assertFalse(matchExpr("Hellow", ".*YoHello", false));
    }

    public static void main(String[] args) {
        TestRunner.run(TestSimpleRegex.class);
    }
}
