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

import java.net.URI;

/**
 * @version $Rev$ $Date$
 */
public class URISupportTest extends TestCase {
    public void test() throws Exception {
        URI absoluteA = new URI("/Users/dblevins/work/openejb3/container/openejb-jee/apple/");
        URI absoluteB = new URI("/Users/dblevins/work/openejb3/container/openejb-core/foo.jar");

        URI relativeB = URISupport.relativize(absoluteA, absoluteB);

        assertEquals("../../openejb-core/foo.jar", relativeB.toString());

        URI resolvedB = absoluteA.resolve(relativeB);
        assertTrue(resolvedB.equals(absoluteB));
    }

    public void testFragment() throws Exception {
        URI absoluteA = new URI("/Users/dblevins/work/openejb3/container/openejb-jee/apple/");
        URI absoluteB = new URI("/Users/dblevins/work/openejb3/container/openejb-core/foo.jar#foo");

        URI relativeB = URISupport.relativize(absoluteA, absoluteB);

        assertEquals("../../openejb-core/foo.jar#foo", relativeB.toString());

        URI resolvedB = absoluteA.resolve(relativeB);
        assertTrue(resolvedB.equals(absoluteB));
    }

}
