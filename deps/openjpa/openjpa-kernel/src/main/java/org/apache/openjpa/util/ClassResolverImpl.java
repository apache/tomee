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
 * Default class resolver implementation.
 *
 * @since 0.3.0
 * @author Abe White
 */
public class ClassResolverImpl
    implements ClassResolver {

    public ClassLoader getClassLoader(Class<?> contextClass,  ClassLoader envLoader) {
        // get the context class' loader; if the class was loaded by the
        // bootstrap loader, use the system classloader in the hopes that
        // class.forName calls on it will find the bootstrap loader's cached
        // class (the bootstrap loader is the parent of the system loader)
        ClassLoader contextLoader = null;
        if (contextClass != null) {
            contextLoader = AccessController.doPrivileged(
                J2DoPrivHelper.getClassLoaderAction(contextClass)); 
            if (contextLoader == null)
                contextLoader = AccessController.doPrivileged(
                    J2DoPrivHelper.getSystemClassLoaderAction()); 
        }

        // if there is only one unique loader, just return it
        ClassLoader threadLoader = AccessController.doPrivileged(
            J2DoPrivHelper.getContextClassLoaderAction());
        if ((contextLoader == null || contextLoader == threadLoader)
            && (envLoader == null || envLoader == threadLoader))
            return threadLoader;

        // construct a multi class loader that will delegate in the order
        // described in section 12.5 of the spec
        MultiClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
        if (contextLoader != null)
            loader.addClassLoader(contextLoader);
        loader.addClassLoader(threadLoader);
        if (envLoader != null)
            loader.addClassLoader(envLoader);
        return loader;
    }
}
