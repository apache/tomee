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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FilteredServiceManagerTest {
    @Test
    public void checkJaxRs() {
        final FilteredServiceManager fsm = new FilteredServiceManager(new String[] { "jaxrs" });
        assertTrue(fsm.accept("httpejbd"));
        assertTrue(fsm.accept("cxf-rs"));
    }

    @Test
    public void checkJaxWs() {
        final FilteredServiceManager fsm = new FilteredServiceManager(new String[] { "jaxws" });
        assertTrue(fsm.accept("httpejbd"));
        assertTrue(fsm.accept("cxf"));
    }

    @Test
    public void checkEjbd() {
        final FilteredServiceManager fsm = new FilteredServiceManager(new String[] { "http" });
        assertTrue(fsm.accept("httpejbd"));
    }

    @Test
    public void checkDefault() {
        final FilteredServiceManager fsm = new FilteredServiceManager(new String[] { "foo" });
        assertTrue(fsm.accept("foo"));
    }
}
