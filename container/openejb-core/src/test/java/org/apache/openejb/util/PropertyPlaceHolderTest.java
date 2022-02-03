/**
 *
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

import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertyPlaceHolderTest {
    @Before
    @After
    public void reset() {
        SystemInstance.get().getProperties().clear();
    }

    @Test
    public void cipher() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest", "cipher:Static3DES:xMH5uM1V9vQzVUv5LG7YLA==");
        assertEquals("Passw0rd", PropertyPlaceHolderHelper.simpleValue("${PropertyPlaceHolderTest}"));
        assertEquals("Passw0rd", PropertyPlaceHolderHelper.simpleValue("cipher:Static3DES:xMH5uM1V9vQzVUv5LG7YLA=="));
    }

    @Test
    public void tomee1509() {
        final String expected = "shuttt don't tell!";
        final char[] encoded = new ReversePasswordCipher().encrypt(expected);
        assertEquals(expected, PropertyPlaceHolderHelper.simpleValue("cipher:reverse:" + new String(encoded)));
        assertEquals(expected, PropertyPlaceHolderHelper.simpleValue("cipher:" + ReversePasswordCipher.class.getName() + ":" + new String(encoded)));
    }

    @Test
    public void simpleReplace() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest", "ok");

        final String foo = PropertyPlaceHolderHelper.simpleValue("${PropertyPlaceHolderTest}");
        assertEquals("ok", foo);
    }

    @Test
    public void defaults() {
        SystemInstance.get().setProperty("last", "e");
        SystemInstance.get().setProperty("end", "real-end");
        SystemInstance.get().setProperty("real-end", "!");
        assertEquals("bah", PropertyPlaceHolderHelper.simpleValue("${PropertyPlaceHolderTest:-bah}"));
        assertEquals("tomee!", PropertyPlaceHolderHelper.simpleValue("${not here sorry:-to}${no more luck:-me}${last:-missed}${${end}}"));
    }

    @Test
    public void noValueFound() {
        assertEquals("v", PropertyPlaceHolderHelper.simpleValue("${v}"));
    }

    @Test
    public void composedReplace() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest1", "uno");
        SystemInstance.get().setProperty("PropertyPlaceHolderTest2", "due");

        final String foo = PropertyPlaceHolderHelper.simpleValue("jdbc://${PropertyPlaceHolderTest1}/${PropertyPlaceHolderTest2}");
        assertEquals("jdbc://uno/due", foo);
    }

    /*
     * Start of tests related to TOMEE-2968
     */

    @Test
    public void singleCurlyBrace() {
        final String foo = PropertyPlaceHolderHelper.simpleValue("tiger...}");
        assertEquals("tiger...}", foo);
    }

    @Test
    public void singleCurlyBraceAsStringOrCharArray() {
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("tiger...}");
        assertEquals("tiger...}", foo);
    }

    @Test
    public void singleCurlyBraceWithSubstitution() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest1", "tiger...}");
        SystemInstance.get().setProperty("PropertyPlaceHolderTest2", "due");
        final String foo = PropertyPlaceHolderHelper.simpleValue("${PropertyPlaceHolderTest1}/${PropertyPlaceHolderTest2}");
        assertEquals("tiger...}/due", foo);
    }

    @Test
    public void singleCurlyBraceAsStringOrCharArrayWithSubstitution() {
        SystemInstance.get().setProperty("PropertyPlaceHolderTest1", "tiger...}");
        SystemInstance.get().setProperty("PropertyPlaceHolderTest2", "due");
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${PropertyPlaceHolderTest1}/${PropertyPlaceHolderTest2}");
        assertEquals("tiger...}/due", foo);
    }

    @Test
    public void singleCurlyBraceAfterVariableReplacementGroup() {
        SystemInstance.get().setProperty("foo", "bar");
        final String foo = PropertyPlaceHolderHelper.simpleValue("${foo}}");
        assertEquals("bar}", foo);
    }

    @Test
    public void singleCurlyBraceAsStringOrCharArrayAfterVariableReplacementGroup() {
        SystemInstance.get().setProperty("foo", "bar");
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${foo}}");
        assertEquals("bar}", foo);
    }

    @Test
    public void escapeCharacterSubstitutionSkip() {
        SystemInstance.get().setProperty("{foo}", "bar");
        //$ is treated as an escape character, thus skipping substitution of variable with key = '{foo}'.
        final String foo = PropertyPlaceHolderHelper.simpleValue("$${{foo}}");
        assertEquals("${{foo}}", foo);
    }

    @Test
    public void escapeCharacterSubstitutionSkipAsStringOrCharArray() {
        SystemInstance.get().setProperty("{foo}", "bar");
        //$ is treated as an escape character, thus skipping substitution of variable with key = '{foo}'.
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("$${{foo}}");
        assertEquals("${{foo}}", foo);
    }

    @Test
    public void nestingSubstitution() {
        SystemInstance.get().setProperty("foo", "abc}");
        SystemInstance.get().setProperty("abc}", "foo");
        final String foo = PropertyPlaceHolderHelper.simpleValue("${${foo}}");
        assertEquals("foo", foo);
    }

    @Test
    public void nestingSubstitutionAsStringOrCharArray() {
        SystemInstance.get().setProperty("foo", "abc}");
        SystemInstance.get().setProperty("abc}", "foo");
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${${foo}}");
        assertEquals("foo", foo);
    }

    @Test
    public void escapedNestingWithNonExistentKey() {
        SystemInstance.get().setProperty("bar", "bar");
        //'$${foo}' is substituted to '${foo}', which does not exist. `${foo}` is thus substituted to its key `foo`.
        final String foo = PropertyPlaceHolderHelper.simpleValue("${$${foo}}");
        assertEquals("foo", foo);
    }

    @Test
    public void escapedNestingWithNonExistentKeyAsStringOrCharArray() {
        SystemInstance.get().setProperty("bar", "bar");
        //'$${foo}' is substituted to '${foo}', which does not exist. `${foo}` is thus substituted to its key `foo`.
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${$${foo}}");
        assertEquals("foo", foo);
    }

    @Test
    public void combinedNestingWithNonExistentKey() {
        SystemInstance.get().setProperty("foo", "bar");
        //variable for key 'bar' does not exist
        final String foo = PropertyPlaceHolderHelper.simpleValue("${foo}-${${bar}}");
        assertEquals("bar-bar", foo);
    }

    @Test
    public void combinedNestingWithNonExistentKeyAsStringOrCharArray() {
        SystemInstance.get().setProperty("foo", "bar");
        //variable for key 'bar' does not exist
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${foo}-${${bar}}");
        assertEquals("bar-bar", foo);
    }

    @Test
    public void nestedMultipleReplacementsWithClosingCurlyBraces() {
        SystemInstance.get().setProperty("foo", "abc}");
        SystemInstance.get().setProperty("abc}", "food");
        SystemInstance.get().setProperty("bar", "yammie");
        final String foo = PropertyPlaceHolderHelper.simpleValue("${bar}/${${foo}}");
        assertEquals("yammie/food", foo);
    }

    @Test
    public void nestedMultipleReplacementsWithClosingCurlyBracesAsStringOrCharArray() {
        SystemInstance.get().setProperty("foo", "abc}");
        SystemInstance.get().setProperty("abc}", "food");
        SystemInstance.get().setProperty("bar", "yammie");
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${bar}/${${foo}}");
        assertEquals("yammie/food", foo);
    }

    @Test
    public void nestedMissingPropertiesKeyAsStringOrCharArray() {
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${${foo}.bar}");
        assertEquals("foo.bar", foo);
    }

    @Test
    public void nestedMissingPropertiesKey() {
        final String foo = PropertyPlaceHolderHelper.simpleValue("${${foo}.bar}");
        assertEquals("foo.bar", foo);
    }

    @Test
    public void nestedPropertiesKeyAsStringOrCharArray() {
        SystemInstance.get().setProperty("foo", "bar");
        SystemInstance.get().setProperty("bar.bar", "val");
        final Object foo = PropertyPlaceHolderHelper.simpleValueAsStringOrCharArray("${${foo}.bar}");
        assertEquals("val", foo);
    }

    @Test
    public void nestedPropertiesKey() {
        SystemInstance.get().setProperty("foo", "bar");
        SystemInstance.get().setProperty("bar.bar", "val");
        final String foo = PropertyPlaceHolderHelper.simpleValue("${${foo}.bar}");
        assertEquals("val", foo);
    }

    @Test
    public void escapedPropertyKey() {
        SystemInstance.get().setProperty("foo", "bar");
        SystemInstance.get().setProperty("bar", "val");
        final String foo = PropertyPlaceHolderHelper.simpleValue("$${foo}.${bar}");
        assertEquals("${foo}.val", foo);
    }

    @Test
    public void escapedPropertyKeyAsStringOrCharArray() {
        SystemInstance.get().setProperty("foo", "bar");
        SystemInstance.get().setProperty("bar", "val");
        final Object foo = PropertyPlaceHolderHelper.simpleValue("$${foo}.${bar}");
        assertEquals("${foo}.val", foo);
    }

    @Test
    public void escapedPropertyKeyUnknownAndKnownVar() {
        SystemInstance.get().setProperty("foo", "bar");
        SystemInstance.get().setProperty("known", "bar");
        final String foo = PropertyPlaceHolderHelper.simpleValue("${bar}.$${foo}.${known}");
        assertEquals("bar.${foo}.bar", foo);
    }

    @Test
    public void escapedPropertyKeyUnknownAndKnownVarAsStringOrCharArray() {
        SystemInstance.get().setProperty("foo", "bar");
        SystemInstance.get().setProperty("known", "bar");
        final Object foo = PropertyPlaceHolderHelper.simpleValue("${bar}.$${foo}.${known}");
        assertEquals("bar.${foo}.bar", foo);
    }

}
