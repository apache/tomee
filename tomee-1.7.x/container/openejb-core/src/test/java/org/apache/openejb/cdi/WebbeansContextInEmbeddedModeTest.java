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
package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Module;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(ApplicationComposer.class)
public class WebbeansContextInEmbeddedModeTest {
    @Module
    public EjbJar jar() {
        return new EjbJar();
    }

    @Test
    public void checkWebbeansContext() {
        final WebBeansContext ctx1 = WebBeansContext.currentInstance();
        final List<AppContext> appCtxs = SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts();
        assertEquals(1, appCtxs.size());
        final WebBeansContext ctx2 = appCtxs.iterator().next().getWebBeansContext();
        assertSame(ctx1, ctx2);
    }
}
