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

import org.apache.openejb.Injection;
import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.testing.TestInstance;
import org.apache.openejb.loader.SystemInstance;
import org.mockito.MockitoAnnotations;

/**
 * this class is instantiated when the FallbackPropertyInjector is set
 * it is generally when the container is started
 * it will reset mockito context (stored mocks) and do the mock injections in the test class
 */
public class MockitoInjector implements FallbackPropertyInjector {
    public MockitoInjector() {
        final Object instance = SystemInstance.get().getComponent(TestInstance.class).getInstance();
        if (instance != null) {
            MockitoAnnotations.initMocks(instance);
        }
        MockRegistry.reset();
    }

    @Override
    public Object getValue(final Injection injection) {
        try {
            return MockRegistry.mocksByType()
                        .get(injection.getTarget().getDeclaredField(injection.getName()).getType());
        } catch (Exception e) {
            return MockRegistry.mocksByName().get(injection.getName());
        }
    }
}
