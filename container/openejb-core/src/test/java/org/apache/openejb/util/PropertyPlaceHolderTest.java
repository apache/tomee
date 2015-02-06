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
}
