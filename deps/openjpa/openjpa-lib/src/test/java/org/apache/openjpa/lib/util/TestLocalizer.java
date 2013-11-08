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

import java.util.Locale;
import java.util.MissingResourceException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.lib.util.testlocalizer.LocalizerTestHelper;

/**
 * Tests the Localizer.
 *
 * @author Abe White
 */
public class TestLocalizer extends TestCase {

    private Localizer _locals = null;

    public TestLocalizer(String test) {
        super(test);
    }

    public void setUp() {
        _locals = Localizer.forPackage(LocalizerTestHelper.class);
    }

    /**
     * Test getting a string for a class.
     */
    public void testForClass() {
        assertEquals(Locale.getDefault().equals(Locale.GERMANY)
            ? "value1_de" : "value1", _locals.get("test.local1"));
    }

    /**
     * Test getting a string for a non-default locale.
     */
    public void testForLocale() {
        Localizer locl = Localizer.forPackage(LocalizerTestHelper.class,
            Locale.GERMANY);
        assertEquals("value1_de", locl.get("test.local1"));
    }

    /**
     * Tests that if a locale is missing the system falls back to the default.
     */
    public void testFallbackLocale() {
        Localizer locl = Localizer.forPackage(LocalizerTestHelper.class,
            Locale.FRANCE);
        assertEquals(Locale.getDefault().equals(Locale.GERMANY)
            ? "value1_de" : "value1", locl.get("test.local1"));
    }

    /**
     * Tests that a null class accesses the localizer.properties file
     * for the top-level(no package).
     */
    public void testTopLevel() {
        Localizer system = Localizer.forPackage(null);
        assertEquals("systemvalue1", system.get("test.systemlocal"));
    }

    /**
     * Test that the message formatting works correctly.
     */
    public void testMessageFormat() {
        String suffix = Locale.getDefault().equals(Locale.GERMANY) ? "_de" : "";

        assertEquals("value2" + suffix + " x sep y", _locals.get("test.local2",
            new String[]{ "x", "y" }));

        // test that it treates single objects as single-element arrays
        assertEquals("value2" + suffix + " x sep {1}",
            _locals.get("test.local2", "x"));
    }

    /**
     * Test that a {@link MissingResourceException} is thrown for missing
     * resource bundles.
     */
    public void testMissingBundle() {
        Localizer missing = Localizer.forPackage(String.class);
        assertEquals("localized message key: foo.bar", missing.get("foo.bar"));
        assertEquals("localized message key: foo.bar; substitutions: [baz, 1]", missing.get("foo.bar", "baz", 1));
        try {
            missing.getFatal("foo.bar");
            fail("No exception for fatal get on missing bundle.");
        } catch (MissingResourceException mre) {
        }
    }

    /**
     * Test that a {@link MissingResourceException} is thrown for missing keys.
     */
    public void testMissingKey() {
        assertEquals("localized message key: foo.bar", _locals.get("foo.bar"));
        assertEquals("localized message key: foo.bar; substitutions: [baz, 1]", _locals.get("foo.bar", "baz", 1));
        try {
            _locals.getFatal("foo.bar");
            fail("No exception for fatal get on missing key.");
        } catch (MissingResourceException mre) {
        }
    }
    
    public static void assertEquals(String s, Message m) {
        assertEquals(s, m.getMessage());
    }

    public static Test suite() {
        return new TestSuite(TestLocalizer.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
