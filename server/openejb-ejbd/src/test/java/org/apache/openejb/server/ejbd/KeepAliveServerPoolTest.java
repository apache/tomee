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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerServiceFilter;
import org.apache.openejb.server.ServicePool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class KeepAliveServerPoolTest {

    @Before
    public void init() throws Exception {
        SystemInstance.init(new Properties());
    }

    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void eachServerFindsItsOwnPool() throws Exception {
        final EjbServer ejbd = new EjbServer();
        final EjbServer ejbds = new EjbServer();
        final ServicePool ejbdPool = new ServicePool(ejbd, 1);
        final ServicePool ejbdsPool = new ServicePool(new ServerServiceFilter(ejbds), 1);
        try {
            ejbdPool.start();
            ejbdsPool.start();

            assertSame(ejbdPool, KeepAliveServer.findPool(ejbd));
            assertSame(ejbdsPool, KeepAliveServer.findPool(ejbds));
        } finally {
            ejbdPool.stop();
            ejbdsPool.stop();
            ejbdPool.getThreadPool().shutdownNow();
            ejbdsPool.getThreadPool().shutdownNow();
        }
    }

    @Test
    public void noPoolBeforeStart() {
        final EjbServer ejbd = new EjbServer();
        final ServicePool pool = new ServicePool(ejbd, 1);
        try {
            assertNull(KeepAliveServer.findPool(ejbd));
        } finally {
            pool.getThreadPool().shutdownNow();
        }
    }

    @Test
    public void unwrappableServiceUsesItsPool() {
        final ServicePool pool = new ServicePool(new EjbServer(), 1);
        try {
            assertSame(pool, KeepAliveServer.findPool(pool));
        } finally {
            pool.getThreadPool().shutdownNow();
        }
    }
}
