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
package org.apache.openejb.config;

import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.filter.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
* @version $Rev$ $Date$
*/
public class AggregatedArchive implements Archive {

    private final Map<URL, List<String>> classesMap = new HashMap<URL, List<String>>();
    private final Archive archive;

    public AggregatedArchive(ClassLoader loader, Iterable<URL> urls) {
        final List<Archive> archives = new ArrayList<Archive>();

        for (URL url : urls) {

            final List<String> classes = new ArrayList<String>();

            final Archive archive = new FilteredArchive(ClasspathArchive.archive(loader, url), new Filter(){
                @Override
                public boolean accept(String name) {
                    classes.add(name);
                    return true;
                }
            });

            classesMap.put(url, classes);
            archives.add(archive);
        }

        this.archive = new CompositeArchive(archives);
    }

    @Override
    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
        return archive.getBytecode(className);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return archive.loadClass(className);
    }

    @Override
    public Iterator<String> iterator() {
        return archive.iterator();
    }

    public Map<URL, List<String>> getClassesMap() {
        return classesMap;
    }
}
