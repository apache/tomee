/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.composed.rest;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.mockito.MockitoInjector;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.MockInjector;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(ApplicationComposer.class)
public class GreetingServiceTest {

    @Mock
    private Messager messager;

    @Configuration
    public Properties configuration() {
        return new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, Boolean.TRUE.toString());
        }};
    }

    @MockInjector
    public Class<?> mockitoInjector() {
        return MockitoInjector.class;
    }

    @Module
    public Class<?>[] app() {
        return new Class<?>[]{GreetingService.class, Messager.class};
    }

    @Test
    public void checkMockIsUsed() throws IOException {
        when(messager.message()).thenReturn("mockito");

        final String message = IO.slurp(new URL("http://localhost:4204/GreetingServiceTest/greeting/"));
        assertEquals("mockito", message);
    }
}
