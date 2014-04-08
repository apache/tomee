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
package org.apache.openejb.core.ivm.naming;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ParsedNameTest extends TestCase {

    public void testRemaining() {
        ParsedName name = new ParsedName("one/two/three/four");

        assertEquals("Name.getComponent()", "one", name.getComponent());
        assertEquals("Name.remaining().path()", "two/three/four", name.remaining().path());

        name.next();

        assertEquals("Name.getComponent()", "two", name.getComponent());
        assertEquals("Name.remaining().path()", "three/four", name.remaining().path());

        name.next();

        assertEquals("Name.getComponent()", "three", name.getComponent());
        assertEquals("Name.remaining().path()", "four", name.remaining().path());

        name.next();

        assertEquals("Name.getComponent()", "four", name.getComponent());
        assertEquals("Name.remaining().path()", "", name.remaining().path());

        name.next();

        assertEquals("Name.remaining().path()", "", name.remaining().path());

    }
}
