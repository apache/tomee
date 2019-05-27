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

package org.apache.openejb.config.rules;

import org.apache.commons.collections.CollectionUtils;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.SystemInstance;
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

    @Override
    public void validate(final AppModule appModule) {
        this.appModule = appModule;
        module = appModule;
        check(appModule.getClassLoader());

        for (final WebModule webModule : appModule.getWebModules()) {
            module = webModule;
            validate(webModule);
        }
        super.validate(appModule);
    }

    private void check(final ClassLoader classLoader) {
        UrlSet set;
        final UrlSet openejbSet;
        try {
            openejbSet = new UrlSet(OpenEJB.class.getClassLoader());
            set = new UrlSet(classLoader);
            set = set.exclude(openejbSet);
        } catch (final IOException e) {
            warn(module.getModuleId() + " application", e.getMessage());
            return;
        }

        final List<URL> parentUrls = openejbSet.getUrls();
        final List<URL> currentUrls = set.getUrls();

        final Classes fcl = new Classes(currentUrls.toArray(new URL[currentUrls.size()]));
        final Classes scl = new Classes(parentUrls.toArray(new URL[parentUrls.size()]));
        final Collection<DiffItem> diffs = intersection(fcl, scl);
        for (final DiffItem diff : diffs) {
            warn(module.getModuleId() + " application", diff.toScreen());
        }
    }

    private void validate(final WebModule webModule) {
        check(webModule.getClassLoader());
    }

    @Override
    public void validate(final ClientModule clientModule) {
        check(clientModule.getClassLoader());
    }

    @Override
    public void validate(final EjbModule ejbModule) {
        check(ejbModule.getClassLoader());
    }

    public static class Classes {
        private static final String[] CLASS_EXTENSION = new String[]{".class"};

        private final Map<String, Collection<String>> fileByArchive = new TreeMap<>();

        public Classes(final URL[] urls) {
            list(urls);
        }

        public void list(final URL[] urls) {
            fileByArchive.clear();

            // for all archives list all files
            // all is sorted by the natural String order
            for (final URL archive : urls) {
                try {
                    final File file = URLs.toFile(archive);
                    final List<String> files = JarUtil.listFiles(file, CLASS_EXTENSION);
                    Collections.sort(files);
                    fileByArchive.put(file.getName(), files);
                } catch (final Exception e) {
                    // ignored
                }
            }
        }
    }

    public static Collection<DiffItem> intersection(final Classes cl1, final Classes cl2) {
        final List<DiffItem> diff = new ArrayList<>();
        for (final Map.Entry<String, Collection<String>> entry1 : cl1.fileByArchive.entrySet()) {
            for (final Map.Entry<String, Collection<String>> entry2 : cl2.fileByArchive.entrySet()) {
                final Collection<String> v1 = entry1.getValue();
                final Collection<String> v2 = entry2.getValue();
                final Collection<String> inter = CollectionUtils.intersection(v1, v2);

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

        diff.sort(DiffItemComparator.getInstance());
        return diff;
    }

    public static final class JarUtil {

        public static final String CLASS_EXT = ".class";

        private JarUtil() {
            // no-op
        }

        public static List<String> listFiles(final File archive, final String[] extensions) throws IOException {
            if (!archive.exists() || !archive.isFile()) {
                throw new IllegalArgumentException(archive.getPath() + " is not a file");
            }

            final List<String> files = new ArrayList<>();

            try (JarFile file = new JarFile(archive)) {
                final Enumeration<JarEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    for (final String ext : extensions) {
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
    }

    public static class DiffItem {
        private Collection<String> files = new ArrayList<>();
        private final String file1;
        private final String file2;

        public DiffItem(final Collection<String> files, final String file1, final String file2) {
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
            if (SystemInstance.get().getOptions().get(OPENEJB_CHECK_CLASSLOADER_VERBOSE, false)) {
                return str + " contains files=" + files;
            }
            return str;
        }
    }

    public static class ContainingItem extends DiffItem {
        public ContainingItem(final Collection<String> inter, final String dir1, final String dir2) {
            super(inter, dir1, dir2);
        }

        @Override
        public String toScreen() {
            return getFile1() + " contains " + getFile2();
        }
    }

    public static class IncludedItem extends DiffItem {
        public IncludedItem(final Collection<String> files, final String file1, final String file2) {
            super(files, file1, file2);
        }

        @Override
        public String toScreen() {
            return getFile1() + " is included inside " + getFile2();
        }
    }

    public static class SameItem extends DiffItem {
        public SameItem(final Collection<String> files, final String file1, final String file2) {
            super(files, file1, file2);
        }

        @Override
        public String toScreen() {
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

        @Override
        public int compare(final DiffItem o1, final DiffItem o2) {
            final int index1 = ORDER.get(o1.getClass());
            final int index2 = ORDER.get(o2.getClass());
            if (index1 == index2) {
                return o1.getFile1().compareTo(o1.getFile1());
            }
            return index1 - index2;
        }
    }
}
