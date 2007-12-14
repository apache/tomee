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
package org.apache.openejb.assembler.classic;

import java.net.URI;

import junit.framework.TestCase;
import org.apache.openejb.util.LinkResolver;

public class LinkResolverTest extends TestCase {

    public void test() throws Exception {
        LinkResolver<Thing> resolver = new LinkResolver<Thing>();

        resolver.add("my/module.jar", "one", Thing.ONE);
        resolver.add("some/other.jar", "two", Thing.TWO);

        URI moduleUri = URI.create("my/module.jar");

        assertEquals(Thing.ONE, resolver.resolveLink("one", moduleUri));
        assertEquals(Thing.ONE, resolver.resolveLink("module.jar#one", moduleUri));
        assertEquals(Thing.ONE, resolver.resolveLink("../my/module.jar#one", moduleUri));
        assertEquals(Thing.ONE, resolver.resolveLink("../my/./module.jar#one", moduleUri));

        assertEquals(Thing.TWO, resolver.resolveLink("two", moduleUri));
        assertEquals(Thing.TWO, resolver.resolveLink("../some/./other.jar#two", moduleUri));
    }

    private static enum Thing {
        ONE, TWO
    }
}
