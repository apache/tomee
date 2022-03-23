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
package org.apache.openejb.core.stateful;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.Stateful;
import jakarta.ejb.StatefulTimeout;
import javax.naming.Context;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class StatefulStatefulTimeoutTest {
    private Context context;

    @Before
    public void initContext() {
        context = (Context) System.getProperties().get(ApplicationComposers.OPENEJB_APPLICATION_COMPOSER_CONTEXT);
    }

    @Stateful
    @StatefulTimeout(value = 3, unit = TimeUnit.SECONDS)
    public static class TimedOutStateful {
        public void foo() {
        }
    }

    @Configuration
    public Properties properties() {
        final Properties properties = new Properties();
        properties.setProperty("Default Stateful Container.Frequency", "1seconds");
        return properties;
    }

    @Module
    public EnterpriseBean stateful() {
        return new StatefulBean("TimedOutStateful", TimedOutStateful.class);
    }

    @Test
    public void checkBeanIsCleaned() throws Exception {
        assertNotNull(context);
        final TimedOutStateful stateful = (TimedOutStateful) context.lookup("global/StatefulStatefulTimeoutTest/stateful/TimedOutStateful");
        stateful.foo();
        Thread.sleep(6000);
        try {
            stateful.foo();
            fail();
        } catch (final NoSuchEJBException e) {
            // ok
        }
    }
}
