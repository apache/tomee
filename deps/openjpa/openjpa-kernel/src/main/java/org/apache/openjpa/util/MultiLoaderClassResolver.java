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
package org.apache.openjpa.util;

import java.security.AccessController;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.MultiClassLoader;

/**
 * A simple {@link ClassResolver} that uses multiple class loaders to
 * resolve classes.
 *
 * @author Steve Kim
 */
public class MultiLoaderClassResolver implements ClassResolver {

    final private MultiClassLoader _loader = AccessController
        .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());

    public MultiLoaderClassResolver() {
    }

    public MultiLoaderClassResolver(ClassLoader[] loaders) {
        for (int i = 0; i < loaders.length; i++)
            _loader.addClassLoader(loaders[i]);
    }

    public boolean addClassLoader(ClassLoader loader) {
        return _loader.addClassLoader(loader);
    }

    public ClassLoader getClassLoader(Class ctx, ClassLoader envLoader) {
        return _loader;
    }
}
