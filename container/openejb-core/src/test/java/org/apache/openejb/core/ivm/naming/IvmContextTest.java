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

import javax.naming.*;

/**
 * @version $Rev$ $Date$
 */
public class IvmContextTest extends TestCase {
    public void test() throws Exception {
        String str3 = "root/comp/env/rate/work/doc/lot";

        IvmContext context = new IvmContext();
        context.bind("root/comp/env/rate/work/doc/lot/pop", new Integer(1));
        context.bind("root/comp/env/rate/work/doc/lot/price", new Integer(2));
        context.bind("root/comp/env/rate/work/doc/lot/break/story", new Integer(3));

        Object o = context.lookup("comp/env/rate/work/doc/lot/pop");
        assertNotNull(o);
        assertTrue(o instanceof Integer);
        assertEquals(o, new Integer(1));

        context.unbind("root/comp/env/rate/work/doc/lot/pop");

        try {
            context.lookup("comp/env/rate/work/doc/lot/pop");
            fail("name should be unbound");
        } catch (javax.naming.NameNotFoundException e) {
            // pass
        }

    }
}
