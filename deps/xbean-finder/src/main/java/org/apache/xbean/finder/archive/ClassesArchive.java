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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder.archive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class ClassesArchive implements Archive {

    private final Set<ClassLoader> loaders = new LinkedHashSet<ClassLoader>();
    private final Map<String, Class<?>> classes = new LinkedHashMap<String, Class<?>>();

    public ClassesArchive(Class<?>... classes) {
        this(Arrays.asList(classes));
    }

    public ClassesArchive(Iterable<Class<?>> classes) {
        assert classes != null;

        for (Class<?> clazz : classes) {
            if (clazz == null) continue;
            if (clazz.getClassLoader() == null) continue;
            this.classes.put(clazz.getName(), clazz);
            loaders.add(clazz.getClassLoader());
        }
    }

    public Iterator<Entry> iterator() {
        return new ArchiveIterator(this, classes.keySet().iterator());
    }

    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
        assert className != null;

        int pos = className.indexOf("<");
        if (pos > -1) {
            className = className.substring(0, pos);
        }
        pos = className.indexOf(">");
        if (pos > -1) {
            className = className.substring(0, pos);
        }
        if (!className.endsWith(".class")) {
            className = className.replace('.', '/') + ".class";
        }
        for (ClassLoader loader : loaders) {
            URL resource = loader.getResource(className);
            if (resource != null) return new BufferedInputStream(resource.openStream());
        }

        throw new ClassNotFoundException(className);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> clazz = classes.get(className);
        if (clazz != null) return clazz;

        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(className);
            } catch (ClassNotFoundException e) {
            }
        }

        throw new ClassNotFoundException(className);
    }

}
