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

import org.junit.Assert;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CounterCallbacksTest implements ExecutionObserver {

    private List<Object> received = new ArrayList<Object>();

    public Context getContext() throws NamingException {
        final Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        return new InitialContext(p);

    }

    @Test
    public void test() throws Exception {
        final Map<String, Object> p = new HashMap<String, Object>();
        p.put("MySTATEFUL", "new://Container?type=STATEFUL");
        p.put("MySTATEFUL.Capacity", "2"); //How many instances of Stateful beans can our server hold in memory?
        p.put("MySTATEFUL.Frequency", "1"); //Interval in seconds between checks
        p.put("MySTATEFUL.BulkPassivate", "0"); //No bulkPassivate - just passivate entities whenever it is needed
        final EJBContainer container = EJBContainer.createEJBContainer(p);

        //this is going to track the execution
        ExecutionChannel.getInstance().addObserver(this);

        {
            final Context context = getContext();

            CallbackCounter counterA = (CallbackCounter) context.lookup("java:global/simple-stateful-callbacks/CallbackCounter");
            Assert.assertNotNull(counterA);
            Assert.assertEquals("postConstruct", this.received.remove(0));

            Assert.assertEquals(0, counterA.count());
            Assert.assertEquals("count", this.received.remove(0));

            Assert.assertEquals(1, counterA.increment());
            Assert.assertEquals("increment", this.received.remove(0));

            Assert.assertEquals(0, counterA.reset());
            Assert.assertEquals("reset", this.received.remove(0));

            Assert.assertEquals(1, counterA.increment());
            Assert.assertEquals("increment", this.received.remove(0));

            System.out.println("Waiting 2 seconds...");
            Thread.sleep(2000);

            Assert.assertEquals("preDestroy", this.received.remove(0));

            try {
                counterA.increment();
                Assert.fail("The ejb is not supposed to be there.");
            } catch (jakarta.ejb.NoSuchEJBException e) {
                //excepted
            }

            context.close();
        }

        {
            final Context context = getContext();

            CallbackCounter counterA = (CallbackCounter) context.lookup("java:global/simple-stateful-callbacks/CallbackCounter");
            Assert.assertEquals("postConstruct", this.received.remove(0));

            Assert.assertEquals(1, counterA.increment());
            Assert.assertEquals("increment", this.received.remove(0));

            ((CallbackCounter) context.lookup("java:global/simple-stateful-callbacks/CallbackCounter")).count();
            Assert.assertEquals("postConstruct", this.received.remove(0));
            Assert.assertEquals("count", this.received.remove(0));

            ((CallbackCounter) context.lookup("java:global/simple-stateful-callbacks/CallbackCounter")).count();
            Assert.assertEquals("postConstruct", this.received.remove(0));
            Assert.assertEquals("count", this.received.remove(0));

            System.out.println("Waiting 2 seconds...");
            Thread.sleep(2000);
            Assert.assertEquals("prePassivate", this.received.remove(0));

            context.close();
        }
        container.close();

        Assert.assertEquals(this.received.toString(),2, this.received.size());
        Assert.assertEquals("preDestroy", this.received.remove(0));
        Assert.assertEquals("preDestroy", this.received.remove(0));

        Assert.assertTrue(this.received.toString(), this.received.isEmpty());
    }

    @Override
    public void onExecution(Object value) {
        System.out.println("[" + System.currentTimeMillis() + "] Test step -> " + value);
        this.received.add(value);
    }
}
