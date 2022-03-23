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
package org.apache.openejb.cdi;

import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.CdiExtensions;
import org.apache.openejb.testing.Classes;
import org.junit.Test;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.concurrent.Callable;

@CdiExtensions(EJBVetoTest.Vetoer.class)
@Classes(cdi = true, innerClassesAsBean = true)
public class EJBVetoTest {
    @Test
    public void run() throws Exception { // https://issues.apache.org/jira/browse/TOMEE-2020, we just check we get no NPE
        new ApplicationComposers(EJBVetoTest.class).evaluate(this, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
    }

    public static class Vetoer implements Extension {
        void pat(@Observes final ProcessAnnotatedType<ToVeto> pat) {
            pat.veto();
        }
    }

    @Startup
    @Singleton
    public static class ToVeto {
    }
}
