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

package org.apache.openejb.util.classloader;

/**
 * Simply to be able to get rid of the openwebbeans classloader stuff
 * without patching it.
 */
public class MultipleClassLoader extends ClassLoader implements ClassLoaderComparator {
    private final ClassLoader second;

    public MultipleClassLoader(final ClassLoader first, final ClassLoader second) {
        super(first);
        this.second = second;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (final ClassNotFoundException | LinkageError ncdfe) {
            if (second != getParent()) {
                return loadClassSecond(name);
            }
            throw ncdfe;
        }
    }

    public Class<?> loadClassSecond(final String name) throws ClassNotFoundException {
        return second.loadClass(name);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || getParent().equals(other);
    }

    @Override
    public int hashCode() {
        return getParent().hashCode();
    }

    @Override
    public boolean isSame(final ClassLoader cl) {
        return equals(cl);
    }
}
