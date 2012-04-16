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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @version $Rev$ $Date$
 */
public class CompositeArchive implements Archive {

    private final List<Archive> archives = new ArrayList<Archive>();

    public CompositeArchive(Archive... archives) {
        this(Arrays.asList(archives));
    }

    public CompositeArchive(Iterable<Archive> archives) {
        for (Archive archive : archives) {
            this.archives.add(archive);
        }
    }

    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
        for (Archive archive : archives) {
            try {
                return archive.getBytecode(className);
            } catch (ClassNotFoundException e) {
            }
        }

        throw new ClassNotFoundException(className);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        for (Archive archive : archives) {
            try {
                return archive.loadClass(className);
            } catch (ClassNotFoundException e) {
            }
        }

        throw new ClassNotFoundException(className);
    }

    public Iterator<Entry> iterator() {
        if (archives.size() == 1) return archives.get(0).iterator();
        return new CompositeIterator(archives);
    }

    private static class CompositeIterator implements Iterator<Entry> {

        private Iterator<Archive> archives;
        private Iterator<Entry> current;

        private CompositeIterator(Iterable<Archive> archives) {
            this.archives = archives.iterator();
            if (this.archives.hasNext()) {
                current = this.archives.next().iterator();
            }
        }

        public boolean hasNext() {
            if (current == null) return false;
            if (current.hasNext()) return true;
            
            if (archives.hasNext()) {
                current = archives.next().iterator();
                return hasNext();
            }
            return false;
        }

        public Entry next() {
            if (!hasNext()) throw new NoSuchElementException();

            return current.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
