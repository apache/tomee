/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.datacache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;

class Caches {

    /**
     * Utility to build up a set of classes from their class names
     * when operating outside the context of a persistence manager.
     * The set classes can be null, in which case a new Set will be created.
     */
    static Set<Class<?>> addTypesByName(OpenJPAConfiguration conf,
        Collection<String> classNames, Set<Class<?>> classes) {
        if (classNames == null || classNames.isEmpty())
            return classes;

        ClassLoader loader = conf.getClassResolverInstance().getClassLoader(null, null);

        Class<?> cls;
        for (String className : classNames) {
            try {
                cls = Class.forName(className, true, loader);
                if (classes == null)
                    classes = new HashSet<Class<?>>();
                classes.add(cls);
            } catch (Throwable t) {
                conf.getLog(OpenJPAConfiguration.LOG_RUNTIME).warn(null, t);
            }
        }
        return classes;
    }
}
