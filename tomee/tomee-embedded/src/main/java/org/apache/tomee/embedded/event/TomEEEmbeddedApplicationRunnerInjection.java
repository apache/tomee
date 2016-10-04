/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.embedded.event;

import org.apache.openejb.observer.Event;

import java.lang.reflect.Field;

@Event
public class TomEEEmbeddedApplicationRunnerInjection {
    private final Object instance;

    public TomEEEmbeddedApplicationRunnerInjection(final Object instance) {
        this.instance = instance;
    }

    public Object getInstance() {
        return instance;
    }

    public <T> TomEEEmbeddedApplicationRunnerInjection inject(final Class<T> type, final T value) {
        Class<?> aClass = instance.getClass();
        while (aClass != null) {
            for (final Field f : aClass.getDeclaredFields()) {
                if (f.getType().isAssignableFrom(type)) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    try {
                        f.set(instance, value);
                    } catch (final IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            aClass = aClass.getSuperclass();
        }
        return this;
    }
}
