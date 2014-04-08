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
package org.apache.openejb.arquillian.common.deployment;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeploymentExceptionObserver {
    private static final Map<Class<?>, Exception> EXCEPTIONS = new HashMap<Class<?>, Exception>();
    private static final Map<Class<?>, Exception> PARENT_EXCEPTIONS = new HashMap<Class<?>, Exception>();

    public void observes(@Observes final DeploymentException t) throws Exception {
        EXCEPTIONS.put(t.getClass(), t);

        Throwable current = t.getCause();
        while (current != null) {
            if (current instanceof Exception) {
                PARENT_EXCEPTIONS.put(current.getClass(), (Exception) current);
            }
            if (current.getCause() != current) {
                current = current.getCause();
            }
        }

        throw t; // don't forget it even if it is an observer and not an interceptor
    }

    public static Exception getExceptions(final Class<?> clazz) {
        final Exception ex = EXCEPTIONS.get(clazz);
        if (ex != null) {
            return ex;
        }
        return PARENT_EXCEPTIONS.get(clazz);
    }

    public static Set<Class<?>> availableExceptionTypes() {
        final Set<Class<?>> set = new HashSet<Class<?>>(EXCEPTIONS.keySet());
        set.addAll(PARENT_EXCEPTIONS.keySet());
        return set;
    }

    public void cleanUp(@Observes final AfterClass event) throws Exception {
        EXCEPTIONS.clear();
        PARENT_EXCEPTIONS.clear();
    }
}
