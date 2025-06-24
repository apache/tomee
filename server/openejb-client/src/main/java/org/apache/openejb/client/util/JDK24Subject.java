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
package org.apache.openejb.client.util;

import javax.security.auth.Subject;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class JDK24Subject {

    private static final MethodHandle CURRENT = lookupCurrent();

    /**
     * Maps to Subject.current() is available, otherwise maps to Subject.getSubject()
     * @return the current subject
     */
    public static Subject currentSubject() {
        try {
            return (Subject) CURRENT.invoke();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static MethodHandle lookupCurrent() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            // Subject.getSubject(AccessControlContext) is deprecated for removal and replaced by
            // Subject.current().
            // Lookup first the new API, since for Java versions where both exists, the
            // new API delegates to the old API (for example Java 18, 19 and 20).
            // Otherwise (Java 17), lookup the old API.
            return lookup.findStatic(Subject.class, "current",
                    MethodType.methodType(Subject.class));
        } catch (NoSuchMethodException e) {
            final MethodHandle getContext = lookupGetContext();
            final MethodHandle getSubject = lookupGetSubject();
            return MethodHandles.filterReturnValue(getContext, getSubject);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static MethodHandle lookupGetSubject() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            final Class<?> contextClazz =
                    ClassLoader.getSystemClassLoader()
                            .loadClass("java.security.AccessControlContext");
            return lookup.findStatic(Subject.class, "getSubject",
                    MethodType.methodType(Subject.class, contextClazz));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static MethodHandle lookupGetContext() {
        try {
            // Use reflection to work with Java versions that have and don't have AccessController.
            final Class<?> controllerClazz =
                    ClassLoader.getSystemClassLoader().loadClass("java.security.AccessController");
            final Class<?> contextClazz =
                    ClassLoader.getSystemClassLoader()
                            .loadClass("java.security.AccessControlContext");

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            return lookup.findStatic(controllerClazz, "getContext",
                    MethodType.methodType(contextClazz));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
