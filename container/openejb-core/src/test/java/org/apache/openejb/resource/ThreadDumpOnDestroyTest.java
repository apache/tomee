/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource;

import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Classes
@ContainerProperties({
        @ContainerProperties.Property(name = "test", value = "new://Resource?class-name=org.apache.openejb.resource.ThreadDumpOnDestroyTest$WillFail&pre-destroy=destroy"),
        @ContainerProperties.Property(name = "openejb.log.async", value = "false"), // don't loose logs
        @ContainerProperties.Property(name = "openejb.resources.destroy.timeout", value = "3 seconds"),
        @ContainerProperties.Property(name = "openejb.resources.destroy.stack-on-timeout", value = "true")
})
public class ThreadDumpOnDestroyTest {
    @Test
    public void run() throws Exception {
        final List<String> records = new ArrayList<>();
        final Handler h = new Handler() {
            @Override
            public synchronized void publish(final LogRecord record) {
                records.add(record.getMessage());
            }

            @Override
            public void flush() {
                // no-op
            }

            @Override
            public void close() throws SecurityException {
                // no-op
            }
        };
        new ApplicationComposers(ThreadDumpOnDestroyTest.class)
                .evaluate(this, new Runnable() {
                    @Override
                    public void run() {
                        Logger.getLogger("OpenEJB.startup").addHandler(h);
                    }
                });
        Logger.getLogger("OpenEJB.startup").removeHandler(h);
        assertEquals("Can't destroy test in 3 seconds, giving up.", records.get(1) /*0 is "undeploying..."*/);
        assertTrue(records.get(2).contains("\"openejb-resource-destruction-test - 1\" suspended=false state=TIMED_WAITING"));
        assertTrue(records.get(2).contains("org.apache.openejb.resource.ThreadDumpOnDestroyTest$WillFail.destroy"));
    }

    public static class WillFail {
        public void destroy() {
            try {
                sleep(5000);
            } catch (final InterruptedException e) {
                Thread.interrupted();
                fail();
            }
        }
    }
}
