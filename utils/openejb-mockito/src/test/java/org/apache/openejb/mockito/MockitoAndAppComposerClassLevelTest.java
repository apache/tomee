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
package org.apache.openejb.mockito;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import jakarta.ejb.EJB;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(ApplicationComposer.class)
@ContainerProperties(
        @ContainerProperties.Property(name = "org.apache.openejb.injection.FallbackPropertyInjector", value = "org.apache.openejb.mockito.MockitoInjector")
)
public class MockitoAndAppComposerClassLevelTest {
    @EJB
    private Facade facade;

    @Mock
    private Hello mock;

    @Mock(name = "named")
    private Hello named;

    @Module
    public Class<?>[] classes() {
        return new Class<?>[] { Hello.class, Facade.class };
    }

    @Test
    public void testDefault() {
        // play with mocks
        when(mock.hi())
            .thenReturn("openejb-mockito");
        when(mock.id())
                .thenReturn(12345);

        // test
        assertEquals("openejb-mockito", facade.hello());
    }

    @Test
    public void testName() {
        // play with mocks
        when(named.hi())
                .thenReturn("named");

        // test
        assertEquals("named", facade.name());
    }
}
