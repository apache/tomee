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

import org.apache.xbean.finder.archive.Archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class MockArchive implements Archive {
    private final List<String> list = new ArrayList<String>();

    public MockArchive(String... classNames) {
        this(Arrays.asList(classNames));
    }
    
    public MockArchive(Iterable<String> classNames) {
        for (String className : classNames) {
            list.add(className);
        }
    }

    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
        return null;
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return null;
    }

    public Iterator<Entry> iterator() {
        return new ArchiveIterator(this, list.iterator());
    }

}
