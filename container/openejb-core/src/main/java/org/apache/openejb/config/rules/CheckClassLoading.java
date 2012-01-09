/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.openejb.config.rules;

import org.apache.commons.collections.CollectionUtils;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CheckClassLoading extends ValidationBase {
    public static final String OPENEJB_CHECK_CLASSLOADER_VERBOSE = "openejb.check.classloader.verbose";

    protected AppModule appModule;

    @Override public void validate(AppModule appModule) {
        this.appModule = appModule;
        module = appModule;
        check(appModule.getClassLoader());

        for (WebModule webModule : appModule.getWebModules()) {
            module = webModule;
            validate(webModule);
        }
        super.validate(appModule);
    }

    private void check(final ClassLoader classLoader) {
        UrlSet set;
        UrlSet openejbSet;
        try {
            openejbSet = new UrlSet(OpenEJB.class.getClassLoader());
            set = new UrlSet(classLoader);
            set = set.exclude(openejbSet);
        } catch (IOException e) {
            warn(module.getModuleId() + " application", e.getMessage());
            return;
        }

        final List<URL> parentUrls = openejbSet.getUrls();
        final List<URL> currentUrls = set.getUrls();

        final Classes fcl = new Classes(currentUrls.toArray(new URL[currentUrls.size()]));
        final Classes scl = new Classes(parentUrls.toArray(new URL[parentUrls.size()]));
        final Collection<DiffItem> diffs = intersection(fcl, scl);
        for (DiffItem diff : diffs) {
            warn(module.getModuleId() + " application", diff.toScreen());
        }
    }

    private void validate(WebModule webModule) {
        check(webModule.getClassLoader());
    }

    @Override public void validate(ClientModule clientModule) {
        check(clientModule.getClassLoader());
    }

    @Override public void validate(EjbModule ejbModule) {
        check(ejbModule.getClassLoader());
    }

    public static class Classes {
        private static final String[] CLASS_EXTENSION = new String[] { ".class" };

        private final Map<String, Collection<String>> fileByArchive = new TreeMap<String, Collection<String>>();

        public Classes(final URL[] urls) {
            list(urls);
        }

        public void list(final URL[] urls) {
            fileByArchive.clear();

            // for all archives list all files
            // all is sorted by the natural String order
            for (URL archive : urls) {
                try {
                    final File file = URLs.toFile(archive);
                    final List<String> files = JarUtil.listFiles(file, CLASS_EXTENSION);
                    Collections.sort(files);
                    fileByArchive.put(file.getName(), files);
                } catch (Exception e) {
                    // ignored
                }
            }
        }
    }

    public static Collection<DiffItem> intersection(Classes cl1, Classes cl2) {
        final List<DiffItem> diff = new ArrayList<DiffItem>();
        for (Map.Entry<String, Collection<String>> entry1 : cl1.fileByArchive.entrySet()) {
            for (Map.Entry<String, Collection<String>> entry2 : cl2.fileByArchive.entrySet()) {
                Collection<String> v1 = entry1.getValue();
                Collection<String> v2 = entry2.getValue();
                Collection<String> inter = CollectionUtils.intersection(v1, v2);

                if (inter.size() == 0) {
                    continue;
                }

                if (inter.size() == v1.size() && v1.size() == v2.size()) {
                    diff.add(new SameItem(inter, entry1.getKey(), entry2.getKey()));
                } else if (inter.size() == v1.size()) {
                    diff.add(new IncludedItem(inter, entry1.getKey(), entry2.getKey()));
                } else if (inter.size() == v2.size()) {
                    diff.add(new ContainingItem(inter, entry1.getKey(), entry2.getKey()));
                } else {
                    diff.add(new DiffItem(inter, entry1.getKey(), entry2.getKey()));
                }
            }
        }

        Collections.sort(diff, DiffItemComparator.getInstance());
        return diff;
    }

    public final static class JarUtil {

        public static final String CLASS_EXT = ".class";

        private JarUtil() {
            // no-op
        }

        public static List<String> listFiles(File archive, String[] extensions) throws IOException {
            if (!archive.exists() || !archive.isFile()) {
                throw new IllegalArgumentException(archive.getPath() + " is not a file");
            }

            List<String> files = new ArrayList<String>();

            JarFile file = new JarFile(archive);
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                for (String ext : extensions) {
                    if (name.endsWith(ext)) {
                        if (CLASS_EXT.equals(ext)) {
                            files.add(name.replace("/", "."));
                        } else {
                            files.add(name);
                        }
                        break;
                    }
                }
            }

            return files;
        }
    }

    public static class DiffItem {
        private Collection<String> files = new ArrayList<String>();
        private String file1;
        private String file2;

        public DiffItem(Collection<String> files, String file1, String file2) {
            this.files = files;
            this.file1 = file1;
            this.file2 = file2;
        }

        public String getFile1() {
            return file1;
        }

        public String getFile2() {
            return file2;
        }

        public String toScreen() {
            final String str = "both files " + file1 + '\''
                    + " and " + file2 + '\'';
            if (Boolean.getBoolean(OPENEJB_CHECK_CLASSLOADER_VERBOSE)) {
                    return str + " contains files=" + files;
            }
            return str;
        }
    }

    public static class ContainingItem extends DiffItem {
        public ContainingItem(Collection<String> inter, String dir1, String dir2) {
            super(inter, dir1, dir2);
        }

        @Override public String toScreen() {
            return getFile1() + " contains " + getFile2();
        }
    }

    public static class IncludedItem extends DiffItem {
        public IncludedItem(Collection<String> files, String file1, String file2) {
            super(files, file1, file2);
        }

        @Override public String toScreen() {
            return getFile1() + " is included inside " + getFile2();
        }
    }

    public static class SameItem extends DiffItem {
        public SameItem(Collection<String> files, String file1, String file2) {
            super(files, file1, file2);
        }

        @Override public String toScreen() {
            return getFile1() + " is the same than " + getFile2();
        }
    }

    public static class DiffItemComparator implements Comparator<DiffItem> {
        private static final DiffItemComparator INSTANCE = new DiffItemComparator();
        private static final Map<Class<?>, Integer> ORDER = new HashMap<Class<?>, Integer>();
        static {
            ORDER.put(SameItem.class, 0);
            ORDER.put(IncludedItem.class, 1);
            ORDER.put(ContainingItem.class, 2);
            ORDER.put(DiffItem.class, 3);
        }


        public static DiffItemComparator getInstance() {
            return INSTANCE;
        }

        @Override public int compare(DiffItem o1, DiffItem o2) {
            int index1 = ORDER.get(o1.getClass());
            int index2 = ORDER.get(o2.getClass());
            if (index1 == index2) {
                return o1.getFile1().compareTo(o1.getFile1());
            }
            return index1 - index2;
        }
    }
}
