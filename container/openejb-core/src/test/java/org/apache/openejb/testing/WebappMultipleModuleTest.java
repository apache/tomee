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
package org.apache.openejb.testing;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@CdiExtensions(WebappMultipleModuleTest.SuperViciousExtension.class)
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, innerClassesAsBean = true)
public class WebappMultipleModuleTest {
    @Module
    @PersistenceRootUrl(value = "")
    public Persistence jpa() throws Exception {
        SuperViciousExtension.CALLED.set(false); // reset before container boot
        return new Persistence(new PersistenceUnit("jpa"));
    }

    @Inject
    private Marker bean;

    @Test
    public void run() {
        assertNotNull(bean);
        assertTrue(SuperViciousExtension.CALLED.get());
    }

    public static class Marker {}

    public static class SuperViciousExtension implements Extension {
        public static final AtomicBoolean CALLED = new AtomicBoolean();

        private void end(@Observes final AfterDeploymentValidation ignored , final BeanManager manager) {
            final Bean<?> bean = manager.resolve(manager.getBeans(Marker.class));
            assertNotNull(manager.getReference(bean, Marker.class, manager.createCreationalContext(bean)));
            CALLED.set(true);
        }
    }
}
