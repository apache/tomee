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
package org.apache.openejb.testing.app;

import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.ApplicationComposersTest;
import org.apache.openejb.testing.Classes;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Classes(cdi = true, value = Application.CdiBean.class)
public class Application {
    public static volatile int startCount = 0;
    private final String[] args;

    public Application(String[] args) {
        this.args = args;
    }

    @AppResource
    private ApplicationComposers composers;

    @Inject
    private CdiBean bean;

    @PostConstruct
    public void init() {
        startCount++;
        try {
            assertNotNull(bean);
            assertEquals("run", bean.run());
            assertNotNull(args);
            assertEquals(asList("a", "b"), asList(args));
            ApplicationComposersTest.ok = true;
        } finally {
            try {
                composers.after();
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    public static class CdiBean {
        String run() {
            return "run";
        }
    }

}
