/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.xbean.finder;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.ResourceDiscoveryFilter;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @version $Rev$ $Date$
 */
public class BundleAnnotationFinder extends AbstractFinder {
    private final Bundle bundle;
    private final Set<String> paths;

    public BundleAnnotationFinder(PackageAdmin packageAdmin, Bundle bundle) throws Exception {
        this(packageAdmin, bundle, BundleResourceFinder.FULL_DISCOVERY_FILTER);
    }

    public BundleAnnotationFinder(PackageAdmin packageAdmin, Bundle bundle, ResourceDiscoveryFilter discoveryFilter) throws Exception {
        this(packageAdmin, bundle, discoveryFilter, Collections.<String>emptySet());
    }

    public BundleAnnotationFinder(PackageAdmin packageAdmin, Bundle bundle, ResourceDiscoveryFilter discoveryFilter, Set<String> paths) throws Exception {
        this.bundle = BundleUtils.unwrapBundle(bundle);
        BundleResourceFinder bundleResourceFinder = new BundleResourceFinder(packageAdmin, this.bundle, "", ".class", discoveryFilter);
        bundleResourceFinder.find(new AnnotationFindingCallback());
        this.paths = paths;
    }

    @Override
    protected URL getResource(String s) {
        return bundle.getResource(s);
    }

    @Override
    protected Class<?> loadClass(String s) throws ClassNotFoundException {
        return bundle.loadClass(s);
    }

    @Override
    public List<String> getAnnotatedClassNames() {
        List<String> classNames = new ArrayList<String>(originalInfos.size());
        for (Map.Entry<String, ClassInfo> entry: originalInfos.entrySet()) {
            if (paths.contains(entry.getValue().getPath())) {
                classNames.add(entry.getKey());
            }
        }
        return classNames;
    }

    private class AnnotationFindingCallback implements BundleResourceFinder.ResourceFinderCallback {
      
        public boolean foundInDirectory(Bundle bundle, String baseDir, URL url) throws Exception {
            InputStream in = url.openStream();
            try {
                readClassDef(in, baseDir);
            } finally {
                in.close();
            }
            return true;
        }

       
        public boolean foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
            readClassDef(in, jarName);
            return true;
        }
    }

}
