/*
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
package org.apache.openejb.client;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class RandomConnectionStrategyIteratorTest {


    @Test
    public void test() throws Exception {
        final URI[] uris = uris(
                "one://localhost:1243",
                "two://localhost:1243",
                "three://localhost:1243",
                "four://localhost:1243");

        ClusterMetaData cluster = new ClusterMetaData(System.currentTimeMillis(), uris);
        Iterable<URI> iterable = new RandomConnectionStrategy().createIterable(cluster);


        List<URI> found = new ArrayList<URI>();

        for (URI uri : iterable) {
            found.add(uri);
        }

        Assert.assertEquals(uris.length, found.size());
        for (URI uri : uris) {
            Assert.assertTrue(found.contains(uri));
        }

        Assert.assertFalse(
                found.get(0).equals(uris[0]) &&
                found.get(1).equals(uris[1]) &&
                found.get(2).equals(uris[2]) &&
                found.get(3).equals(uris[3])
        );
    }

    private URI[] uris(String... strings) {
        final URI[] uris = new URI[strings.length];

        for (int i = 0; i < strings.length; i++) {
            uris[i] = URI.create(strings[i]);
        }

        return uris;
    }
}
