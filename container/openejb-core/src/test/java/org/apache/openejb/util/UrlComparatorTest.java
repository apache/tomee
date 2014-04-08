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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public class UrlComparatorTest extends TestCase {

    public void test() throws Exception {
        ArrayList<URL> urls = new ArrayList<URL>();

        urls.add(new URL("file:///Users/lblack/four"));
        urls.add(new URL("file:///Users/jstuart/two"));
        urls.add(new URL("file:///Users/jstuart/one"));
        urls.add(new URL("file:///Users/scobert/three"));

        Collections.sort(urls, new UrlComparator(new URL("file:///Users/jstuart")));

        assertEquals(new URL("file:///Users/jstuart/two"), urls.get(0));

        Collections.sort(urls, new UrlComparator(new URL("file:///Users/jstuart/one")));

        assertEquals(new URL("file:///Users/jstuart/one"), urls.get(0));
    }
}
