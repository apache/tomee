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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.xbean.finder.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
* @version $Rev$ $Date$
*/
public class ArchiveIterator implements Iterator<Archive.Entry> {
    private final Iterator<String> classes;
    private final Archive archive;

    public ArchiveIterator(Archive archive, Iterator<String> classes) {
        this.archive = archive;
        this.classes = classes;
    }

    public boolean hasNext() {
        return classes.hasNext();
    }

    public Archive.Entry next() {
        final String name = classes.next();
        return new Archive.Entry() {
            public String getName() {
                return name;
            }

            public InputStream getBytecode() throws IOException {
                try {
                    return archive.getBytecode(name);
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    public void remove() {
        classes.remove();
    }
}
