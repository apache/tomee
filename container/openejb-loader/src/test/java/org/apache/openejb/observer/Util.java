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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.observer;

import org.apache.openejb.observer.event.AfterEvent;
import org.apache.openejb.observer.event.BeforeEvent;
import org.apache.openejb.observer.event.ObserverFailed;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Util {

    public static String join(final Object... collection) {
        return join(Arrays.asList(collection));
    }

    public static String join(final Collection<?> collection) {
        final String delimiter = "\n";
        if (collection.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (final Object obj : collection) {
            sb.append(obj).append(delimiter);
        }
        return sb.substring(0, sb.length() - delimiter.length());
    }

    static void assertEvent(final List<String> observed, final String... expected) {
        assertEquals(join(expected), join(observed));
    }

    static Method caller(final int i) {
        try {
            final StackTraceElement[] stackTrace = new Exception().fillInStackTrace().getStackTrace();
            final String methodName = stackTrace[i].getMethodName();
            final String className = stackTrace[i].getClassName();

            final Class<?> clazz = Util.class.getClassLoader().loadClass(className);
            for (final Method method : clazz.getDeclaredMethods()) {
                if (methodName.endsWith(method.getName())) {
                    return method;
                }
            }

            throw new NoSuchMethodException(methodName);
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static String description(final Object event) {
        if (event instanceof ObserverFailed) {
            final ObserverFailed observerFailed = (ObserverFailed) event;
            return "ObserverFailed{" + observerFailed.getMethod().getName() + "}";
        }
        if (event instanceof BeforeEvent) {
            return "BeforeEvent<" + description(((BeforeEvent) event).getEvent()) + ">";
        }
        if (event instanceof AfterEvent) {
            return "AfterEvent<" + description(((AfterEvent) event).getEvent()) + ">";
        }
        return event.getClass().getSimpleName();
    }
}
