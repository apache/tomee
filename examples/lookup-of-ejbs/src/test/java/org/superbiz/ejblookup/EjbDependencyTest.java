/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.ejblookup;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;

//START SNIPPET: code
public class EjbDependencyTest extends TestCase {

    private Context context;

    protected void setUp() throws Exception {
        context = EJBContainer.createEJBContainer().getContext();
    }

    public void testRed() throws Exception {

        final Friend red = (Friend) context.lookup("java:global/lookup-of-ejbs/RedBean");

        assertNotNull(red);
        assertEquals("Red says, Hello!", red.sayHello());
        assertEquals("My friend Blue says, Hello!", red.helloFromFriend());

    }

    public void testBlue() throws Exception {

        final Friend blue = (Friend) context.lookup("java:global/lookup-of-ejbs/BlueBean");

        assertNotNull(blue);
        assertEquals("Blue says, Hello!", blue.sayHello());
        assertEquals("My friend Red says, Hello!", blue.helloFromFriend());

    }

}
//END SNIPPET: code
