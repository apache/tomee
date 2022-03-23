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
package org.superbiz.counter;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;

public class CounterTest extends TestCase {

    //START SNIPPET: local
    public void test() throws Exception {

        final Context context = EJBContainer.createEJBContainer().getContext();

        Counter counterA = (Counter) context.lookup("java:global/simple-stateful/Counter");

        assertEquals(0, counterA.count());
        assertEquals(0, counterA.reset());
        assertEquals(1, counterA.increment());
        assertEquals(2, counterA.increment());
        assertEquals(0, counterA.reset());

        counterA.increment();
        counterA.increment();
        counterA.increment();
        counterA.increment();

        assertEquals(4, counterA.count());

        // Get a new counter
        Counter counterB = (Counter) context.lookup("java:global/simple-stateful/Counter");

        // The new bean instance starts out at 0
        assertEquals(0, counterB.count());
    }
    //END SNIPPET: local

}
