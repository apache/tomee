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

package org.apache.openejb.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * @version $Rev$ $Date$
 */
public class UrlComparator implements Comparator<URL> {
    private final File dir;
    private final List<String> rootPath;

    public UrlComparator(final File directory) {
        dir = directory;
        rootPath = path(dir);
    }

    public UrlComparator(final URL base) {
        this(URLs.toFile(base));
    }

    public int compare(final URL a, final URL b) {
        return score(b) - score(a);
    }

    private int score(final URL url) {
        final File file = URLs.toFile(url);
        final List<String> filePath = path(file);
        int matches = 0;

        final ListIterator<String> a = rootPath.listIterator();
        final ListIterator<String> b = filePath.listIterator();
        while (a.hasNext() && b.hasNext()) {
            final String nameA = a.next();
            final String nameB = b.next();

            if (nameA.equals(nameB)) {
                matches++;
            } else {
                break;
            }
        }

        return matches;
    }

    private List<String> path(final File file) {
        final ArrayList<String> path = new ArrayList<>();
        path(file, path);
        return path;
    }

    private void path(final File file, final List<String> path) {
        if (file == null) {
            return;
        }

        path(file.getParentFile(), path);

        path.add(file.getName());
    }
}
