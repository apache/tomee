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
package org.apache.openejb.arquillian.common.mockito;

import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Method;

/**
 * do it by reflection to avoid to need mockito
 */
public class MockitoEnricher implements TestEnricher {
    private static final String MOCKITO_CLASS = "org.mockito.MockitoAnnotations";

    @Override
    public void enrich(final Object testCase) {
        try {
            final Class<?> clazz = testCase.getClass().getClassLoader().loadClass(MOCKITO_CLASS);
            final Method injectMethod = clazz.getMethod("initMocks", Object.class);
            injectMethod.invoke(null, testCase);
        } catch (final Exception e) {
            // no-op: can't use mockito, not a big deal for common cases
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
