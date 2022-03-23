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
package org.apache.openejb.junit;

import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SingletonBean;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * Test for https://issues.apache.org/jira/browse/OPENEJB-2014
 *
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class PreDestroyTest {

    private static final AtomicBoolean isConstructed = new AtomicBoolean(false);
    private static final AtomicBoolean isDestroyed = new AtomicBoolean(false);

    @Inject
    private TestMe testMe;

    @AfterClass
    public static void onAfterClass() {
        assertTrue("onPostConstruct was not called", isConstructed.get());
        assertTrue("onPreDestroy was not called", isDestroyed.get());
    }

    @org.apache.openejb.testing.Module
    public SessionBean getEjbs() {
        return new SingletonBean(TestMe.class);
    }

    @Test
    public void testLifecycle() {
        this.testMe.noOp();
    }

    @Singleton
    public static class TestMe {

        @PostConstruct
        public void onPostConstruct() {
            isConstructed.set(true);
        }

        @PreDestroy
        public void onPreDestroy() {
            isDestroyed.set(true);
        }

        public void noOp() {
            //no-op
        }

    }

}
