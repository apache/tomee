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
package org.apache.openejb.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.openejb.util.Pool;
import org.apache.openejb.util.Duration;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.interceptor.InvocationContext;

/**
 * @version $Rev$ $Date$
 */
public class ScratchPad {

    public static void main(String[] args) throws Exception {
        new ScratchPad().main();
    }

    public void main() throws Exception {

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        Pool.Builder builder = new Pool.Builder();
        builder.setPoolMin(4);
        builder.setIdleTimeout(new Duration("30 seconds"));
        builder.setPollInterval(new Duration("15 seconds"));
        builder.setMaxAge(new Duration("2 minutes"));
        builder.setSupplier(new Pool.Supplier(){
            public void discard(Object o, Pool.Event reason) {
            }

            public Object create() {
                return "";
            }
        });
        Pool pool = builder.build();
        pool.start();
        pool.add("");
        pool.add("");
        pool.add("");
        pool.add("");
        pool.add("");
        pool.add("");

        Map<String, String> map = new HashMap<String, String>();
        map.put("EJBModule", "FooModule");
        map.put("J2EEApplication", "FooApp");
        map.put("J2EEServer", "FooServer");
        map.put("j2eeType", "StatelessSessionBean");
        map.put("name", "Pool");

        ObjectName objectName = new ObjectName("something", new Hashtable(map));
        server.registerMBean(new ManagedMBean(pool), objectName);

//        while ("".equals("")) {
//            object.tick(System.currentTimeMillis() % 1000);
//            Thread.sleep(287);
//        }

//        server.createMBean()
        while (true) {
            List<Pool.Entry> entries = new ArrayList<Pool.Entry>();

            try {
                while (true) {
                    entries.add(pool.pop(1, TimeUnit.SECONDS));
                    snooze();
                }
            } catch (TimeoutException e) {
            }

            for (Pool.Entry entry : entries) {
                pool.push(entry);
                snooze();
            }
        }
//        new CountDownLatch(1).await();

    }

    private void snooze() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

}
