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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class URISupportTest extends TestCase {
    public void test() throws Exception {
        final URI absoluteA = new URI("/Users/dblevins/work/openejb3/container/openejb-jee/apple/");
        final URI absoluteB = new URI("/Users/dblevins/work/openejb3/container/openejb-core/foo.jar");

        final URI relativeB = URISupport.relativize(absoluteA, absoluteB);

        assertEquals("../../openejb-core/foo.jar", relativeB.toString());

        final URI resolvedB = absoluteA.resolve(relativeB);
        assertTrue(resolvedB.equals(absoluteB));
    }

    public void testFragment() throws Exception {
        final URI absoluteA = new URI("/Users/dblevins/work/openejb3/container/openejb-jee/apple/");
        final URI absoluteB = new URI("/Users/dblevins/work/openejb3/container/openejb-core/foo.jar#foo");

        final URI relativeB = URISupport.relativize(absoluteA, absoluteB);

        assertEquals("../../openejb-core/foo.jar#foo", relativeB.toString());

        final URI resolvedB = absoluteA.resolve(relativeB);
        assertTrue(resolvedB.equals(absoluteB));
    }

    public void testAddNewParameters() throws Exception {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("create", "false");

        final URI uri = URISupport.addParameters(URLs.uri("vm://broker"), parameters);
        assertEquals("vm://broker?create=false", uri.toString());
    }

    public void testDoNotReplaceAnExistingParameters() throws Exception {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("create", "false");

        final URI uri = URISupport.addParameters(URLs.uri("vm://broker?create=true"), parameters);
        assertEquals("vm://broker?create=true", uri.toString());
    }

    public void testAddToSetOfAlreadyExistingParameters() throws Exception {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("create", "false");

        final URI uri = URISupport.addParameters(URLs.uri("vm://broker?foo=bar&boo=baz&welcome=helloworld"), parameters);
        final Map<String, String> actual = URISupport.parseParamters(uri);

        assertEquals(4, actual.size());
        assertEquals("bar", actual.get("foo"));
        assertEquals("baz", actual.get("boo"));
        assertEquals("helloworld", actual.get("welcome"));
        assertEquals("false", actual.get("create"));
    }

    public void testAddToSetOfAlreadyExistingParametersButDontOverwriteExistingParameter() throws Exception {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("create", "false");

        final URI uri = URISupport.addParameters(URLs.uri("vm://broker?foo=bar&boo=baz&welcome=helloworld&create=true"), parameters);
        final Map<String, String> actual = URISupport.parseParamters(uri);

        assertEquals(4, actual.size());
        assertEquals("bar", actual.get("foo"));
        assertEquals("baz", actual.get("boo"));
        assertEquals("helloworld", actual.get("welcome"));
        assertEquals("true", actual.get("create"));
    }

    public void testNullParameters() throws Exception {
        final URI initial = URLs.uri("vm://broker?foo=bar&boo=baz&welcome=helloworld&create=true");
        final URI uri = URISupport.addParameters(initial, null);

        assertEquals(initial, uri);
    }

    public void testEmptyParameters() throws Exception {
        final URI initial = URLs.uri("vm://broker?foo=bar&boo=baz&welcome=helloworld&create=true");
        final URI uri = URISupport.addParameters(initial, Collections.emptyMap());

        assertEquals(initial, uri);
    }
}
