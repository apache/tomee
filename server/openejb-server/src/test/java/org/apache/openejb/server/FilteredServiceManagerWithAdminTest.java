/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * This test aims at testing the filtering feature on services.
 * Basically, this test does not do a lot of stuff except declaring only one service
 * and looking through the service if there is only one service.
 */
@EnableServices("admin")
@RunWith(ApplicationComposer.class)
public class FilteredServiceManagerWithAdminTest {

    @Module
    public EjbJar jar() {
        return new EjbJar();
    }

    @Test
    public void numberOfServices() {
        // when using @EnableServices with the application composer
        // the return value should be a FilteredServiceManager
        assertEquals(FilteredServiceManager.class, ServiceManager.get().getClass());

        final FilteredServiceManager manager = (FilteredServiceManager) ServiceManager.get();
        assertEquals(1, manager.getDaemons().length);
        assertEquals("admin", manager.getDaemons()[0].getName());
    }

}
