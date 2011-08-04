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

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class UrlComparator implements Comparator<URL> {
    private File dir;
    private List<String> rootPath;

    public UrlComparator(File directory) {
        dir = directory;
        rootPath = path(dir);
    }

    public UrlComparator(URL base) {
        this(URLs.toFile(base));
    }

    public int compare(URL a, URL b) {
        return score(b) - score(a);
    }

    private int score(URL url){
        File file = URLs.toFile(url);
        List<String> filePath = path(file);
        int matches = 0;

        ListIterator<String> a = rootPath.listIterator();
        ListIterator<String> b = filePath.listIterator();
        while(a.hasNext() && b.hasNext()) {
            String nameA = a.next();
            String nameB = b.next();

            if (nameA.equals(nameB)) {
                matches++;
            } else {
                break;
            }
        }

        return matches;
    }

    private List<String> path(File file){
        ArrayList<String> path = new ArrayList<String>();
        path(file, path);
        return path;
    }

    private void path(File file, List<String> path){
        if (file == null) return;

        path(file.getParentFile(), path);

        path.add(file.getName());
    }
}
