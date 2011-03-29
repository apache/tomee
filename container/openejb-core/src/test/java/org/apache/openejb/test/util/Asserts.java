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
package org.apache.openejb.test.util;

import junit.framework.Assert;

import java.util.Iterator;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Asserts {
    public static void assertEquals(Iterable<?> expectedList, Iterable<?> actualList) {
        final Iterator<?> expected = expectedList.iterator();
        final Iterator<?> actual = actualList.iterator();

        while (expected.hasNext() && actual.hasNext()) {
            Assert.assertEquals(expected.next(), actual.next());
        }

        Assert.assertEquals(expected.hasNext(), actual.hasNext());
    }

    public static void assertEquals(Map<?, ?> expectedMap, Map<?, ?> actualMap) {
        final Iterator<? extends Map.Entry<?, ?>> expectedIt = expectedMap.entrySet().iterator();
        final Iterator<? extends Map.Entry<?, ?>> actualIt = actualMap.entrySet().iterator();

        while (expectedIt.hasNext() && actualIt.hasNext()) {
            final Map.Entry<?, ?> expected = expectedIt.next();
            final Map.Entry<?, ?> actual = actualIt.next();
            Assert.assertEquals("key", expected.getKey(), actual.getKey());
            Assert.assertEquals(expected.getKey().toString(), expected.getValue(), actual.getValue());
        }

        Assert.assertEquals(expectedIt.hasNext(), actualIt.hasNext());
    }

}
