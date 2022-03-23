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
package org.apache.openejb.core.stateless;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import static org.junit.Assert.assertEquals;

@SimpleLog
@Classes(innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@ContainerProperties(@ContainerProperties.Property(name = "Default Stateless Container.useOneSchedulerThreadByBean", value = "false"))
public class SchedulerThreadStatelessTest {
    @Stateless
    public static class S1 {
        public void touch() {
        }
    }

    @Stateless
    public static class S2 {
        public void touch() {
        }
    }

    @EJB
    private S1 s1;

    @EJB
    private S2 s2;

    @Test
    public void run() {
        s1.touch();
        s2.touch();

        final int count = Thread.activeCount();
        final Thread[] all = new Thread[count];
        Thread.enumerate(all);
        int schedulers = 0;
        for (final Thread t : all) {
            if (t.getName().startsWith("Default Stateless Container")) {
                schedulers++;
            }
        }
        assertEquals(1, schedulers);
    }
}
