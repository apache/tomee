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
package org.superbiz.accesstimeout;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
public class BusyBeeTest extends TestCase {

    public void test() throws Exception {

        final Context context = EJBContainer.createEJBContainer().getContext();

        final CountDownLatch ready = new CountDownLatch(1);

        final BusyBee busyBee = (BusyBee) context.lookup("java:global/access-timeout/BusyBee");

        // This asynchronous method will never exit 
        busyBee.stayBusy(ready);

        // Are you working yet little bee?
        ready.await();

        // OK, Bee is busy

        { // Timeout Immediately
            final long start = System.nanoTime();

            try {
                busyBee.doItNow();

                fail("The bee should be busy");
            } catch (Exception e) {
                // the bee is still too busy as expected
            }

            assertEquals(0, seconds(start));
        }

        { // Timeout in 5 seconds
            final long start = System.nanoTime();

            try {
                busyBee.doItSoon();

                fail("The bee should be busy");
            } catch (Exception e) {
                // the bee is still too busy as expected
            }

            assertEquals(5, seconds(start));
        }

        // This will wait forever, give it a try if you have that long
        //busyBee.justDoIt();
    }

    private long seconds(long start) {
        return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
    }
}
