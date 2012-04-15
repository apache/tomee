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

import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.ResourceDiscoveryFilter;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.zip.ZipEntry;

/**
 * TODO Unfinished
 * @version $Rev$ $Date$
 */
public class BundleArchive implements Archive {

    private final Bundle bundle;

    public BundleArchive(PackageAdmin packageAdmin, Bundle bundle) throws Exception {
        this(packageAdmin, bundle, BundleResourceFinder.FULL_DISCOVERY_FILTER);
    }

    public BundleArchive(PackageAdmin packageAdmin, Bundle bundle, ResourceDiscoveryFilter discoveryFilter) throws Exception {
        this.bundle = bundle;
        BundleResourceFinder bundleResourceFinder = new BundleResourceFinder(packageAdmin, bundle, "", ".class", discoveryFilter);
        bundleResourceFinder.find(new AnnotationFindingCallback());
    }

    public Iterator<Entry> iterator() {
        return Collections.EMPTY_LIST.iterator();
    }

    public InputStream getBytecode(String className) throws IOException, ClassNotFoundException {
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

        URL resource = bundle.getResource(className);
        if (resource != null) return resource.openStream();

        throw new ClassNotFoundException(className);
    }

    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return bundle.loadClass(s);
    }

    private class AnnotationFindingCallback implements BundleResourceFinder.ResourceFinderCallback {

        public boolean foundInDirectory(Bundle bundle, String baseDir, URL url) throws Exception {
            InputStream in = url.openStream();
            try {
                //TODO
//                readClassDef(in);
            } finally {
                in.close();
            }
            return true;
        }


        public boolean foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
            //TODO
//            readClassDef(in);
            return true;
        }
    }


}