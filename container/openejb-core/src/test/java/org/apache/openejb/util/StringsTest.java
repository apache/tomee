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

import junit.framework.TestCase;

public class StringsTest extends TestCase {

    public void testForceSlash() {
        assertEquals("/", Strings.forceSlash(null));
        assertEquals("/", Strings.forceSlash(""));
        assertEquals("/", Strings.forceSlash("/"));
        assertEquals("/hello", Strings.forceSlash("/hello"));
        assertEquals("/test/", Strings.forceSlash("/test/"));
        assertEquals("/path/", Strings.forceSlash("path/"));
    }

    public void testSlashify() {

        // same as previous
        assertEquals("/", Strings.slashify(null));
        assertEquals("/", Strings.slashify(""));
        assertEquals("/", Strings.slashify("/"));
        assertEquals("/hello", Strings.slashify("/hello"));
        assertEquals("/test/", Strings.slashify("/test/"));
        assertEquals("/path/", Strings.slashify("path/"));

        assertEquals("/", Strings.slashify(null, null));
        assertEquals("/", Strings.slashify(null, ""));
        assertEquals("/", Strings.slashify(null, "/"));
        assertEquals("/hello", Strings.slashify(null, "/hello"));
        assertEquals("/test/", Strings.slashify(null, "/test/"));
        assertEquals("/path/", Strings.slashify(null, "path/"));

        assertEquals("/", Strings.slashify("", null));
        assertEquals("/", Strings.slashify("", ""));
        assertEquals("/", Strings.slashify("", "/"));
        assertEquals("/hello", Strings.slashify("", "/hello"));
        assertEquals("/test/", Strings.slashify("", "/test/"));
        assertEquals("/path/", Strings.slashify("", "path/"));

        assertEquals("/", Strings.slashify("/", null));
        assertEquals("/", Strings.slashify("/", ""));
        assertEquals("/", Strings.slashify("/", "/"));
        assertEquals("/hello", Strings.slashify("/", "/hello"));
        assertEquals("/test/", Strings.slashify("/", "/test/"));
        assertEquals("/path/", Strings.slashify("/", "path/"));

        assertEquals("/hello/", Strings.slashify("/hello", null));
        assertEquals("/hello/", Strings.slashify("/hello", ""));
        assertEquals("/hello/", Strings.slashify("/hello", "/"));
        assertEquals("/hello/hello", Strings.slashify("/hello", "/hello"));
        assertEquals("/hello/test/", Strings.slashify("/hello", "/test/"));
        assertEquals("/hello/path/", Strings.slashify("/hello", "path/"));

        assertEquals("/test/", Strings.slashify("/test/", null));
        assertEquals("/test/", Strings.slashify("/test/", ""));
        assertEquals("/test/", Strings.slashify("/test/", "/"));
        assertEquals("/test/hello", Strings.slashify("/test/", "/hello"));
        assertEquals("/test/test/", Strings.slashify("/test/", "/test/"));
        assertEquals("/test/path/", Strings.slashify("/test/", "path/"));

        assertEquals("/path/", Strings.slashify("path/", null));
        assertEquals("/path/", Strings.slashify("path/", ""));
        assertEquals("/path/", Strings.slashify("path/", "/"));
        assertEquals("/path/hello", Strings.slashify("path/", "/hello"));
        assertEquals("/path/test/", Strings.slashify("path/", "/test/"));
        assertEquals("/path/path/", Strings.slashify("path/", "path/"));

        // double slash in the middle of a part should not be altered
        assertEquals("/path/te//st/", Strings.slashify("path/", "/te//st/"));
    }
    
}
