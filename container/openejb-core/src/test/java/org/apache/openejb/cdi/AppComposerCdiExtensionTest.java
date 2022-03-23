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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.CdiExtensions;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.spi.Extension;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@CdiExtensions(AppComposerCdiExtensionTest.ThisTestExtensionWithoutSPIFiles.class)
@RunWith(ApplicationComposer.class)
public class AppComposerCdiExtensionTest {
    @Module
    @Classes(cdi = true)
    public EjbJar jar() {
        return new EjbJar();
    }

    @Test
    public void checkOnlyConfiguredExtensionsArePresent() {
        final Map<?, ?> extensions = Map.class.cast(Reflections.get(WebBeansContext.currentInstance().getExtensionLoader(), "extensions"));
        assertEquals(1, extensions.size());
        assertEquals(ThisTestExtensionWithoutSPIFiles.class, extensions.values().iterator().next().getClass());
    }

    public static class ThisTestExtensionWithoutSPIFiles implements Extension {
        // no need of body
    }
}
