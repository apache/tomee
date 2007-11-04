/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.client;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class NoOverrideCallbackFilter implements CallbackFilter {
    private Class superClass;

    public NoOverrideCallbackFilter(Class superClass) {
        this.superClass = superClass;
    }

    public int accept(Method method) {
        // we don't intercept non-public methods like finalize
        if (!Modifier.isPublic(method.getModifiers())) {
            return 0;
        }

        if (method.getName().equals("remove") && Modifier.isAbstract(method.getModifiers())) {
            return 1;
        }

        try {
            // if the super class defined this method don't intercept
            superClass.getMethod(method.getName(), method.getParameterTypes());
            return 0;
        } catch (Throwable e) {
            return 1;
        }
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }

        NoOverrideCallbackFilter otherFilter = null;
        if (other instanceof NoOverrideCallbackFilter) {
            otherFilter = (NoOverrideCallbackFilter) other;
        } else {
            return false;
        }

        return superClass.equals(otherFilter.superClass);
    }

    public int hashCode() {
        return superClass.hashCode();
    }
}
